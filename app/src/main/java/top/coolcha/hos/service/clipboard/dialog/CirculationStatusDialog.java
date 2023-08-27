package top.coolcha.hos.service.clipboard.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import top.coolcha.hos.service.clipboard.R;
import top.coolcha.hos.service.clipboard.custom.StatusView;
import top.coolcha.hos.service.clipboard.databinding.DialogCirculationStatusBinding;
import top.coolcha.hos.service.clipboard.util.BroadcastHelper;
import top.coolcha.hos.service.clipboard.util.Constant;
import top.coolcha.hos.service.clipboard.util.HOSLogger;
import top.coolcha.hos.service.clipboard.util.api.HOSApiManager;
import top.coolcha.hos.service.clipboard.util.api.HOSParamsManager;

/**
 * 流转消息弹窗
 * @author byk
 * @date 2023/8/13
 */
public class CirculationStatusDialog implements ICirculationStatusDialog {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    /**
     * 弹窗默认偏移方向
     */
    private static final String DEFAULT_SHIFTING = "left";
    /**
     * 弹窗默认距离顶部距离
     */
    private static final String DEFAULT_Y = "0";

    private final WindowManager windowManager;
    private final Context context;
    private DialogCirculationStatusBinding binding;

    /**
     * 是否长按了图标
     * 允许滑动修改位置
     */
    private boolean isLongClick = false;
    private int downX = -1;
    private final int downY = 100;
    private int moveX, moveY;
    /**
     * 弹窗是否位于左侧
     */
    private boolean isLeft = false;

    private WindowManager.LayoutParams layoutParams;
    private final HOSParamsManager paramsManager;

