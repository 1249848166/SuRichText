package su.com.richtext.utils;

import android.graphics.Bitmap;
import android.util.LruCache;

public class SuLruCacheHelper {

    private static SuLruCacheHelper instance;
    private LruCache<String,Bitmap> lruCache;

    private SuLruCacheHelper(){
        int maxSize= (int) (Runtime.getRuntime().maxMemory()/8);
        lruCache=new LruCache<String,Bitmap>(maxSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getHeight()*value.getRowBytes();
            }
        };
    }

    public static SuLruCacheHelper getInstance(){
        if(instance==null){
            synchronized (SuLruCacheHelper.class){
                if(instance==null){
                    instance=new SuLruCacheHelper();
                }
            }
        }
        return instance;
    }

    public void put(String path,Bitmap bitmap){
        lruCache.put(path,bitmap);
    }

    public Bitmap get(String path){
        return lruCache.get(path);
    }
}
