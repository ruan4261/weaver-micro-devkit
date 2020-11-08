package weaver.micro.devkit;

import java.io.File;

/**
 * 通用接口
 *
 * @author ruan4261
 */
public final class SystemAPI {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");// 系统换行符('\n'或'\r\n'或其他)
    public static final String FILE_SEPARATOR = File.separator;// 文件系统名称分隔符('/'或'\'或其他)
    public static final String PATH_SEPARATOR = File.pathSeparator;// 文件系统路径分隔符(':'或';'或其他)

    /**
     * 返回非显式调用的异常栈，性能并没有多大提高
     */
    public static String getCompleteStackTraceInfo(String title) {
        title = title == null ? "StackTrace : Without Message" : title;
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
    public static StackTraceElement[] getStackTrace() {
        return (new Throwable()).getStackTrace();
    }

    /**
     * 当前标准时间戳
     * UTC +0 1970-01-01 00:00:00.0至当前的毫秒数
     * 基于计算机系统，会产生误差
     */
    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 纳秒级时间，此方法没有固定基准
     * 仅可用于计时运算，此方法可能会返回负值
     * 在不同实例（虚拟机运行时）中，此方法同一时间的返回值不相同
     */
    public static long nanoTime() {
        return System.nanoTime();
    }
}
