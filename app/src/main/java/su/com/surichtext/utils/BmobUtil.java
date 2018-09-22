package su.com.surichtext.utils;

import java.io.File;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;
import su.com.surichtext.model.Content;

public class BmobUtil {

    public interface UploadFileCallback{
        void onUploadFile(String url);
    }

    public interface UploadContentCallback{
        void onUploadContent(boolean success);
    }

    private static BmobUtil instance;

    public static BmobUtil getInstance(){
        if(instance==null){
            synchronized (BmobUtil.class){
                if(instance==null){
                    instance=new BmobUtil();
                }
            }
        }
        return instance;
    }

    public void uploadFile(String[] paths, final String failUrlOrPath, final UploadFileCallback callback){
        BmobFile.uploadBatch(paths, new UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> list, List<String> list1) {
                if(callback!=null)
                    callback.onUploadFile(list1.get(0));
            }

            @Override
            public void onProgress(int i, int i1, int i2, int i3) {

            }

            @Override
            public void onError(int i, String s) {
                if(callback!=null) {
                    if(failUrlOrPath.startsWith("http://")||failUrlOrPath.startsWith("https://")) {
                        callback.onUploadFile(failUrlOrPath);
                    }else{
                        File file=new File(failUrlOrPath);
                        if(file.exists()){
                            BmobFile.uploadBatch(new String[]{failUrlOrPath}, new UploadBatchListener() {
                                @Override
                                public void onSuccess(List<BmobFile> list, List<String> list1) {
                                    callback.onUploadFile(list1.get(0));
                                }

                                @Override
                                public void onProgress(int i, int i1, int i2, int i3) {

                                }

                                @Override
                                public void onError(int i, String s) {

                                }
                            });
                        }else{
                            callback.onUploadFile("");
                        }
                    }
                }
            }
        });
    }

    public void uploadContent(String title, String contentString, final UploadContentCallback callback){
        Content content=new Content();
        content.setTitle(title);
        content.setContent(contentString);
        content.save(new SaveListener<String>() {
            @Override
            public void done(String s, BmobException e) {
                if(e==null){
                    if(callback!=null)
                        callback.onUploadContent(true);
                }else{
                    if(callback!=null)
                        callback.onUploadContent(false);
                }
            }
        });
    }
}
