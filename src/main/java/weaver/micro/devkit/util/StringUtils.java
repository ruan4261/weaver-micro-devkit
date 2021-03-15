package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

public class StringUtils {

    public static String makeStackTraceInfo(Throwable t) {
        Assert.notNull(t);
        StackTraceElement[] trace = t.getStackTrace();

        return t.toString() + '\n' + makeStackTraceInfo(trace, "\tat ");
    }

    /**
     * 自动跨行, 前缀加载每行行头
     * 返回字符串的尾部有空行
     */
    public static String makeStackTraceInfo(StackTraceElement[] trace, String prefix) {
        if (trace == null || trace.length == 0)
            return "";

        StringBuilder builder = new StringBuilder(trace.length << 6);
        for (StackTraceElement traceElement : trace) {
            if (prefix != null)
                builder.append(prefix);

            builder.append(traceElement)
                    .append('\n');
        }
        return builder.toString();
    }

    /**
     * Check whether s1 include s2(val.toString()).
     * The minus sign cannot be used as separators.
     */
    public static boolean isInclude(String s1, int val) {
        Assert.notNull(s1);
        int len = s1.length();
        if (len == 0)
            return false;

        String s2 = Integer.toString(val);
        int wid = s2.length();
        int lastPossible = len - wid;

        int offset = 0;
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
