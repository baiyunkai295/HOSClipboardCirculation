package top.coolcha.hos.service.clipboard.util;

/**
 * 静态变量
 * @author byk
 * @date 2023/8/12
 */
public final class Constant {
    /**
     * 传递给流转服务的文本内容
     */
    public static final String KEY_CIRCULATION_DATA = "circulation_data";
    /**
     * 广播发送的动作
     */
    public static final String KEY_CIRCULATION_ACTION = "circulation_action";

    /**
     * 数据参数
     * 弹窗偏移位置
     */
    public static final String PARAMS_KEY_DIALOG_SHIFTING = "params_key_dialog_shifting";
    /**
     * 数据参数
     * 弹窗距离顶部距离
     */
    public static final String PARAMS_KEY_DIALOG_Y = "params_key_dialog_y";

    /**
     * socket连接动作
     * 请求发送消息
     */
    public static final byte SOCKET_ACTION_SEND_TEXT = 'a';
}
