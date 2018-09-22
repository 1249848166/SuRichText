package su.com.richtext.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import su.com.richtext.R;
import su.com.richtext.utils.SuTimeUtil;
import static android.view.View.INVISIBLE;

public class SuVideoPlayer implements SurfaceHolder.Callback{

    private int screenWidth;
    private View parent;
    private MediaPlayer player;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private boolean isFirst=true;
    private String url;
    private SeekBar seekBar;
    private TextView text;
    private ImageView play;
    private ImageView drop;

    public interface DropListener{
        void drop();
    }

    private DropListener dropListener;

    public void setDropListener(DropListener dropListener) {
        this.dropListener = dropListener;
    }

    private void reset() {
        player.reset();
    }

    public void release() {
        player.stop();
        player.release();
        player=null;
    }

    public interface Callback{
        void onStateChange(boolean isPlaying);
    }

    public SuVideoPlayer(Context context, final String url,@Nullable final Callback callback) {
        try {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            screenWidth = metrics.widthPixels;
            this.url = url;
            parent = LayoutInflater.from(context).inflate(R.layout.video, null);
            surfaceView = parent.findViewById(R.id.surface);
            final ProgressBar progress = parent.findViewById(R.id.progress);
            play = parent.findViewById(R.id.play);
            seekBar = parent.findViewById(R.id.seekbar);
            text = parent.findViewById(R.id.text);
            seekBar.setMax(100);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        player.seekTo((int) (((float) seekBar.getProgress() / 100) * player.getDuration()));
                        text.setText(SuTimeUtil.getTimeString(player.getCurrentPosition()) + "/" + SuTimeUtil.getTimeString(player.getDuration()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            holder = surfaceView.getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onPrepared(MediaPlayer mp) {
                    try {
                        float ratio = (float) screenWidth / player.getVideoWidth();
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(screenWidth, (int) (ratio * player.getVideoHeight()));
                        surfaceView.setLayoutParams(params);
                        player.start();
                        player.pause();
                        text.setText(SuTimeUtil.getTimeString(player.getCurrentPosition())+"/"+ SuTimeUtil.getTimeString(player.getDuration()));
                        progress.setVisibility(INVISIBLE);
                        isFirst = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    play.setImageResource(R.drawable.play_green);
                    seekBar.setProgress(0);
                }
            });
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(player.isPlaying()){
                        player.pause();
                        play.setImageResource(R.drawable.play_green);
                        if(callback!=null)
                            callback.onStateChange(false);
                    }else{
                        player.start();
                        play.setImageResource(R.drawable.pause_red);
                        if(callback!=null)
                            callback.onStateChange(true);
                    }
                }
            });
            drop=parent.findViewById(R.id.drop);
            drop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(dropListener!=null)
                        dropListener.drop();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCanDrop(boolean canDrop,DropListener dropListener){
        if(canDrop){
            drop.setVisibility(View.VISIBLE);
            this.dropListener=dropListener;
        }else {
            drop.setVisibility(View.GONE);
        }
    }

    public void pause() {
        player.pause();
    }

    public void insertInto(ViewGroup p){
        try {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,20,10,20);
            parent.setLayoutParams(params);
            p.addView(parent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void dropFrom(ViewGroup p) {
        p.removeView(parent);
        if(player.isPlaying())
            player.stop();
    }

    @SuppressLint("SetTextI18n")
    private void prepareNew(String url){
        player.reset();
        try {
            player.setDataSource(url);
            player.setDisplay(holder);
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            prepareNew(url);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    @SuppressLint("SetTextI18n")
    public void update() {
        try {
            seekBar.setProgress((int) (((float) player.getCurrentPosition() / player.getDuration()) * 100));
            text.setText(SuTimeUtil.getTimeString(player.getCurrentPosition()) + "/" + SuTimeUtil.getTimeString(player.getDuration()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getUrl() {
        return url;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public ImageView getControllor() {
        return play;
    }

    public void start() {
        player.start();
    }
}
