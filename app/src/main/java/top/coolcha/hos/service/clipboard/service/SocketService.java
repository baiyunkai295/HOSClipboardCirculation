package top.coolcha.hos.service.clipboard.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import top.coolcha.hos.service.clipboard.entity.BroadcastPacket;
import top.coolcha.hos.service.clipboard.entity.DeviceInfo;
import top.coolcha.hos.service.clipboard.util.BroadcastHelper;
import top.coolcha.hos.service.clipboard.util.Circulation;
import top.coolcha.hos.service.clipboard.util.Constant;
import top.coolcha.hos.service.clipboard.util.HOSLogger;
import top.coolcha.hos.service.clipboard.util.api.HOSDeviceManager;
import top.coolcha.hos.service.clipboard.util.api.HOSWifiManager;

/**
 * 连接服务
 * @author byk
 * @date 2023/8/14
 */
public class SocketService extends Service {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SocketHandler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread socketHandler = new HandlerThread("socket_handler");
        socketHandler.start();
        handler = new SocketHandler(socketHandler.getLooper());
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

        if (BroadcastHelper.ACTION_FIND_DEVICE.equals(action)) {
            Message message = new Message();
            message.what = SocketHandler.MSG_SEND_BROADCAST_FIND;
            handler.sendMessage(message);
        } else if (BroadcastHelper.ACTION_SEND_TEXT.equals(action)) {
            Message message = new Message();
            message.what = SocketHandler.MSG_SEND_CONNECT_TEXT;
            Bundle bundle = new Bundle();
            bundle.putString("ip", intent.getStringExtra("ip"));
            bundle.putString("text", intent.getStringExtra("text"));
            message.setData(bundle);
            handler.sendMessage(message);
        }

