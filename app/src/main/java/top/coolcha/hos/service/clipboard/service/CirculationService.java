package top.coolcha.hos.service.clipboard.service;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.List;

import top.coolcha.hos.service.clipboard.dialog.CirculationStatusDialog;
import top.coolcha.hos.service.clipboard.entity.DeviceInfo;
import top.coolcha.hos.service.clipboard.util.BroadcastHelper;
import top.coolcha.hos.service.clipboard.util.Circulation;
import top.coolcha.hos.service.clipboard.util.Constant;
import top.coolcha.hos.service.clipboard.util.api.HOSApiManager;
import top.coolcha.hos.service.clipboard.util.api.HOSWifiManager;
import top.coolcha.hos.service.clipboard.util.base.BaseService;

/**
 * 流转服务
 * @author byk
 * @date 2023/8/12
 */
public class CirculationService extends BaseService {
    /**
     * 获取到了新的待流转消息
     */
    private static final int MSG_NEW_MESSAGE = 1;
    /**
     * 搜索在线设备
     */
    private static final int MSG_FIND_DEVICE = 2;
    /**
     * 查找结束
     */
    private static final int MSG_FIND_FINISH = 3;
    /**
     * 查找成功
     */
    private static final int MSG_FIND_SUCCESS = 4;
    /**
     * 查找失败
     * 原因：1、WiFi未打开
     *      2、WiFi未连接
     *      3、WiFi未获取到ip
     *      4、同一WiFi下未找到其他在线设备
     */
    private static final int MSG_FIND_ERROR = 5;

    private CirculationStatusDialog dialog;
    private String circulationData = "";
    /**
     * 流转消息处理
     */
    private final Handler circulationHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            logger.d("handleMessage: " + what);
            if (what == MSG_NEW_MESSAGE) {
                removeMessages(MSG_FIND_DEVICE);
                dialog.waiting();
                // 1000ms后判断是否内容改变，如果未改变显示设备选择界面
                sendEmptyMessageDelayed(MSG_FIND_DEVICE, 1000);
            } else if (what == MSG_FIND_DEVICE) {
                // 发送广播消息查看当前在线设备
                Circulation.INSTANCE.setText(circulationData);
                Circulation.INSTANCE.clearDevice();
                new FindDevicesThread().start();
            } else if (what == MSG_FIND_FINISH) {
                List<DeviceInfo> deviceInfoList = Circulation.INSTANCE.getDeviceInfoList();
                if (deviceInfoList.isEmpty()) {
                    circulationHandler.sendEmptyMessage(MSG_FIND_ERROR);
                } else {
                    circulationHandler.sendEmptyMessage(MSG_FIND_SUCCESS);
                }
            } else if (what == MSG_FIND_SUCCESS) {
                dialog.success();
            } else if (what == MSG_FIND_ERROR) {
                dialog.error();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        logger.d("onCreate");
        dialog = new CirculationStatusDialog(getApplicationContext()){{
            show();
        }};
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }

        if (Circulation.INSTANCE.isSending()) {
            logger.w("is sending");
            return START_STICKY;
        }

        String circulationData = intent.getStringExtra(Constant.KEY_CIRCULATION_DATA);
        if (TextUtils.isEmpty(circulationData)) {
            return START_STICKY;
        }
        if (!this.circulationData.equals(circulationData)) {
            circulationHandler.sendEmptyMessage(MSG_NEW_MESSAGE);
            this.circulationData = circulationData;
        }
        logger.d("circulation: " + this.circulationData);
        return START_STICKY;
    }

    /**
     * 开启
     */
    private class FindDevicesThread extends Thread {
        private final HOSWifiManager wifiManager = HOSApiManager.getWifiManager();
        @Override
        public void run() {
            if (!wifiManager.isWifiEnabled()) {
                circulationHandler.sendEmptyMessage(MSG_FIND_ERROR);
                return;
            }

            String ip = wifiManager.getIp();
            if (TextUtils.isEmpty(ip)) {
                circulationHandler.sendEmptyMessage(MSG_FIND_ERROR);
                return;
            }

            BroadcastHelper.sendFindDevice(getApplicationContext());

            circulationHandler.sendEmptyMessageDelayed(MSG_FIND_FINISH, 1000);
        }
    }
}
