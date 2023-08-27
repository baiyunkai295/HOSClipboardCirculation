package top.coolcha.hos.service.clipboard.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 可逆向绘制的线性布局
 * @author byk
 * @date 2023/8/13
 */
public class ReverseLinearLayout extends LinearLayout {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());
    /**
     * 是否逆向绘制
     * 默认为从左到右，从上到下
     */
    private boolean isReverse = false;

    public ReverseLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View getChildAt(int index) {
        if (isReverse) {
            return super.getChildAt(getChildCount() - index - 1);
        }
        return super.getChildAt(index);
    }

    public void setReverse(boolean reverse) {
        if (reverse != isReverse) {
            logger.d("change reverse: " + reverse);
            isReverse = reverse;
            invalidate();
        }
    }

    /**
     * {@link #isReverse}
     */
    public boolean isReverse() {
        return isReverse;
    }
}
