package su.com.surichtext.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import su.com.richtext.SuRichTextEditor;
import su.com.richtext.callback.SuResultCallback;
import su.com.richtext.callback.SuSubmit;
import su.com.richtext.callback.SuUploadAudio;
import su.com.richtext.callback.SuUploadCallback;
import su.com.richtext.callback.SuUploadImage;
import su.com.richtext.callback.SuUploadVideo;
import su.com.surichtext.R;
import su.com.surichtext.utils.BmobUtil;

import static su.com.richtext.SuRichTextEditor.CODE_REQUEST_AUDIO;
import static su.com.richtext.SuRichTextEditor.CODE_REQUEST_PICTURE;

public class WriteActivity extends AppCompatActivity {

    SuRichTextEditor richTextEditor;
    EditText edit;
    View insert_text, insert_raw;
    TextView updating_text;
    ProgressBar updating_progress;
    ImageView finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        richTextEditor=new SuRichTextEditor(this, (LinearLayout) findViewById(R.id.container));

        updating_progress=findViewById(R.id.updating_progress);
        updating_text=findViewById(R.id.updating_text);
        finish=findViewById(R.id.finish);

        edit=findViewById(R.id.txt);
        insert_raw =findViewById(R.id.img);
        insert_raw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    insert_raw.setFocusable(true);
                    insert_raw.setFocusableInTouchMode(true);
                    insert_raw.requestFocus();
                    final Dialog switchDialog=new Dialog(WriteActivity.this);
                    @SuppressLint("InflateParams") View content = LayoutInflater.from(WriteActivity.this).inflate(R.layout.dialog, null);
                    switchDialog.setContentView(content);
                    TextView img = content.findViewById(R.id.img);
                    TextView audio = content.findViewById(R.id.audio);
                    TextView video = content.findViewById(R.id.video);
                    TextView cancel = content.findViewById(R.id.cancel);
                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchDialog.dismiss();
                            richTextEditor.SearchFileDatas(SuRichTextEditor.TYPE_PICTURE, null);
                        }
                    });
                    audio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchDialog.dismiss();
                            richTextEditor.SearchFileDatas(SuRichTextEditor.TYPE_AUDIO, null);
                        }
                    });
                    video.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchDialog.dismiss();
                            richTextEditor.SearchFileDatas(SuRichTextEditor.TYPE_VIDEO, null);
                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switchDialog.dismiss();
                        }
                    });
                    switchDialog.show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        insert_text=findViewById(R.id.btn);
        insert_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    richTextEditor.insertText(edit.getText().toString());
                    edit.setText("");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(richTextEditor.getDataList().size()==0){
                    Toast.makeText(WriteActivity.this, "没有内容", Toast.LENGTH_SHORT).show();
                    return;
                }
                final Dialog dialog=new Dialog(WriteActivity.this);
                @SuppressLint("InflateParams")
                View content=LayoutInflater.from(WriteActivity.this).inflate(R.layout.submit,null);
                final EditText title=content.findViewById(R.id.title);
                final EditText others=content.findViewById(R.id.others);
                final TextView ok=content.findViewById(R.id.ok);
                final TextView no=content.findViewById(R.id.no);
                dialog.setContentView(content);
                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        insert_raw.setFocusable(true);
                        insert_raw.setFocusableInTouchMode(true);
                        insert_raw.requestFocus();
                        if(!title.getText().toString().trim().equals("")) {
                            dialog.dismiss();
                            submit(title.getText().toString().trim(),"",others.getText().toString().trim());
                            updating_progress.setVisibility(View.VISIBLE);
                            updating_text.setVisibility(View.VISIBLE);
                            finish.setVisibility(View.INVISIBLE);
                        }else{
                            Toast.makeText(WriteActivity.this, "请输入标题", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        insert_raw.setFocusable(true);
                        insert_raw.setFocusableInTouchMode(true);
                        insert_raw.requestFocus();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==SuRichTextEditor.CODE_REQUEST_VIDEO){
                List<String> paths=data.getStringArrayListExtra("paths");
                for(String path:paths){
                    System.out.println("得到视频路径："+path);
                    richTextEditor.insertVideo(path);
                }
            }else if(requestCode== CODE_REQUEST_PICTURE){
                List<String> paths=data.getStringArrayListExtra("paths");
                for(String path:paths){
                    System.out.println("得到图片路径："+path);
                    richTextEditor.insertImage(path);
                }
            }else if(requestCode==CODE_REQUEST_AUDIO){
                List<String> paths=data.getStringArrayListExtra("paths");
                for(String path:paths){
                    System.out.println("得到音频路径："+path);
                    richTextEditor.insertAudio(path);
                }
            }
        }
    }

    void submit(String title,String author,String others){
        //最后提交内容到服务器(完整文本，图片，视频，音频拼接成的字符窜)需要自己写具体将各种类别传到服务器的代码，
        // 因为实际上可能有视频服务器和普通服务器的区别，所以没有合并成一个接口
        richTextEditor.submit(title,author,others,
                new SuUploadImage() {//提交图片获得url
            @Override
            public void upload(String path, final int index, final SuUploadCallback suUploadCallback) {
                //这里填写将图片上传到服务器的代码，会得到一个url
                BmobUtil.getInstance().uploadFile(new String[]{path}, "", new BmobUtil.UploadFileCallback() {
                    @Override
                    public void onUploadFile(String url) {
                        suUploadCallback.onReturn(index,url);//将得到的url返回
                        System.out.println(url);
                    }
                });
            }
        }, new SuUploadAudio() {//提交音频获得url
            @Override
            public void upload(String path, final int index, final SuUploadCallback suUploadCallback) {
                //这里填写将音频上传到服务器的代码，会得到一个url
                BmobUtil.getInstance().uploadFile(new String[]{path}, "", new BmobUtil.UploadFileCallback() {
                    @Override
                    public void onUploadFile(String url) {
                        suUploadCallback.onReturn(index,url);//将得到的url返回
                        System.out.println(url);
                    }
                });
            }
        }, new SuUploadVideo() {//提交视频获得url
            @Override
            public void upload(String path, final int index, final SuUploadCallback suUploadCallback) {
                //这里填写将视频上传到服务器的代码，会得到一个url
                BmobUtil.getInstance().uploadFile(new String[]{path}, "", new BmobUtil.UploadFileCallback() {
                    @Override
                    public void onUploadFile(String url) {
                        suUploadCallback.onReturn(index,url);//将得到的url返回
                        System.out.println(url);
                    }
                });
            }
        },new SuSubmit() {//最后一步提交
            @Override
            public void submit(String title,String author,String content,String others, final SuResultCallback suResultCallback) {
                //这里填写最后将文本，图片url，音频url，视频url提交到服务器的代码
                BmobUtil.getInstance().uploadContent(title, content, new BmobUtil.UploadContentCallback() {
                    @Override
                    public void onUploadContent(boolean success) {
                        suResultCallback.onResult(success);//通知结果
                    }
                });
                System.out.println("最后整合成的文本:"+content);
            }
        }, new SuResultCallback() {//最后提交的返回结果，成功或失败
            @Override
            public void onResult(boolean success) {
                updating_progress.setVisibility(View.INVISIBLE);
                updating_text.setVisibility(View.INVISIBLE);
                finish.setVisibility(View.VISIBLE);
                //返回结果
                if(success) {
                    Toast.makeText(WriteActivity.this, "提交成功" , Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(WriteActivity.this, "提交失败" , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        richTextEditor.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        richTextEditor.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        richTextEditor.onEnd();
    }
}