    public CirculationStatusDialog(Context context) {
        this.context = context;
        this.paramsManager = HOSApiManager.getParamsManager();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void show() {
        logger.d("show");
        if (binding == null) {
            binding = DialogCirculationStatusBinding.inflate(LayoutInflater.from(context), null, false);

            binding.ivDialogCirculationOpen.setOnClickListener(v -> {
                logger.d("onclick");
                if (binding.ivDialogCirculationStatus.getStatus() == StatusView.Status.SUCCESS) {
                    binding.ivDialogCirculationStatus.setStatus(StatusView.Status.UNKNOWN);
                } else {
                    BroadcastHelper.sendFindDevice(context);
                }
                new SendDialog(context).show();
            });

            binding.ivDialogCirculationOpen.setOnLongClickListener(v -> {
                // 长按后才允许view被手指拖动
                logger.d("onLongClick");
                isLongClick = true;
                binding.ivDialogCirculationOpen.setBackgroundResource(R.drawable.ic_background_dialog_circle);
                return false;
            });

            binding.ivDialogCirculationOpen.setOnTouchListener((v, event) -> {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (downX == -1) {
                        downX = (int) event.getRawX();
                    }

                    moveX = (int) event.getRawX();
                    moveY = (int) event.getRawY();
                } else if (action == MotionEvent.ACTION_MOVE && isLongClick) {
                    int x = (int) event.getRawX();
                    int y = (int) event.getRawY();

                    if (Math.abs(x - moveX) > 5 || Math.abs(y - moveY) > 5) {
                        WindowManager.LayoutParams layoutParams = getLayoutParams();
                        if (isLeft) {
                            layoutParams.x = moveX - downX;
                        } else {
                            layoutParams.x = downX - moveX;
                        }
                        layoutParams.y = moveY - downY;
                        logger.d("layoutParams, x:" + layoutParams.x + ", y: " + layoutParams.y);

                        moveX = x;
                        moveY = y;

                        windowManager.updateViewLayout(binding.getRoot(), layoutParams);
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (!isLongClick) {
                        return false;
                    }
                    isLongClick = false;
                    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                    int center = displayMetrics.widthPixels / 2;
                    if (moveX < center) {
                        logger.d("left");
                        startValueAnimator(moveX, true);
                    } else {
                        logger.d("right");
                        startValueAnimator(displayMetrics.widthPixels - moveX, false);
                    }
                }
                return false;
            });
        }

        windowManager.addView(binding.getRoot(), getLayoutParams());
    }

    /**
     * 获取悬浮窗位置
     * @return layout params
     */
    private WindowManager.LayoutParams getLayoutParams() {
        if (layoutParams == null) {
            layoutParams = new WindowManager.LayoutParams();

            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.dimAmount = 0f;
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            layoutParams.gravity = Gravity.TOP;
            if (paramsManager.getParam(HOSParamsManager.Type.SHARE, Constant.PARAMS_KEY_DIALOG_SHIFTING, DEFAULT_SHIFTING).equals(DEFAULT_SHIFTING)) {
                isLeft = true;
                layoutParams.gravity = layoutParams.gravity | Gravity.START;
                binding.ivDialogCirculationOpen.setBackgroundResource(R.drawable.ic_background_dialog_edge_left);
                binding.llDialogCirculation.setReverse(false);
            } else {
                isLeft = false;
                layoutParams.gravity = layoutParams.gravity | Gravity.END;
                binding.ivDialogCirculationOpen.setBackgroundResource(R.drawable.ic_background_dialog_edge_right);
                binding.llDialogCirculation.setReverse(true);
            }
            layoutParams.x = 0;

            layoutParams.y = Integer.parseInt(paramsManager.getParam(HOSParamsManager.Type.SHARE, Constant.PARAMS_KEY_DIALOG_Y, DEFAULT_Y)) - downY;
        }

        return layoutParams;
    }

    /**
     * view移动松手后自动贴边动画
     * @param start  开始的x坐标
     * @param isLeft 是否向左移动
     */
    private void startValueAnimator(int start, boolean isLeft) {
        if (isLeft != this.isLeft) {
            downX = -1;
        }
        this.isLeft = isLeft;
        if (isLeft) {
            getLayoutParams().gravity = Gravity.TOP | Gravity.START;
        } else {
            getLayoutParams().gravity = Gravity.TOP | Gravity.END;
        }

        ValueAnimator animator = ValueAnimator.ofInt(start, 0);
        animator.setDuration(500);

        animator.addUpdateListener(animation -> {
            WindowManager.LayoutParams layoutParams = getLayoutParams();
            layoutParams.x = (int) animation.getAnimatedValue();

            windowManager.updateViewLayout(binding.getRoot(), layoutParams);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                binding.ivDialogCirculationOpen.setFocusable(false);
                binding.ivDialogCirculationOpen.setClickable(false);
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                binding.ivDialogCirculationOpen.setFocusable(true);
                binding.ivDialogCirculationOpen.setClickable(true);

                if (isLeft) {
                    binding.ivDialogCirculationOpen.setBackgroundResource(R.drawable.ic_background_dialog_edge_left);
                    binding.llDialogCirculation.setReverse(false);
                    paramsManager.setParam(HOSParamsManager.Type.SHARE, Constant.PARAMS_KEY_DIALOG_SHIFTING, DEFAULT_SHIFTING);
                } else {
                    binding.ivDialogCirculationOpen.setBackgroundResource(R.drawable.ic_background_dialog_edge_right);
                    binding.llDialogCirculation.setReverse(true);
                    paramsManager.setParam(HOSParamsManager.Type.SHARE, Constant.PARAMS_KEY_DIALOG_SHIFTING, "right");
                }
                moveX = 0;
                paramsManager.setParam(HOSParamsManager.Type.SHARE, Constant.PARAMS_KEY_DIALOG_Y, String.valueOf(moveY));
            }
        });

        animator.start();
    }

    @Override
    public void waiting() {
        binding.ivDialogCirculationStatus.setStatus(StatusView.Status.WAIT);
    }

    @Override
    public void success() {
        binding.ivDialogCirculationStatus.setStatus(StatusView.Status.SUCCESS);
    }

    @Override
    public void error() {
        binding.ivDialogCirculationStatus.setStatus(StatusView.Status.ERROR);
    }
}
