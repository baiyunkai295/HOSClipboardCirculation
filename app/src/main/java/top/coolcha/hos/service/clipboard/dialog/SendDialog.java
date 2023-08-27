package top.coolcha.hos.service.clipboard.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import top.coolcha.hos.service.clipboard.databinding.DialogSendBinding;
import top.coolcha.hos.service.clipboard.entity.DeviceInfo;
import top.coolcha.hos.service.clipboard.util.BroadcastHelper;
import top.coolcha.hos.service.clipboard.util.Circulation;
import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 消息发送弹窗
 * @author byk
 * @date 2023/8/26
 */
public class SendDialog {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    private final Context context;
    private DialogSendBinding binding;
    private View mask;
    private final WindowManager windowManager;
    private List<DeviceInfo> deviceInfoList;

    public SendDialog(@NonNull Context context) {
        this.context = context;
        this.binding = DialogSendBinding.inflate(LayoutInflater.from(context), null, false);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        deviceInfoList = Circulation.INSTANCE.getDeviceInfoList();
        binding.etDialogSendEdit.setText(Circulation.INSTANCE.getText());

        // 发送按钮点击
        binding.btnDialogSend.setOnClickListener(v -> {
            binding.tvDialogSendToast.setVisibility(View.GONE);
            String send = binding.etDialogSendEdit.getText().toString();
            if (TextUtils.isEmpty(send)) {
                binding.tvDialogSendToast.setVisibility(View.VISIBLE);
                binding.tvDialogSendToast.setText("传入内容不得为空");
                return;
            }
            int childCount = binding.llDialogSend.getChildCount();
            List<DeviceInfo> sendList = new ArrayList<>();
            for (int i = 0; i < childCount; i++) {
                CheckBox childAt = (CheckBox) binding.llDialogSend.getChildAt(i);
                if (childAt.isChecked()) {
                    sendList.add(deviceInfoList.get(i));

                    DeviceInfo deviceInfo = deviceInfoList.get(i);
                    BroadcastHelper.sendText(context, deviceInfo.getIp(), send);
                }
            }
            if (sendList.isEmpty()) {
                binding.tvDialogSendToast.setVisibility(View.VISIBLE);
                binding.tvDialogSendToast.setText("目标设备不得为空");
                return;
            }
            logger.d("send msg: " + send + " to: " + Arrays.toString(sendList.toArray()));
            dismiss();
        });
    }

    public void show() {
        Circulation.INSTANCE.setSending(true);
        showMask();
        windowManager.addView(binding.getRoot(), getLayoutParams());

        long delay = deviceInfoList.size() == 0 ? 1000 : 0;
        binding.llDialogSend.postDelayed(() -> {
            deviceInfoList = Circulation.INSTANCE.getDeviceInfoList();

            // 动态加载设备列表
            binding.llDialogSend.removeAllViews();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, context.getResources().getDisplayMetrics())
            );
            ViewGroup.LayoutParams svLayout = binding.svDialogSend.getLayoutParams();
            svLayout.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40 * Math.min(deviceInfoList.size(), 3), context.getResources().getDisplayMetrics());
            binding.svDialogSend.setLayoutParams(svLayout);
            for (DeviceInfo deviceInfo : deviceInfoList) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setLayoutParams(layoutParams);
                String name = deviceInfo.getName();
                if (TextUtils.isEmpty(name)) {
                    name = deviceInfo.getIp();
                }
                checkBox.setText(name);

                binding.llDialogSend.addView(checkBox, 0);
            }
        }, delay);
    }

    public void dismiss() {
        windowManager.removeView(binding.getRoot());
        windowManager.removeView(mask);
        binding = null;
        deviceInfoList.clear();
        Circulation.INSTANCE.setSending(false);
    }

    /**
     * 绘制阴影
     */
    private void showMask() {
        mask = new View(context);
        mask.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mask.setBackgroundColor(Color.TRANSPARENT);
        mask.setOnClickListener(v -> dismiss());
        WindowManager.LayoutParams maskLayoutParams = getLayoutParams();
        maskLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        maskLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        windowManager.addView(mask, maskLayoutParams);
    }


    /**
     * 获取悬浮窗位置
     * @return layout params
     */
    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();

        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.dimAmount = 0.5f;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.gravity = Gravity.CENTER;

        return layoutParams;
    }
}