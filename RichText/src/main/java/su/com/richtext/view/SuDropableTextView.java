package su.com.richtext.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class SuDropableTextView extends TextView {

    public DrawableRightClickListener drawableRightClickListener;

    public SuDropableTextView(Context context) {
        super(context);
    }

    public SuDropableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SuDropableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public DrawableRightClickListener getDrawableRightClick() {
        return drawableRightClickListener;
    }

    public void setDrawableRightClick(DrawableRightClickListener drawableRightClickListener) {
        this.drawableRightClickListener = drawableRightClickListener;
    }

    //为了方便,直接写了一个内部类的接口
    public interface DrawableRightClickListener {
        void onDrawableRightClickListener(View view);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (drawableRightClickListener != null) {
                    Drawable rightDrawable = getCompoundDrawables()[2];
                    if (rightDrawable != null && event.getRawX() >= (getRight() - rightDrawable.getBounds().width())) {
                        drawableRightClickListener.onDrawableRightClickListener(this);
                    }
                    return false;
                }

                break;

        }
        return super.onTouchEvent(event);

    }
}
