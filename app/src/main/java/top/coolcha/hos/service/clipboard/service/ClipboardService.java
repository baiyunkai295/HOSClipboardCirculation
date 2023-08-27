package top.coolcha.hos.service.clipboard.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.List;

import top.coolcha.hos.service.clipboard.util.BroadcastHelper;
import top.coolcha.hos.service.clipboard.util.Constant;
import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 剪贴板流转服务
 * @author byk
 * @date 2023/6/27
 */
public class ClipboardService extends AccessibilityService {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    private ClipboardManager clipboardManager;

    @Override
    protected void onServiceConnected() {
        logger.d("onServiceConnected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;

        setServiceInfo(info);

        BroadcastHelper.sendCirculation(getApplicationContext(), "");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.d("onStartCommand");
        if (intent == null) {
            return START_STICKY;
        }

        String action = intent.getStringExtra(Constant.KEY_CIRCULATION_ACTION);
        if (TextUtils.isEmpty(action)) {
            return START_STICKY;
        }
        logger.d("onStartCommand: " + action);

        if (BroadcastHelper.ACTION_NEW_CLIPBOARD.equals(action)) {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("clipboard_circulation", intent.getStringExtra("text")));
            Toast.makeText(getApplicationContext(), "接收到消息流转", Toast.LENGTH_SHORT).show();
        }
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int type = event.getEventType();
        logger.d("onAccessibilityEvent type: " + type);
        if (type == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            List<CharSequence> selection = event.getText();
            if (selection == null) {
                return;
            }

            String selected = selection.get(0).toString();
            logger.d("selected: " + selected);
            String realSelected;
            try {
                realSelected = selected.substring(event.getFromIndex(), event.getToIndex());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            logger.d("real selected: " + realSelected);

            if (TextUtils.isEmpty(realSelected)) {
                return;
            }

            BroadcastHelper.sendCirculation(getApplicationContext(), realSelected);
        }
    }

    @Override
    public void onInterrupt() {
        logger.d("onInterrupt");
    }
}
