package top.coolcha.hos.service.clipboard.util;

import android.util.Log;

/**
 * 统一日志打印工具
 * @author byk
 * @date 2023/4/19
 */
public class HOSLogger {

    private final String tag;

    public HOSLogger(Class<?> cls) {
        this.tag = cls.getSimpleName();
    }

    public static HOSLogger getLogger(Class<?> cls) {
        return new HOSLogger(cls);
    }

    public void i(String str) {
        StackTraceElement stackTraceElement = getStackTraceElement();
        String msg = str;
        if (stackTraceElement != null) {
            msg = "[" + stackTraceElement.getMethodName() + "][" + stackTraceElement.getLineNumber() + "]: " + str;
        }
        Log.i(tag, msg);
    }

    public void d(String str) {
        StackTraceElement stackTraceElement = getStackTraceElement();
        String msg = str;
        if (stackTraceElement != null) {
            msg = "[" + stackTraceElement.getMethodName() + "][" + stackTraceElement.getLineNumber() + "]: " + str;
        }
        Log.d(tag, msg);
    }

    public void w(String str) {
        StackTraceElement stackTraceElement = getStackTraceElement();
        String msg = str;
        if (stackTraceElement != null) {
            msg = "[" + stackTraceElement.getMethodName() + "][" + stackTraceElement.getLineNumber() + "]: " + str;
        }
        Log.w(tag, msg);
    }

    public void e(String str) {
        StackTraceElement stackTraceElement = getStackTraceElement();
        String msg = str;
        if (stackTraceElement != null) {
            msg = "[" + stackTraceElement.getMethodName() + "][" + stackTraceElement.getLineNumber() + "]: " + str;
        }
        Log.e(tag, msg);
    }

    private StackTraceElement getStackTraceElement() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.getClassName().equals(this.getClass().getName())) {
                return stackTraceElement;
            }
        }

        return null;
    }
}
