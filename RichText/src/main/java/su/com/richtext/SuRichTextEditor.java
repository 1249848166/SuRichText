package su.com.richtext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import su.com.richtext.activity.SuSelectAudioActivity;
import su.com.richtext.activity.SuSelectImageActivity;
import su.com.richtext.activity.SuSelectVideoActivity;
import su.com.richtext.utils.SuImageLoader;
import su.com.richtext.view.HttpDrawable;
import su.com.richtext.view.SuDropableTextView;
import su.com.richtext.callback.SuResultCallback;
import su.com.richtext.callback.SuSearchCallback;
import su.com.richtext.callback.SuSubmit;
import su.com.richtext.callback.SuUploadAudio;
import su.com.richtext.callback.SuUploadCallback;
import su.com.richtext.callback.SuUploadImage;
import su.com.richtext.callback.SuUploadVideo;
import su.com.richtext.widget.SuAudioPlayer;
import su.com.richtext.widget.SuVideoPlayer;

public class SuRichTextEditor {

    public static final int TYPE_VIDEO = 1001;
    public static final int TYPE_AUDIO = 1002;
    public static final int TYPE_PICTURE = 1003;

    public static final int CODE_REQUEST_VIDEO = 1004;
    public static final int CODE_REQUEST_AUDIO = 1005;
    public static final int CODE_REQUEST_PICTURE = 1006;

    private List<String> dataList;
    private LinearLayout container;

    @SuppressLint("UseSparseArrays")
    private Map<Integer, String> contentMap = new HashMap<>();

    private SuSubmit suSubmit;
    private SuResultCallback suResultCallback;
    private String title;
    private String author;
    private String others;

    private int finished=0;

    int screenWidth;

