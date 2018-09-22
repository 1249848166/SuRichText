package su.com.richtext.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuImageLoader {

    private Context context;
    private static SuImageLoader instance;
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
                String p=obj.path;
                if(imageView.getTag().toString().equals(p)) {
                    imageView.setImageBitmap(bitmap);
                    if (onLoaded != null)
                        onLoaded.onLoaded(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private SuImageLoader(Context context) {
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(1);
        cache = SuLruCacheHelper.getInstance();
    }

    public static SuImageLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (SuImageLoader.class) {
                if (instance == null) {
                    instance = new SuImageLoader(context);
                }
            }
        }
        return instance;
    }

    public interface OnLoaded {
        void onLoaded(Bitmap bitmap);
    }

    @SuppressLint("HandlerLeak")
    public void loadImage(final String path, final ImageView imageView, final OnLoaded onLoaded) {
        try {
            imageView.setTag(path);
            final Bitmap[] bitmap = {null};
            if (cache.get(path) != null) {
                bitmap[0] =cache.get(path);
            }
            if(bitmap[0] !=null){
                ImageObj obj = new ImageObj(imageView, bitmap[0], onLoaded, path);
                Message msg = handler.obtainMessage();
                msg.obj = obj;
                handler.sendMessage(msg);
            }else {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (path.startsWith("http://") || path.startsWith("https://")) {
                                URL u = null;
                                HttpURLConnection conn = null;
                                u = new URL(path);
                                conn = (HttpURLConnection) u.openConnection();
                                conn.setRequestMethod("GET");
                                conn.setReadTimeout(5000);
                                conn.setConnectTimeout(5000);
                                conn.connect();
                                if (conn.getResponseCode() == 200) {
                                    bitmap[0] = BitmapFactory.decodeStream(conn.getInputStream());
                                    bitmap[0] = compressImage(bitmap[0]);
                                    cache.put(path, bitmap[0]);
                                    ImageObj obj = new ImageObj(imageView, bitmap[0], onLoaded, path);
                                    Message msg = handler.obtainMessage();
                                    msg.obj = obj;
                                    handler.sendMessage(msg);
                                }
                            } else {
                                File f = new File(path);
                                if (f.exists()) {
                                    BitmapFactory.Options options = new BitmapFactory.Options();
                                    options.inJustDecodeBounds = true;
                                    BitmapFactory.decodeFile(path, options);
                                    int outWidth = options.outWidth;
                                    int outHeight = options.outHeight;
                                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                                    int w = params != null ? params.width : 0;
                                    int h = params != null ? params.height : 0;
                                    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                                    if (w <= 0) {
                                        w = metrics.widthPixels;
                                    }
                                    if (h <= 0) {
                                        h = metrics.heightPixels;
                                    }
                                    int widthRatio = outWidth / w;
                                    int heightRatio = outHeight / h;
                                    options.inSampleSize = Math.min(widthRatio, heightRatio);
                                    options.inJustDecodeBounds = false;
                                    bitmap[0] = BitmapFactory.decodeFile(path, options);
                                    cache.put(path, bitmap[0]);
                                    ImageObj obj = new ImageObj(imageView, bitmap[0], onLoaded, path);
                                    Message msg = handler.obtainMessage();
                                    msg.obj = obj;
                                    handler.sendMessage(msg);
                                } else {
                                    System.out.println("文件不存在");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap compressImage(Bitmap image) {
        Bitmap bitmap = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int options = 100;
            while (options > 10 && baos.toByteArray().length / 1024 > 100) {
                baos.reset();
                options -= 10;
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            }
            ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
            bitmap = BitmapFactory.decodeStream(isBm, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
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
