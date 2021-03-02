package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

public class StringUtils {

    public static String makeStackTraceInfo(Throwable t) {
        Assert.notNull(t);
        StackTraceElement[] trace = t.getStackTrace();
        StringBuilder builder = new StringBuilder(trace.length << 5);
        builder.append(t.getClass().getName())
                .append(':')
                .append(t.getMessage());
        for (StackTraceElement traceElement : trace) {
            builder.append("\n")
                    .append("\tat ")
                    .append(traceElement);
        }
        return builder.toString();
    }

}
