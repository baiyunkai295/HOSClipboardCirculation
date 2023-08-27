package top.coolcha.hos.service.clipboard.util.api;

import android.content.Context;
import android.net.wifi.WifiInfo;

import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 网络管理器
 * @author byk
 * @date 2023/4/16
 */
public enum HOSWifiManager {

    /** 单例 */
    INSTANCE;

    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    /** 原生manager */
    private android.net.wifi.WifiManager wifiManager;
    public void init(Context context) {
        if (wifiManager == null) {
            logger.d("init wifi manager");
            wifiManager = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
    }

    /**
     * 获取当前ip
     */
    public String getIp() {
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (!wifiManager.isWifiEnabled()) {
            return "";
        }
        if (connectionInfo != null) {
            int ip = connectionInfo.getIpAddress();
            int first = ip >> 24;
            if (first < 0) {
                first = 0xff + first + 1;
            }
            int second = ip >> 16 & 0xff;
            int third = ip >> 8 & 0xff;
            int four = ip & 0xff;

            String ipStr = four + "." + third + "." + second + "." + first;
            if (ipStr.equals("0.0.0.0")) {
                // 为获取到真实的ip
                return "";
            }
            return ipStr;
        }

        return "";
    }

    /**
     * 判断wifi是否打开
     * @return true 已打开
     */
    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }
}
