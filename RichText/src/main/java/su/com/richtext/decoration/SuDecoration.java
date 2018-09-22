package su.com.richtext.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SuDecoration extends RecyclerView.ItemDecoration {

    int space;
    int col;

    public SuDecoration(int space, int col) {
        this.space = space;
        this.col=col;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position=parent.getChildAdapterPosition(view);
        if(position%col==col-1){
            outRect.set(space,space,space,0);
        }else{
            outRect.set(space,space,0,0);
        }
    }
}
