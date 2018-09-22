package su.com.richtext.callback;

import java.util.List;

public interface SuSearchCallback<T> {
    void onSearch(List<T> datas);
}