        return START_STICKY;
    }

    class SocketHandler extends Handler {
        private final HOSLogger logger = HOSLogger.getLogger(getClass());

        /**
         * 开启广播包监听
         */
        static final int MSG_LISTEN_BROADCAST = 1;
        /**
         * 开启tcp连接监听
         */
        static final int MSG_LISTEN_CONNECT = 2;
        /**
         * 发送广播发现设备
         */
        static final int MSG_SEND_BROADCAST_FIND = 3;
        /**
         * 发送广播回复发现设备
         */
        static final int MSG_SEND_BROADCAST_REGISTER = 4;
        /**
         * 发送连接文本
         */
        static final int MSG_SEND_CONNECT_TEXT = 5;

        private ListenBroadcastThread listenBroadcastThread;
        private ListenConnectThread listenConnectThread;
        public SocketHandler(@NonNull Looper looper) {
            super(looper);
            sendEmptyMessage(MSG_LISTEN_BROADCAST);
            sendEmptyMessage(MSG_LISTEN_CONNECT);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            logger.d("handle message: " + what);

            if (what == MSG_LISTEN_BROADCAST) {
                if (listenBroadcastThread == null || !listenBroadcastThread.isAlive()) {
                    if (listenBroadcastThread != null) {
                        listenBroadcastThread.shutdown();
                    }
                    listenBroadcastThread = new ListenBroadcastThread();
                    listenBroadcastThread.start();
                }
            } else if (what == MSG_LISTEN_CONNECT) {
                if (listenConnectThread == null || !listenConnectThread.isAlive()) {
                    if (listenConnectThread != null) {
                        listenConnectThread.shutdown();
                    }
                    listenConnectThread = new ListenConnectThread();
                    listenConnectThread.start();
                }
            }  else if (what == MSG_SEND_BROADCAST_FIND) {
                BroadcastPacket packet = new BroadcastPacket();
                packet.setAction(BroadcastPacket.ACTION_FIND_DEVICE);
                HOSWifiManager wifiManager = HOSWifiManager.INSTANCE;
                packet.setIp(wifiManager.getIp());
                try {
                    DatagramPacket datagramPacket = packet.getDatagramPacket();
                    DatagramSocket datagramSocket = new DatagramSocket();
                    datagramSocket.setBroadcast(true);
                    datagramSocket.setReuseAddress(true);
                    datagramSocket.send(datagramPacket);
                    datagramSocket.close();
                    logger.d("find devices");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (what == MSG_SEND_BROADCAST_REGISTER) {
                BroadcastPacket packet = new BroadcastPacket();
                packet.setAction(BroadcastPacket.ACTION_REGISTER_DEVICE);
                HOSWifiManager wifiManager = HOSWifiManager.INSTANCE;
                String ip = wifiManager.getIp();
                packet.setData(new String[] {
                        String.valueOf(wifiManager.isWifiEnabled()),
                        ip,
                        "false",
                        "-1",
                        HOSDeviceManager.INSTANCE.getDeviceName()
                });
                packet.setIp(ip);
                try {
                    DatagramPacket datagramPacket = packet.getDatagramPacket();
                    DatagramSocket datagramSocket = new DatagramSocket();
                    datagramSocket.setBroadcast(true);
                    datagramSocket.setReuseAddress(true);
                    datagramSocket.send(datagramPacket);
                    datagramSocket.close();
                    logger.d("reply devices");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (what == MSG_SEND_CONNECT_TEXT) {
                try {
                    Bundle data = msg.getData();
                    String ip = data.getString("ip", "");
                    if (TextUtils.isEmpty(ip)) {
                        return;
                    }
                    String text = data.getString("text", "");
                    if (TextUtils.isEmpty(text)) {
                        return;
                    }

                    Socket socket = new Socket();
                    socket.setSoTimeout(1000);
                    socket.connect(new InetSocketAddress(ip, 23430));

                    if (socket.isConnected()) {
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write(Constant.SOCKET_ACTION_SEND_TEXT);
                        outputStream.flush();

                        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                        outputStream.close();
                    }
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 广播包监听线程
     */
    class ListenBroadcastThread extends Thread {
        private final HOSLogger logger = HOSLogger.getLogger(getClass());

        private boolean running = true;
        @Override
        public void run() {
            super.run();
            logger.d("start listen thread");

            byte[] data = new byte[1024];
            DatagramPacket datagramPacket = new DatagramPacket(data, 1024);
            try {
                DatagramSocket datagramSocket = new DatagramSocket(BroadcastPacket.PACKAGE_BROADCAST_PORT);
                while (running) {
                    try {
                        datagramSocket.receive(datagramPacket);
                    } catch (IOException e) {
                        continue;
                    }
                    BroadcastPacket packet = BroadcastPacket.analysis(datagramPacket.getData());
                    if (packet == null) {
                        continue;
                    }

                    if (BroadcastPacket.ACTION_FIND_DEVICE.equals(packet.getAction())) {
                        handler.sendEmptyMessage(SocketHandler.MSG_SEND_BROADCAST_REGISTER);
                    } else if (BroadcastPacket.ACTION_REGISTER_DEVICE.equals(packet.getAction())) {
                        DeviceInfo deviceInfo = new DeviceInfo(packet.getData());
                        Circulation.INSTANCE.addDevice(deviceInfo);
                    }

                    logger.d("listen data: " + packet);
                }
            } catch (IOException e) {
                logger.e("listen socket error: " + e.getMessage());
                e.printStackTrace();
            }

            if (running) {
                handler.sendEmptyMessage(SocketHandler.MSG_LISTEN_BROADCAST);
            }
        }

        void shutdown() {
            running = false;
        }
    }

    /**
     * tcp连接监听线程
     */
    class ListenConnectThread extends Thread {
        private final HOSLogger logger = HOSLogger.getLogger(getClass());

        private boolean running = true;
        @Override
        public void run() {
            super.run();
            logger.d("start listen thread");

            try {
                ServerSocket serverSocket = new ServerSocket(23430);
                while (running) {
                    Socket accept = serverSocket.accept();

                    InputStream inputStream = accept.getInputStream();
                    byte[] action = new byte[1];
                    int read = inputStream.read(action);
                    if (read != -1) {
                        switch (action[0]) {
                            case Constant.SOCKET_ACTION_SEND_TEXT:
                                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                String line;
                                StringBuilder data = new StringBuilder();
                                while ((line = reader.readLine()) != null) {
                                    data.append(line).append("\n");
                                }
                                if (data.length() != 0) {
                                    data.deleteCharAt(data.length() - 1);
                                }
                                logger.d("get from: " + accept.getInetAddress() + ": " + data);
                                reader.close();

                                BroadcastHelper.sendNewClipboard(getApplicationContext(), data.toString());
                                break;
                            default:
                                inputStream.close();
                                break;
                        }
                    } else {
                        inputStream.close();
                    }
                    accept.close();
                }
            } catch (IOException e) {
                logger.e("listen socket error: " + e.getMessage());
                e.printStackTrace();
            }

            if (running) {
                handler.sendEmptyMessage(SocketHandler.MSG_LISTEN_CONNECT);
            }
        }

        void shutdown() {
            running = false;
        }
    }
}
