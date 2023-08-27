package top.coolcha.hos.service.clipboard.util.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import top.coolcha.hos.service.clipboard.util.HOSLogger;

/**
 * 参数管理
 * @author byk
 * @date 2023/8/13
 */
public enum HOSParamsManager {

    /**
     * 单例模式
     */
    INSTANCE;

    private final HOSLogger logger = HOSLogger.getLogger(getClass());

    private SharedPreferences sharedPreferences;
    private Map<String, String> memoryMap;
    private Method getProp;

    /**
     * 初始化
     */
    public void init(Context context) {
        if (sharedPreferences != null) {
            logger.d("is already init");
            return;

        }
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        memoryMap = new LinkedHashMap<>();
        try {
            @SuppressLint("PrivateApi") Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            getProp = systemProperties.getDeclaredMethod("get", String.class, String.class);
        } catch (Exception e) {
            logger.w("getprop get error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setParam(Type type, String key, String value) {
        switch (type) {
            case SHARE:
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putString(key, value);
                edit.apply();
                break;
            case MEMORY:
                memoryMap.put(key, value);
                break;
            case PROP:
                logger.w("no permissions");
                break;
        }
    }

    public String getParam(Type type, String key, String defVal) {
        String val = defVal;
        switch (type) {
            case SHARE:
                val = sharedPreferences.getString(key, defVal);
                break;
            case MEMORY:
                if (memoryMap.containsKey(key)) {
                    val = memoryMap.getOrDefault(key, defVal);
                }
                break;
            case PROP:
                if (getProp != null) {
                    try {
                        getProp.invoke(null, key, defVal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

        logger.d("get param: " + key + "=" + val);
        return val;
    }

    /**
     * 参数存放位置
     */
    public enum Type {
        /**
         * sp文件中
         */
        SHARE,
        /**
         * 内存中
         */
        MEMORY,
        /**
         * getprop获取
         */
        PROP,
    }
}