    Map<String,SuAudioPlayer> audioPlayerMap =new HashMap<>();
    Map<String,SuVideoPlayer> videoPlayerMap =new HashMap<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
                for(SuVideoPlayer suVideoPlayer : videoPlayerMap.values()){
                    if(suVideoPlayer!=null&&!suVideoPlayer.isFirst())
                        if(suVideoPlayer.isPlaying()){
                            suVideoPlayer.update();
                        }
                }
            if(audioPlayerMap!=null&&audioPlayerMap.size()>0)
                for(SuAudioPlayer suAudioPlayer : audioPlayerMap.values()){
                    if(suAudioPlayer!=null&&suAudioPlayer.isPrepared())
                        if(suAudioPlayer.isPlaying()){
                            suAudioPlayer.update();
                        }
                }
            loop();
        }
    };

    private void loop(){
        handler.sendEmptyMessageDelayed(0,500);
    }

    public void onResume(){
        if(audioPlayerMap!=null&&audioPlayerMap.size()>0)
            for(SuAudioPlayer suAudioPlayer : audioPlayerMap.values()){
                if(suAudioPlayer!=null&&!suAudioPlayer.isPlaying()&&suAudioPlayer.isPreIsPlaying()) {
                    suAudioPlayer.start();
                }
            }
        System.out.println("audioPlayerMap已开始");
        if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
            for(SuVideoPlayer suVideoPlayer : videoPlayerMap.values()){
                if(suVideoPlayer!=null&&!suVideoPlayer.isPlaying()&&suVideoPlayer.isPreIsPlaying()) {
                    suVideoPlayer.start();
                }
            }
        System.out.println("videoPlayerMap已开始");
    }

    public void onPause(){
        if(audioPlayerMap!=null&&audioPlayerMap.size()>0)
            for(SuAudioPlayer suAudioPlayer : audioPlayerMap.values()){
                if(suAudioPlayer!=null&&suAudioPlayer.isPlaying()) {
                    suAudioPlayer.setPreIsPlaying(true);
                    suAudioPlayer.pause();
                }
                if(suAudioPlayer!=null&&!suAudioPlayer.isPlaying()) {
                    suAudioPlayer.setPreIsPlaying(false);
                }
            }
        System.out.println("audioPlayerMap已暂停");
        if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
            for(SuVideoPlayer suVideoPlayer : videoPlayerMap.values()){
                if(suVideoPlayer!=null&&suVideoPlayer.isPlaying()) {
                    suVideoPlayer.setPreIsPlaying(true);
                    suVideoPlayer.pause();
                }
                if(suVideoPlayer!=null&&!suVideoPlayer.isPlaying()) {
                    suVideoPlayer.setPreIsPlaying(false);
                }
            }
        System.out.println("videoPlayerMap已暂停");
    }

    public void onEnd(){
        if(audioPlayerMap!=null&&audioPlayerMap.size()>0)
            for(SuAudioPlayer suAudioPlayer : audioPlayerMap.values()){
                suAudioPlayer.release();
            }
        audioPlayerMap.clear();
        System.out.println("audioPlayerMap已回收");
        if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
            for(SuVideoPlayer suVideoPlayer : videoPlayerMap.values()){
                if(suVideoPlayer!=null) {
                    suVideoPlayer.release();
                }
            }
        videoPlayerMap.clear();
        System.out.println("videoPlayerMap已回收");
    }

    private SuUploadCallback suUploadCallback = new SuUploadCallback() {
        @Override
        public void onReturn(int index, String url) {
            contentMap.put(index, url);
            finished++;
            if (finished == dataList.size()) {
                String content = getStringConcatFromMap(contentMap);
                suSubmit.submit(title,author,content,others, suResultCallback);
            }
        }
    };

    private Context context;

    @SuppressLint("StaticFieldLeak")
    private static SuRichTextEditor instance;

    public static SuRichTextEditor getInstance(Context context, LinearLayout container) {
        if (instance == null) {
            synchronized (SuRichTextEditor.class) {
                if (instance == null) {
                    instance = new SuRichTextEditor(context, container);
                }
            }
        }
        return instance;
    }

    public SuRichTextEditor(Context context, LinearLayout container) {
        this.container = container;
        dataList = new ArrayList<>();
        this.context = context;
        DisplayMetrics metrics = container.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        handler.sendEmptyMessage(0);
    }

    public void insertText(final String text) {
        dataList.add("[text]" + text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(params);
        final SuDropableTextView textView = new SuDropableTextView(context);
        textView.setLayoutParams(params);
        Spanned spanned = Html.fromHtml(text, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String s) {
                HttpDrawable httpDrawable = new HttpDrawable(null);
                SuRichTextEditor.Task task = new SuRichTextEditor.Task(textView, httpDrawable);
                task.execute(s);
                return httpDrawable;
            }
        }, new Html.TagHandler() {
            @Override
            public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {
                System.out.println(s + ":" + editable.toString());
            }
        });
        textView.setText(spanned);
        textView.setBackgroundColor(Color.parseColor("#dddddd"));
        textView.setPadding(10, 10, 10, 10);
        params.setMargins(10, 10, 10, 10);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        Drawable drawable = context.getResources().getDrawable(R.drawable.clear);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        textView.setCompoundDrawables(null, null, drawable, null);
        textView.setCompoundDrawablePadding(10);
        textView.setTextColor(Color.BLACK);
        textView.setDrawableRightClick(new SuDropableTextView.DrawableRightClickListener() {
            @Override
            public void onDrawableRightClickListener(View view) {
                Animation animation = new ScaleAnimation(1, 0.1f, 1, 0.1f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(200);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        container.removeView(layout);
                        dataList.remove(text);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                textView.startAnimation(animation);
            }
        });
        layout.addView(textView);
        container.addView(layout);
    }

    @SuppressLint("StaticFieldLeak")
    class Task extends AsyncTask<String, Void, Drawable> {

        SoftReference<TextView> textView;
        SoftReference<HttpDrawable> mDrawable;

        Task(TextView textView,HttpDrawable drawable) {
            this.textView = new SoftReference<>(textView);
            mDrawable = new SoftReference<>(drawable);
        }

        @Override
        protected Drawable doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                connection.connect();
                int code = connection.getResponseCode();
                if (code == 200) {
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return new BitmapDrawable(bitmap);
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            mDrawable.get().setDrawable(drawable);
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int w = metrics.widthPixels;
            int h = metrics.heightPixels;
            float ratio = (float) drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
            mDrawable.get().setBounds(0, 0, w, (int) (h * ratio));
            mDrawable.get().getDrawable().setBounds(0, 0, w, (int) (h * ratio));
            textView.get().setText(textView.get().getText());
        }
    }

    @SuppressLint("SetTextI18n")
    public void insertAudio(final String path) {
        dataList.add("[audio]" + path);
        final SuAudioPlayer player=new SuAudioPlayer(context, path, new SuAudioPlayer.Callback() {
            @Override
            public void onCompletion(MediaPlayer player) {

            }

            @Override
            public void onError(String msg) {

            }

            @Override
            public void onPrepared() {

            }

            @Override
            public void onUpdate(float progress) {

            }

            @Override
            public void onStateChange(boolean isPlaying) {
                if(isPlaying){
                    if(audioPlayerMap!=null&&audioPlayerMap.size()>0)
                        for (SuAudioPlayer suAudioPlayer : audioPlayerMap.values()) {
                            if (!suAudioPlayer.getUrl().equals(path)) {
                                if (suAudioPlayer.isPrepared() && suAudioPlayer.isPlaying()) {
                                    suAudioPlayer.pause();
                                    suAudioPlayer.getControllor().setImageResource(R.drawable.play_white);
                                }
                            }
                        }
                }
            }

        });
        player.setCanDrop(true, new SuAudioPlayer.DropListener() {
            @Override
            public void drop() {
                player.dropFrom(container);
                dataList.remove("[audio]" + path);
            }
        });
        player.insertInto(container);
        audioPlayerMap.put(path,player);
    }

    public void insertVideo(final String path) {
        dataList.add("[video]" + path);
        final SuVideoPlayer player = new SuVideoPlayer(context, path, new SuVideoPlayer.Callback() {
            @Override
            public void onStateChange(boolean isPlaying) {
                if(isPlaying){
                    if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
                        for (SuVideoPlayer suVideoPlayer : videoPlayerMap.values()) {
                            if (!suVideoPlayer.getUrl().equals(path)) {
                                if (!suVideoPlayer.isFirst() && suVideoPlayer.isPlaying()) {
                                    suVideoPlayer.pause();
                                    suVideoPlayer.getControllor().setImageResource(R.drawable.play_white);
                                }
                            }
                        }
                }
            }
        });
        player.setCanDrop(true, new SuVideoPlayer.DropListener() {
            @Override
            public void drop() {
                player.dropFrom(container);
                dataList.remove("[video]" + path);
            }
        });
        player.insertInto(container);
        videoPlayerMap.put(path,player);
    }

    @SuppressLint("RtlHardcoded")
    public void insertImage(final String path) {
        try {
            dataList.add("[img]" + path);
            final RelativeLayout layout=new RelativeLayout(context);
            final ImageView imageView = new ImageView(context);
            SuImageLoader.getInstance(context).loadImage(path, imageView, new SuImageLoader.OnLoaded() {
                @Override
                public void onLoaded(Bitmap bitmap) {
                    try {
                        float ratio = (float) (screenWidth - 20) / bitmap.getWidth();
                        int width = screenWidth - 20;
                        int height = (int) (ratio * bitmap.getHeight());
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                        params.setMargins(10, 20, 10, 20);
                        imageView.setLayoutParams(params);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
            layout.addView(imageView);
            RelativeLayout r=new RelativeLayout(context);
            RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            r.setLayoutParams(params);
            r.setGravity(Gravity.END);
            ImageView drop=new ImageView(context);
            drop.setImageResource(R.mipmap.clear);
            drop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dataList.remove("[img]" + path);
                    container.removeView(layout);
                }
            });
            r.addView(drop);
            layout.addView(r);
            container.addView(layout);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void submit(String title, String author, String others, SuUploadImage suUploadImage, SuUploadAudio suUploadAudio, SuUploadVideo suUploadVideo,
                       SuSubmit suSubmit, SuResultCallback suResultCallback) {
        this.suSubmit = suSubmit;
        this.suResultCallback = suResultCallback;
        this.title=title;
        this.author=author;
        this.others=others;
        for (int i = 0; i < dataList.size(); i++) {
            String data = dataList.get(i);
            if (data.startsWith("[text]")) {
                contentMap.put(i, data.replace("[text]", ""));
                finished++;
                if (finished == dataList.size()) {
                    String content = getStringConcatFromMap(contentMap);
                    suSubmit.submit(title,"",content,"", suResultCallback);
                }
            } else if (data.startsWith("[img]")) {
                if (suUploadImage != null)
                    suUploadImage.upload(data.replace("[img]", ""), i, suUploadCallback);
            } else if (data.startsWith("[audio]")) {
                if (suUploadAudio != null)
                    suUploadAudio.upload(data.replace("[audio]", ""), i, suUploadCallback);
            } else if (data.startsWith("[video]")) {
                if (suUploadVideo != null)
                    suUploadVideo.upload(data.replace("[video]", ""), i, suUploadCallback);
            }
        }
    }

    private void clearContainer() {
        container.removeAllViews();
        dataList.clear();
        contentMap.clear();
    }

    public void SearchFileDatas(int type, final SuSearchCallback callback) {
        try {
            switch (type) {
                case TYPE_VIDEO:
                    if (callback == null) {//跳转到选择界面
                        Intent intent1 = new Intent(context, SuSelectVideoActivity.class);
                        ((Activity) context).startActivityForResult(intent1, CODE_REQUEST_VIDEO);
                    } else {
                        //TODO 获取所有视频路径
                    }
                    break;
                case TYPE_AUDIO:
                    if (callback == null) {
                        Intent intent2 = new Intent(context,SuSelectAudioActivity.class);
                        ((Activity) context).startActivityForResult(intent2, CODE_REQUEST_AUDIO);
                    } else {
                        //TODO 获取所有音频路径
                    }
                    break;
                case TYPE_PICTURE:
                    if(callback==null) {
                        Intent intent3 = new Intent(context, SuSelectImageActivity.class);
                        ((Activity) context).startActivityForResult(intent3, CODE_REQUEST_PICTURE);
                    }else{
                        //TODO 获取所有图片路径
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getDataList() {
        return dataList;
    }

    private String getStringConcatFromMap(Map map) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < dataList.size(); i++) {
            String data = dataList.get(i);
            if (data.startsWith("[text]")) {
                content.append(map.get(i)).append("<br>");
            } else if (data.startsWith("[img]")) {
                content.append("[img](").append(map.get(i)).append(")").append("<br>");
            } else if (data.startsWith("[audio]")) {
                content.append("[audio](").append(map.get(i)).append(")").append("<br>");
            } else if (data.startsWith("[video]")) {
                content.append("[video](").append(map.get(i)).append(")").append("<br>");
            }
        }
        clearContainer();
        return content.toString();
    }
}
