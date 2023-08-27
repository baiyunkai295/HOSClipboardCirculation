package top.coolcha.hos.service.clipboard.util.base;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 通用服务
 * @author byk
 * @date 2023/8/26
 */
public class BaseService extends Service {
    protected final HOSLogger logger = HOSLogger.getLogger(getClass());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
