package weaver.micro.devkit.core;

import static weaver.micro.devkit.core.CacheBase.EMPTY;
import static weaver.micro.devkit.core.CacheBase.LINE_SEPARATOR;

/**
 * java语言完全通用的接口
 *
 * @author ruan4261
 */
public interface SystemAPI {

    /**
     * 非显式调用异常栈，性能并没有多大提高
     */
    static String getCompleteStackTraceInfo(String title) {
        title = title == null ? EMPTY : title;
        StackTraceElement[] trace = getStackTrace();
        StringBuilder builder = new StringBuilder(title).append(LINE_SEPARATOR);
        for (int i = 2; i < trace.length; i++) {
            StackTraceElement traceElement = trace[i];
            builder.append("\tat ").append(traceElement).append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    /**
     * 获取方法调用栈
     */
    static StackTraceElement[] getStackTrace() {
        return (new Throwable()).getStackTrace();
    }

    static long currentTimestamp() {
        return System.currentTimeMillis();
    }
}
