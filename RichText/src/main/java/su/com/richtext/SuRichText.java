package su.com.richtext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import su.com.richtext.utils.SuImageLoader;
import su.com.richtext.view.HttpDrawable;
import su.com.richtext.widget.SuAudioPlayer;
import su.com.richtext.widget.SuVideoPlayer;

public class SuRichText extends ScrollView {

    Context context;
    LinearLayout container;
    Spanned spanned;
    int screenWidth;

    private Map<String,SuAudioPlayer> audioPlayerMap =new HashMap<>();
    private Map<String,SuVideoPlayer> videoPlayerMap =new HashMap<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
                for(SuVideoPlayer suVideoPlayer : videoPlayerMap.values()){
                    if(!suVideoPlayer.isFirst())
                        if(suVideoPlayer.isPlaying()){
                            suVideoPlayer.update();
                        }
                }
            if(audioPlayerMap!=null&&audioPlayerMap.size()>0)
                for(SuAudioPlayer suAudioPlayer : audioPlayerMap.values()){
                    if(suAudioPlayer.isPrepared())
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

    public SuRichText(Context context) {
        this(context, null);
    }

    public SuRichText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SuRichText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    void init(Context context) {
        try {
            this.context = context;
            container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);
            ScrollView.LayoutParams params = new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.setLayoutParams(params);
            this.addView(container);
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            screenWidth = metrics.widthPixels;
            handler.sendEmptyMessage(0);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setContent(String content) {
        try {
            String[] lines = content.split("<br>");
            for (String line : lines) {
                if (!line.equals("")) {
                    if (line.startsWith("[img]")) {
                        String url = line.replace("[img](", "").replace(")", "");
                        setImage(url);
                    } else if (line.startsWith("[audio]")) {
                        String url = line.replace("[audio](", "").replace(")", "");
                        setAudio(url);
                    } else if (line.startsWith("[video]")) {
                        String url = line.replace("[video](", "").replace(")", "");
                        setVideo(url);
                    } else {
                        setHtmlText(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHtmlText(String htmlText) {
        final TextView textView = new TextView(context);
        spanned = Html.fromHtml(htmlText, new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String s) {
                HttpDrawable httpDrawable = new HttpDrawable(null);
                Task task = new Task(textView, httpDrawable);
                task.execute(s);
                return httpDrawable;
            }
        }, new Html.TagHandler() {
            @Override
            public void handleTag(boolean b, String s, Editable editable, XMLReader xmlReader) {
                System.out.println(s + ":" + editable.toString());
            }
        });
        textView.setTextSize(20);
        textView.setText(spanned);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,20,10,20);
        textView.setLayoutParams(params);
        container.addView(textView);
    }

    @SuppressLint("SetTextI18n")
    private void setAudio(final String url) {
        try {
            SuAudioPlayer player=new SuAudioPlayer(context, url, new SuAudioPlayer.Callback() {
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
                                if (!suAudioPlayer.getUrl().equals(url)) {
                                    if (suAudioPlayer.isPrepared() && suAudioPlayer.isPlaying()) {
                                        suAudioPlayer.pause();
                                        suAudioPlayer.getControllor().setImageResource(R.drawable.play_black);
                                    }
                                }
                            }
                    }
                }
            });
            player.insertInto(container);
            audioPlayerMap.put(url,player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setVideo(final String url) {
        try {
            SuVideoPlayer player = new SuVideoPlayer(context, url, new SuVideoPlayer.Callback() {
                @Override
                public void onStateChange(boolean isPlaying) {
                    if(isPlaying){
                        if(videoPlayerMap!=null&&videoPlayerMap.size()>0)
                            for (SuVideoPlayer suVideoPlayer : videoPlayerMap.values()) {
                                if (!suVideoPlayer.getUrl().equals(url)) {
                                    if (!suVideoPlayer.isFirst() && suVideoPlayer.isPlaying()) {
                                        suVideoPlayer.pause();
                                        suVideoPlayer.getControllor().setImageResource(R.drawable.play_black);
                                    }
                                }
                            }
                    }
                }
            });
            player.insertInto(container);
            videoPlayerMap.put(url,player);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void setImage(String url) {
        try {
            final ImageView imageView = new ImageView(context);
            SuImageLoader.getInstance(context).loadImage(url, imageView, new SuImageLoader.OnLoaded() {
                @Override
                public void onLoaded(Bitmap bitmap) {
                    float ratio=(float) (screenWidth-20)/bitmap.getWidth();
                    int width=screenWidth-20;
                    int height=(int) (ratio*bitmap.getHeight());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
                    params.setMargins(10,20,10,20);
                    imageView.setLayoutParams(params);
                }
            });
            container.addView(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @SuppressLint("StaticFieldLeak")
    class Task extends AsyncTask<String, Void, Drawable> {

        SoftReference<TextView> textView;
        SoftReference<HttpDrawable> mDrawable;

        Task(TextView textView, HttpDrawable drawable) {
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
}
