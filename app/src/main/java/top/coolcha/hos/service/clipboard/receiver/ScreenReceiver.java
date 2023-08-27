package top.coolcha.hos.service.clipboard.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import top.coolcha.hos.service.clipboard.service.CirculationService;
import top.coolcha.hos.service.clipboard.util.base.BaseBroadcastReceiver;

/**
 * 屏幕显示关闭广播
 * @author byk
 * @date 2023/4/16
 */
public class ScreenReceiver extends BaseBroadcastReceiver {

    @Override
    public void receive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("ScreenReceiver", "invoke receiver, action: " + action);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            context.startService(new Intent(context, CirculationService.class));
        }
    }
}