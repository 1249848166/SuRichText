package su.com.surichtext.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MyRecyclerViewDecoration extends RecyclerView.ItemDecoration {

    int space;

    public MyRecyclerViewDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(space,space,space,space);
    }
}
