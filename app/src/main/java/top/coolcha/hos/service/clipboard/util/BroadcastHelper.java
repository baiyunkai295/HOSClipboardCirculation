package top.coolcha.hos.service.clipboard.util;

import android.content.Context;
import android.content.Intent;

import top.coolcha.hos.service.clipboard.service.CirculationService;
import top.coolcha.hos.service.clipboard.service.ClipboardService;
import top.coolcha.hos.service.clipboard.service.SocketService;

/**
 * 广播发送帮助类
 * @author byk
 * @date 2023/8/15
 */
public class BroadcastHelper {
    private static final HOSLogger logger = HOSLogger.getLogger(BroadcastHelper.class);

    /**
     * 发现设备
     */
    public static final String ACTION_FIND_DEVICE = "find_device";
    public static final String ACTION_SEND_TEXT = "send_text";
    public static final String ACTION_NEW_CLIPBOARD = "new_clipboard";


    public static void sendCirculation(Context context, String data) {
        Intent circulationIntent = new Intent(context, CirculationService.class);
        circulationIntent.putExtra(Constant.KEY_CIRCULATION_DATA, data);
        context.startService(circulationIntent);
    }

    /**
     * 请求服务开启发现设备
     * @param context 上下文
     */
    public static void sendFindDevice(Context context) {
        logger.d("invoke find device");
        Intent intent = new Intent(context, SocketService.class);
        intent.putExtra(Constant.KEY_CIRCULATION_ACTION, ACTION_FIND_DEVICE);
        context.startService(intent);
    }

    public static void sendText(Context context, String ip, String text) {
        logger.d("invoke send text");
        Intent intent = new Intent(context, SocketService.class);
        intent.putExtra(Constant.KEY_CIRCULATION_ACTION, ACTION_SEND_TEXT);
        intent.putExtra("ip", ip);
        intent.putExtra("text", text);
        context.startService(intent);
    }

    public static void sendNewClipboard(Context context, String text) {
        logger.d("invoke send new clipboard");
        Intent intent = new Intent(context, ClipboardService.class);
        intent.putExtra(Constant.KEY_CIRCULATION_ACTION, ACTION_NEW_CLIPBOARD);
        intent.putExtra("text", text);
        context.startService(intent);
    }
}