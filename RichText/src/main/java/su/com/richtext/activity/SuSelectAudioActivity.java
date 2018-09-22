package su.com.richtext.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import su.com.richtext.R;
import su.com.richtext.adapter.SuAudioAdapter;
import su.com.richtext.adapter.SuAudioFolderAdapter;
import su.com.richtext.decoration.SuDecoration;
import su.com.richtext.decoration.SuFolderDecoration;
import su.com.richtext.model.SuFolder;

public class SuSelectAudioActivity extends AppCompatActivity implements SuAudioFolderAdapter.SuAudioFolderSelectCallback,View.OnClickListener ,SuAudioAdapter.SuAudioSelectCallback{

    RecyclerView audioRecycler;
    SuAudioAdapter audioAdapter;
    SuDecoration audioDecoration;
    List<String> audioPaths;
    int col=2;
    int space=1;
    int maxSize=5;
    int screenWidth,screenHeight;

    TextView foldername,num,submit;

    SuFolder selectedFolder;

    RecyclerView audioFolderRecyclerView;
    List<SuFolder> audioFolders;
    SuAudioFolderAdapter audioFolderAdapter;
    SuFolderDecoration audioFolderDecoration;

    PopupWindow popupWindow;

    final int MSG_UPDATE_POPUP=1;
    final int MSG_UPDATE_PANEL=2;

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==MSG_UPDATE_POPUP)
                audioFolderAdapter.notifyDataSetChanged();
            else if(msg.what==MSG_UPDATE_PANEL) {
                audioAdapter.notifyDataSetChanged();
                foldername.setText(selectedFolder.getFolderName());
                num.setText(selectedFolder.getNum()+"张");
                popupWindow.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        init();
    }

    private void init(){
        foldername=findViewById(R.id.foldername);
        num=findViewById(R.id.num);
        submit=findViewById(R.id.submit);
        submit.setOnClickListener(this);

        DisplayMetrics metrcs=getResources().getDisplayMetrics();
        screenWidth=metrcs.widthPixels;
        screenHeight=metrcs.heightPixels;

        audioRecycler =findViewById(R.id.imagerecycler);
        audioPaths =new ArrayList<>();
        audioAdapter =new SuAudioAdapter(this, audioPaths,maxSize,this);
        audioRecycler.setAdapter(audioAdapter);
        audioRecycler.setLayoutManager(new GridLayoutManager(this,col));
        audioDecoration =new SuDecoration(space,col);
        audioRecycler.addItemDecoration(audioDecoration);

        View bottombar=findViewById(R.id.bottombar);
        bottombar.setOnClickListener(this);

        @SuppressLint("InflateParams")
        View popupwindowView= LayoutInflater.from(this).inflate(R.layout.layout_popupwindow_imagefolder,null);
        audioFolderRecyclerView =popupwindowView.findViewById(R.id.imagefolderrecycler);
        audioFolders =new ArrayList<>();
        audioFolderAdapter =new SuAudioFolderAdapter(this, audioFolders,this);
        audioFolderRecyclerView.setAdapter(audioFolderAdapter);
        audioFolderRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        audioFolderDecoration =new SuFolderDecoration(5);
        audioFolderRecyclerView.addItemDecoration(audioFolderDecoration);

        popupWindow=new PopupWindow(popupwindowView,screenWidth, (int) (screenHeight*0.8));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_corner_white));
        popupWindow.setAnimationStyle(R.style.PopupWindowAnim);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                try {
                    backgroundAlpha(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchImageFolders();
            }
        }).start();
    }

    void searchImageFolders(){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "没有存储卡", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = getContentResolver();
        Cursor cs = resolver.query(uri, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        Set<String> loopSet = new HashSet<>();
        assert cs != null;
        audioFolders.clear();
        while (cs.moveToNext()) {
            String path = cs.getString(cs.getColumnIndex(MediaStore.Audio.Media.DATA));
            File parentFile = new File(path).getParentFile();
            if (parentFile == null) {
                continue;
            }
            String dirPath = parentFile.getAbsolutePath();
            if (loopSet.contains(dirPath)) {
                continue;
            }
            loopSet.add(dirPath);
            SuFolder folder = new SuFolder(dirPath.substring(dirPath.lastIndexOf("/")+1, dirPath.length()), dirPath, path, 0);
            if (parentFile.list() == null) {
                continue;
            }
            int num = parentFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if (s.endsWith(".aac") || s.endsWith(".mp3") || s.endsWith(".pcm")|| s.endsWith(".amr")|| s.endsWith(".wav")) {
                        return true;
                    }
                    return false;
                }
            }).length;
            folder.setNum(num);
            audioFolders.add(folder);
        }
        cs.close();
        Message msg=handler.obtainMessage();
        msg.what=MSG_UPDATE_POPUP;
        handler.sendMessage(msg);

        if(audioFolders.size()>0){
            selectedFolder= audioFolders.get(0);
            setFolder(selectedFolder);
        }
    }

    void setFolder(SuFolder folder){
        File d=new File(folder.getFolderPath());
        if(d.exists()&&d.isDirectory()){
            File[] fs=d.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String s) {
                    if (s.endsWith(".aac") || s.endsWith(".mp3") || s.endsWith(".pcm")|| s.endsWith(".amr")|| s.endsWith(".wav")) {
                        return true;
                    }
                    return false;
                }
            });
            audioPaths.clear();
            for(File fi:fs){
                audioPaths.add(fi.getAbsolutePath());
            }
            Message message=handler.obtainMessage();
            message.what=MSG_UPDATE_PANEL;
            handler.sendMessage(message);
        }
    }

    void openPopupWindow(View view){
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        backgroundAlpha(0.3f);
    }

    void backgroundAlpha(float bgAlpha){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFolderSelect(SuFolder folder) {
        selectedFolder=folder;
        setFolder(selectedFolder);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bottombar) {
            openPopupWindow(audioRecycler);
        }
        if(i==R.id.submit){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            AlertDialog dialog=builder.create();
            dialog.setTitle("是否确定选择");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Intent intent=new Intent();
                    intent.putStringArrayListExtra("paths", (ArrayList<String>) audioAdapter.getSelectedList());
                    setResult(RESULT_OK,intent);
                    finish();
                }
            });
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onBackPressed() {
        if(popupWindow.isShowing())
            popupWindow.dismiss();
        else
            super.onBackPressed();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onImageSelected(String path,int size,int maxSize) {
        submit.setText(size+"/"+maxSize);
    }
}
