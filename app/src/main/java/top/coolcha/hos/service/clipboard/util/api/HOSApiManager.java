package top.coolcha.hos.service.clipboard.util.api;

import android.content.Context;

/**
 * 中间件
 * @author byk
 * @date 2023/8/13
 */
public class HOSApiManager {

    public static void init(Context context) {
        HOSWifiManager.INSTANCE.init(context);
        HOSParamsManager.INSTANCE.init(context);
        HOSDeviceManager.INSTANCE.init(context);
    }

    public static HOSWifiManager getWifiManager() {
        return HOSWifiManager.INSTANCE;
    }

    public static HOSParamsManager getParamsManager() {
        return HOSParamsManager.INSTANCE;
    }

    public static HOSDeviceManager getDeviceManager() {
        return HOSDeviceManager.INSTANCE;
    }
}
