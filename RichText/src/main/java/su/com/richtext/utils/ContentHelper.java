package su.com.richtext.utils;

public class ContentHelper {

    private static ContentHelper instance;

    public static ContentHelper getInstance(){
        if(instance==null){
            synchronized (ContentHelper.class){
                if(instance==null){
                    instance=new ContentHelper();
                }
            }
        }
        return instance;
    }

    public void loadVideoFiles(){}

    public void loadAudioFiles(){}

    public void loadImageFiles(){}
}
