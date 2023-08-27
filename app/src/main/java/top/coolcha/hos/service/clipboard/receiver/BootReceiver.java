package top.coolcha.hos.service.clipboard.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import top.coolcha.hos.service.clipboard.service.ClipboardService;
import top.coolcha.hos.service.clipboard.util.base.BaseBroadcastReceiver;

public class BootReceiver extends BaseBroadcastReceiver {

    @Override
    protected void receive(Context context, Intent intent) {
        Log.d("BootReceiver", "boot now");
        context.startService(new Intent(context, ClipboardService.class));
    }
}
