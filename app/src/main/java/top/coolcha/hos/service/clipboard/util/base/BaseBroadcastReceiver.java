package top.coolcha.hos.service.clipboard.util.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 不为空的广播回调
 * @author byk
 * @date 2023/4/16
 */
public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) return;
        if (intent == null) return;

        receive(context, intent);
    }

    protected abstract void receive(Context context, Intent intent);
}
