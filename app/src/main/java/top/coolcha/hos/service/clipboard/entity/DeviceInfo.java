package top.coolcha.hos.service.clipboard.entity;

import java.util.Objects;

/**
 * 设备信息
 * @author byk
 * @date 2023/8/15
 */
public class DeviceInfo {
    /**
     * WiFi是否打开
     */
    private final boolean isWifiEnabled;
    /**
     * WiFi下获取的ip
     */
    private final String ip;
    /**
     * 蓝牙是否打开
     */
    private final boolean isBluetoothEnabled;
    /**
     * 蓝牙mac信息
     */
    private final String mac;
    /**
     * 自定义名称
     */
    private final String name;

    public DeviceInfo(String[] data) {
        isWifiEnabled = Boolean.parseBoolean(data[0]);
        ip = data[1];
        isBluetoothEnabled = Boolean.parseBoolean(data[2]);
        mac = data[3];
        name = data[4];
    }

    /**
     * {@link #isWifiEnabled}
     */
    public boolean isWifiEnabled() {
        return isWifiEnabled;
    }

    /**
     * {@link #ip}
     */
    public String getIp() {
        return ip;
    }

    /**
     * {@link #isBluetoothEnabled}
     */
    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled;
    }


    /**
     * {@link #mac}
     */
    public String getMac() {
        return mac;
    }


    /**
     * {@link #name}
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceInfo that = (DeviceInfo) o;
        return Objects.equals(ip, that.ip) && Objects.equals(mac, that.mac);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, mac);
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "isWifiEnabled=" + isWifiEnabled +
                ", ip='" + ip + '\'' +
                ", isBluetoothEnabled=" + isBluetoothEnabled +
                ", mac='" + mac + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}