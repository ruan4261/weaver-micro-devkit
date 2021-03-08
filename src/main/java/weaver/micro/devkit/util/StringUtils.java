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

    /**
     * Check whether s1 include s2(val.toString())
     */
    public static boolean isInclude(String s1, int val) {
        Assert.notEmpty(s1);
        int offset = 0;
        int len = s1.length();
        String s2 = Integer.toString(val);
        int wid = s2.length();
        int lastPossible = len - wid;

        while (offset <= lastPossible) {
            offset = s1.indexOf(s2, offset);
            if (offset == -1)
                return false;

            int end = offset + wid;
            // check start
            if (offset > 0) {
                char prev = s1.charAt(offset - 1);
                if (prev == '-' || (prev >= '0' && prev <= '9')) {
                    offset = end;
                    continue;
                }
            }

            // check end
            if (end < len) {
                char next = s1.charAt(end);
                if (next >= '0' && next <= '9') {
                    offset = end;
                    continue;
                }
            }

            return true;
        }

        return false;
    }

}
