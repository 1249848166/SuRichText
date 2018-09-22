package su.com.surichtext.model;

import cn.bmob.v3.BmobObject;

public class Content extends BmobObject {

    String title;
    String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
