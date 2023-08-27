package top.coolcha.hos.service.clipboard.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import top.coolcha.hos.service.clipboard.entity.DeviceInfo;

/**
 * 静态公共数据
 * @author byk
 * @date 2023/8/15
 */
public enum Circulation {
    /**
     * 单例模式
     */
    INSTANCE;

    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    /**
     * 当前可连接的设备列表
     */
    private final List<DeviceInfo> deviceInfoList = new CopyOnWriteArrayList<>();
    public void addDevice(DeviceInfo deviceInfo) {
        logger.d("find device: " + deviceInfo);
        deviceInfoList.add(deviceInfo);
    }
    public void clearDevice() {
        deviceInfoList.clear();
    }
    public List<DeviceInfo> getDeviceInfoList() {
        return deviceInfoList;
    }

    /**
     * 是否正在发送，设备弹窗弹出后不再监听
     */
    private boolean isSending = false;
    public boolean isSending() {
        return isSending;
    }
    public void setSending(boolean sending) {
        isSending = sending;
    }

    /**
     * 流转内容
     */
    private String text = "";
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
}