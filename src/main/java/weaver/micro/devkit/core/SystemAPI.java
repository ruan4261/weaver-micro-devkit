package weaver.micro.devkit.core;

/**
 * java语言完全通用的接口
 *
 * @author ruan4261
 */
public interface SystemAPI {

    /**
     * 非显式异常栈，性能并没有多大提高
     */
    static String getCompleteStackTraceInfo(String title) {
        StackTraceElement[] trace = getStackTrace();
        StringBuilder builder = new StringBuilder(title).append(CacheBase.EMPTY);
        for (int i = 2; i < trace.length; i++) {
            StackTraceElement traceElement = trace[i];
            builder.append("\tat ").append(traceElement).append(CacheBase.EMPTY);
        }
        return builder.toString();
    }

    /**
     * 获取方法调用栈
     */
    static StackTraceElement[] getStackTrace() {
        return (new Throwable()).getStackTrace();
    }
}
