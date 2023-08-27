package top.coolcha.hos.service.clipboard.entity;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import top.coolcha.hos.service.clipboard.util.api.HOSApiManager;

/**
 * 广播包
 * @author byk
 * @date 2023/8/14
 */
public class BroadcastPacket {
    /**
     * 广播包发送端口及监听端口
     */
    public static final int PACKAGE_BROADCAST_PORT = 23429;

    /**
     * 请求发现广播
     */
    public static final String ACTION_FIND_DEVICE = "find_device";

    /**
     * 发现设备
     * data[0]: WiFi是否打开
     * data[1]: wifi ip
     * data[2]: 蓝牙是否打开
     * data[3]: 蓝牙mac地址
     * data[4]: 自定义名称
     */
    public static final String ACTION_REGISTER_DEVICE = "register_device";

    /**
     * 广播包发送前缀
     */
    private static final String PACKAGE_BROADCAST_PREFIX = "clipboard_circulation";

    /**
     * 广播包发送分割线
     */
    private static final String PACKAGE_BROADCAST_DIVIDE = ";";

    /**
     * 发起的应用
     */
    private final String application = PACKAGE_BROADCAST_PREFIX;

    /**
     * 发送者的ip
     */
    private String ip;
    /**
     * 报文中动作
     */
    private String action;
    /**
     * 数据
     */
    private String[] data;

    @Nullable
    public static BroadcastPacket analysis(byte[] data) throws IOException {
        String origin = new String(data);
        origin = origin.substring(0, origin.indexOf("\0"));

        if (TextUtils.isEmpty(origin)) {
            return null;
        }
        String[] split = origin.split(PACKAGE_BROADCAST_DIVIDE);
        if (split.length < 3) {
            // 非标准包
            return null;
        }

        if (!PACKAGE_BROADCAST_PREFIX.equals(split[0])) {
            // 不是当前应用发送的  不处理
            return null;
        }

        String ip = HOSApiManager.getWifiManager().getIp();
        if (TextUtils.equals(ip, split[1])) {
            // 本机发送的  不处理
            return null;
        }

        BroadcastPacket broadcastPacket = new BroadcastPacket();
        broadcastPacket.setIp(split[1]);
        broadcastPacket.setAction(split[2]);
        String[] strings = new String[split.length - 3];
        System.arraycopy(split, 3, strings, 0, split.length - 3);
        broadcastPacket.setData(strings);

        return broadcastPacket;
    }

    /**
     * 将对象转为包数据
     * @return datagram
     */
    public DatagramPacket getDatagramPacket() throws UnknownHostException {
        StringBuilder datagram = new StringBuilder();
        datagram.append(getApplication())
                .append(PACKAGE_BROADCAST_DIVIDE)
                .append(getIp())
                .append(PACKAGE_BROADCAST_DIVIDE)
                .append(getAction())
                .append(PACKAGE_BROADCAST_DIVIDE);

        if (getData() != null) {
            for (String param : getData()) {
                datagram.append(param).append(PACKAGE_BROADCAST_DIVIDE);
            }
            datagram.deleteCharAt(datagram.length() - 1);
        }
        byte[] data = datagram.append("\0").toString().getBytes(StandardCharsets.UTF_8);

        return new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), PACKAGE_BROADCAST_PORT);
    }

    /**
     * {@link #application}
     */
    public String getApplication() {
        return application;
    }

    /**
     * {@link #ip}
     */
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * {@link #action}
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    /**
     * {@link #data}
     */
    public String[] getData() {
        return data;
    }

    public void setData(String[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BroadcastPacket{" +
                "application='" + application + '\'' +
                ", ip='" + ip + '\'' +
                ", action='" + action + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
