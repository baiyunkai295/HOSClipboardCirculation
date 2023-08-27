package top.coolcha.hos.service.clipboard.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 状态显示view
 * @author byk
 * @date 2023/8/13
 */
public class StatusView extends View {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    private Status status = Status.UNKNOWN;
    private int background;
    private final Paint paint;
    private ValueAnimator animator;

    public StatusView(Context context, AttributeSet set) {
        super(context, set);

        paint = new Paint() {{
            setStyle(Style.FILL);
            setAntiAlias(true);
        }};
    }

    /**
     * {@link #status}
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 切换当前显示状态
     * @param status 状态
     */
    public void setStatus(Status status) {
        if (this.status == status) {
            return;
        }
        logger.d("change status: " + status);
        this.status = status;
        if (animator != null) {
            animator.cancel();
        }
        if (status != Status.UNKNOWN) {
            setVisibility(VISIBLE);
        }
        switch (status) {
            case UNKNOWN:
                // 防止悬浮窗位置闪烁
                setVisibility(INVISIBLE);
                break;
            case WAIT:
                animator = ValueAnimator.ofArgb(Color.parseColor("#FFCC80"), Color.parseColor("#ff9900"));

                animator.addUpdateListener(animation -> {
                    background = (int) animation.getAnimatedValue();
                    invalidate();
                });

                animator.setRepeatCount(-1);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setInterpolator(new LinearInterpolator());
                animator.setDuration(600);
                animator.start();
                break;
            case SUCCESS:
                background = Color.parseColor("#ddff21");
                break;
            case ERROR:
                background = Color.parseColor("#FF5858");
                postDelayed(() -> {
                    if (this.status == Status.ERROR) {
                        setStatus(Status.UNKNOWN);
                    }
                }, 2000);
                break;
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas == null) {
            return;
        }

        if (status == Status.UNKNOWN) {
            setVisibility(INVISIBLE);
            return;
        }

        paint.setColor(background);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, getWidth() / 2f, paint);
    }

    public enum Status {
        /**
         * 不显示，隐藏
         */
        UNKNOWN,
        /**
         * 黄光闪烁
         */
        WAIT,
        /**
         * 绿色
         */
        SUCCESS,
        /**
         * 红色
         */
        ERROR
    }
}
