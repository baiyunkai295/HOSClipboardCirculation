package top.coolcha.hos.service.clipboard.util.api;

import android.content.Context;
import android.os.Build;

/**
 * 设备管理
 * @author byk
 * @date 2023/8/13
 */
public enum HOSDeviceManager {

    /**
     * 单例模式
     */
    INSTANCE;

    /**
     * 当前设备名称
     * 可自定义，原始值为厂商+型号
     */
    private static final String KEY_DEVICE_NAME = "key_device_name";

    private HOSParamsManager paramsManager;
    public void init(Context context) {
        paramsManager = HOSApiManager.getParamsManager();
        paramsManager.init(context);
    }

    public String getDeviceName() {
        return paramsManager.getParam(HOSParamsManager.Type.SHARE, KEY_DEVICE_NAME, Build.MANUFACTURER + " " + Build.MODEL);
    }
}