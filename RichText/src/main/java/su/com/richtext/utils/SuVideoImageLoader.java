package su.com.richtext.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuVideoImageLoader {

    private Context context;
    private static SuVideoImageLoader instance;
    private ExecutorService executorService;
    private SuLruCacheHelper cache;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                ImageObj obj = (ImageObj) msg.obj;
                ImageView imageView = obj.imageView;
                Bitmap bitmap = obj.bitmap;
                OnLoaded onLoaded = obj.onLoaded;
                String p = obj.path;
                if (imageView.getTag().toString().equals(p)) {
                    imageView.setImageBitmap(bitmap);
                    if (onLoaded != null)
                        onLoaded.onLoaded(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private SuVideoImageLoader(Context context) {
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(1);
        cache = SuLruCacheHelper.getInstance();
    }

    public static SuVideoImageLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (SuVideoImageLoader.class) {
                if (instance == null) {
                    instance = new SuVideoImageLoader(context);
                }
            }
        }
        return instance;
    }

    public interface OnLoaded {
        void onLoaded(Bitmap bitmap);
    }

    @SuppressLint("HandlerLeak")
    public void loadVideoImage(final String path, final ImageView imageView, final OnLoaded onLoaded) {
        try {
            imageView.setTag(path);
            final Bitmap[] bitmap = {null};
            if (cache.get(path) != null) {
                bitmap[0] = cache.get(path);
            }
            if (bitmap[0] != null) {
                ImageObj obj = new ImageObj(imageView, bitmap[0], onLoaded, path);
                Message msg = handler.obtainMessage();
                msg.obj = obj;
                handler.sendMessage(msg);
            } else {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File f = new File(path);
                            if (f.exists()) {
                                bitmap[0] = getVideoThumbnail(path);
                                cache.put(path, bitmap[0]);
                                ImageObj obj = new ImageObj(imageView, bitmap[0], onLoaded, path);
                                Message msg = handler.obtainMessage();
                                msg.obj = obj;
                                handler.sendMessage(msg);
                            } else {
                                System.out.println("文件不存在");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getVideoThumbnail(String videoPath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(videoPath);
        return media.getFrameAtTime();
    }

    class ImageObj {
        ImageView imageView;
        Bitmap bitmap;
        OnLoaded onLoaded;
        String path;

        ImageObj(ImageView imageView, Bitmap bitmap, OnLoaded onLoaded, String path) {
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.onLoaded = onLoaded;
            this.path = path;
        }
    }
}
