package su.com.richtext.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import su.com.richtext.R;
import su.com.richtext.utils.SuTimeUtil;

public class SuAudioPlayer {

    private MediaPlayer player;
    private boolean isPrepared;
    private String url;
    private Callback callback;
    int screenWidth;
    ImageView play;
    SeekBar seekbar;
    TextView duration;
    ImageView drop;
    LinearLayout layout;
    boolean preIsPlaying=false;
    int progress=0;

    public boolean isPreIsPlaying() {
        return preIsPlaying;
    }

    public void setPreIsPlaying(boolean preIsPlaying) {
        this.preIsPlaying = preIsPlaying;
    }

    public void release() {
        player.stop();
        player.release();
        player=null;
    }

    public interface Callback {
        void onCompletion(MediaPlayer player);
        void onError(String msg);
        void onPrepared();
        void onUpdate(float progress);
        void onStateChange(boolean isPlaying);
    }

    public SuAudioPlayer(Context context, String url, @Nullable Callback callback) {
        this.url = url;
        this.callback = callback;
        try {
            init(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void init(Context context) throws IOException {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.audio, null);
        drop=layout.findViewById(R.id.drop);
        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dropListener!=null)
                    dropListener.drop();
            }
        });
        TextView name = layout.findViewById(R.id.folderName);
        name.setText(url.substring(url.lastIndexOf("/") + 1, url.length()));
        duration = layout.findViewById(R.id.duration);
        play = layout.findViewById(R.id.play);
        seekbar = layout.findViewById(R.id.seekbar);
        seekbar.setMax(100);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    player.seekTo((int) ((float) seekBar.getProgress() / 100 * player.getDuration()));
                    duration.setText(SuTimeUtil.getTimeString(player.getCurrentPosition()) + "/" + SuTimeUtil.getTimeString(player.getDuration()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(context, Uri.parse(url));
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                play.setImageResource(R.drawable.play_black);
                seekbar.setProgress(0);
                if(callback!=null){
                    callback.onCompletion(player);
                }
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                play.setImageResource(R.drawable.play_black);
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        System.out.println("未知错误");
                        if(callback!=null)
                            callback.onError("未知错误");
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        System.out.println("服务器崩溃");
                        if(callback!=null)
                            callback.onError("服务器崩溃");
                        break;
                }
                switch (extra) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        System.out.println("读取错误");
                        if(callback!=null)
                            callback.onError("读取错误");
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        System.out.println("格式错误");
                        if(callback!=null)
                            callback.onError("格式错误");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        System.out.println("类型不支持");
                        if(callback!=null)
                            callback.onError("类型不支持");
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        System.out.println("读取超时");
                        if(callback!=null)
                            callback.onError("读取超时");
                        break;
                }
                return true;
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared=true;
                duration.setText(SuTimeUtil.getTimeString(player.getCurrentPosition()) + "/" + SuTimeUtil.getTimeString(player.getDuration()));
                if(callback!=null)
                    callback.onPrepared();
            }
        });
        player.prepareAsync();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying()){
                    player.pause();
                    play.setImageResource(R.drawable.play_black);
                    if(callback!=null)
                        callback.onStateChange(false);
                }else{
                    player.start();
                    play.setImageResource(R.drawable.pause_black);
                    if(callback!=null)
                        callback.onStateChange(true);
                }
            }
        });
        duration.setText(SuTimeUtil.getTimeString(player.getCurrentPosition()) + "/" + SuTimeUtil.getTimeString(player.getDuration()));
    }

    public interface DropListener{
        void drop();
    }

    private DropListener dropListener;

    public void setCanDrop(boolean canDrop,DropListener dropListener){
        if(canDrop){
            this.dropListener=dropListener;
            drop.setVisibility(View.VISIBLE);
        }else{
            drop.setVisibility(View.GONE);
        }
    }

    public long getDuration() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    @SuppressLint("SetTextI18n")
    public void update() {
        seekbar.setProgress((int) (((float)player.getCurrentPosition()/player.getDuration())*seekbar.getMax()));
        duration.setText(SuTimeUtil.getTimeString(player.getCurrentPosition()) + "/" + SuTimeUtil.getTimeString(player.getDuration()));
        if(callback!=null)
            callback.onUpdate((float)player.getCurrentPosition()/player.getDuration());
    }

    public void pause() {
        player.pause();
        progress=player.getCurrentPosition();
    }

    public void start() {
        player.seekTo(progress);
        player.start();
    }

    public void insertInto(ViewGroup p){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth-20, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,20,10,20);
        layout.setLayoutParams(params);
        p.addView(layout);
    }

    public void dropFrom(ViewGroup p){
        p.removeView(layout);
        if(player.isPlaying())
            player.stop();
    }

    public ImageView getControllor() {
        return play;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public String getUrl() {
        return url;
    }
}
