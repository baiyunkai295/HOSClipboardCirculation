package top.coolcha.hos.service.clipboard;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import top.coolcha.hos.service.clipboard.receiver.ScreenReceiver;
import top.coolcha.hos.service.clipboard.service.SocketService;
import top.coolcha.hos.service.clipboard.util.HOSLogger;
import top.coolcha.hos.service.clipboard.util.api.HOSApiManager;

/**
 * 剪贴板流转
 * @author byk
 * @date 2023/8/13
 */
public class ClipboardCirculation extends Application {
    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    @Override
    public void onCreate() {
        super.onCreate();
        logger.d("onCreate");
        long start = System.currentTimeMillis();

        HOSApiManager.init(getApplicationContext());

        registerReceiver(new ScreenReceiver(), new IntentFilter() {{
            addAction(Intent.ACTION_SCREEN_ON);
        }});
        startService(new Intent(this, SocketService.class));

        logger.d("onCreate use: " + (System.currentTimeMillis() - start) + "ms");
    }
}
