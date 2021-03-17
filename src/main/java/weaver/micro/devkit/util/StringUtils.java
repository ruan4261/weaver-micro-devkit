package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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

    /**
     * 有toString方法会优先调用toString方法
     * 否则递归输出所有字段
     *
     * 输出格式不固定
     *
     * @since 1.1.4
     */
    public static String fullRecursionPrint(Object obj) {
        if (obj == null) {
            return "null";
        } else if (BeanUtil.isPrimitive(obj.getClass())) {
            return obj.toString();
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            int i = 0;
            while (true) {
                Object e = Array.get(obj, i++);
                sb.append(e == obj ?
                        "(this array)" : fullRecursionPrint(e));
                if (i == len)
                    return sb.append(']').toString();
                sb.append(',')
                        .append(' ');
            }
        } else if (obj instanceof Collection<?>) {
            Iterator<?> it = ((Collection<?>) obj).iterator();
            if (!it.hasNext())
                return "[]";

            StringBuilder sb = new StringBuilder();
            sb.append('[');
            while (true) {
                Object e = it.next();
                sb.append(e == obj ?
                        "(this collection)" : fullRecursionPrint(e));
                if (!it.hasNext())// next or end
                    return sb.append(']').toString();
                sb.append(',')
                        .append(' ');
            }
        } else if (obj instanceof Map<?, ?>) {
            Iterator<? extends Map.Entry<?, ?>> i = ((Map<?, ?>) obj).entrySet().iterator();
            if (!i.hasNext())
                return "{}";

            StringBuilder sb = new StringBuilder();
            sb.append('{');
            while (true) {
                Map.Entry<?, ?> e = i.next();
                Object key = e.getKey();
                Object value = e.getValue();
                sb.append(key == obj ?
                        "(this map)" : fullRecursionPrint(key));
                sb.append('=');
                sb.append(value == obj ?
                        "(this map)" : fullRecursionPrint(value));
                if (!i.hasNext())// next or end
                    return sb.append('}').toString();
                sb.append(',')
                        .append(' ');
            }
        } else {
            return fullRecursionPrint(BeanUtil.object2Map(obj, 0));
        }
    }

}
