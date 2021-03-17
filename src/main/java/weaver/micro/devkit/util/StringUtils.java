package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.lang.reflect.Array;
import java.util.*;

public class StringUtils {

    /**
     * 用于{@link #fullRecursionPrint(Object)}方法内部去重
     */
    private final static ThreadLocal<Set<Object>> REPEAT_POND = new ThreadLocal<Set<Object>>() {

        @Override
        protected Set<Object> initialValue() {
            return new HashSet<Object>();
        }

    };

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
     * 元类型直接输出, array, collection, map会被解析
     * 如有toString方法会优先调用toString方法
     * 否则递归输出所有字段
     *
     * 输出格式不固定
     *
     * @since 1.1.4
     */
    public static String fullRecursionPrint(Object obj) {
        String str = fullRecursionPrint0(obj);
        REPEAT_POND.remove();
        return str;
    }

    static String fullRecursionPrint0(Object obj) {
        Class<?> clazz;
        if (obj == null || (clazz = obj.getClass()) == null) {
            return "null";
        }

        // 该信息用于提示重复
        String nativeInfo = '<' + toStringNative(obj) + '>';
        Set<Object> repeated = REPEAT_POND.get();
        if (repeated.contains(obj)) {
            return "(repeat object: " + nativeInfo + ")";
        }

        String res;
        if (BeanUtil.isPrimitive(clazz)
                || obj instanceof Number
                || obj instanceof CharSequence) {
            return obj.toString();
        } else if (clazz.isArray()) {
            repeated.add(obj);
            res = nativeInfo + fullRecursionPrintArray(obj);
        } else if (obj instanceof Collection<?>) {
            repeated.add(obj);
            res = nativeInfo + fullRecursionPrintCollection(((Collection<?>) obj));
        } else if (obj instanceof Map<?, ?>) {
            repeated.add(obj);
            res = nativeInfo + fullRecursionPrintMap(((Map<?, ?>) obj));
        } else if (BeanUtil.hasOwnMethod(clazz, "toString")) {
            // 非结构化对象, 并且有自定义toString
            return obj.toString();
        } else {
            repeated.add(obj);
            res = nativeInfo + fullRecursionPrintMap(BeanUtil.object2Map(obj, 0));
        }

        return res;
    }

    /**
     * java原生toString方式
     */
    public static String toStringNative(Object o) {
        if (o == null)
            return "null";

        return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
    }

    static String fullRecursionPrintMap(Map<?, ?> m) {
        if (m == null)
            return "null";

        Iterator<? extends Map.Entry<?, ?>> i = m.entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            Map.Entry<?, ?> e = i.next();
            Object key = e.getKey();
            Object value = e.getValue();
            sb.append(fullRecursionPrint0(key));
            sb.append('=');
            sb.append(fullRecursionPrint0(value));
            if (!i.hasNext())// next or end
                return sb.append('}').toString();
            sb.append(',')
                    .append(' ');
        }
    }

    static String fullRecursionPrintCollection(Collection<?> collection) {
        if (collection == null)
            return "null";

        Iterator<?> it = collection.iterator();
        if (!it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (true) {
            Object e = it.next();
            sb.append(fullRecursionPrint0(e));
            if (!it.hasNext())// next or end
                return sb.append(']').toString();
            sb.append(',')
                    .append(' ');
        }
    }

    static String fullRecursionPrintArray(Object arr) {
        if (arr == null)
            return "null";

        int len = Array.getLength(arr);
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        int i = 0;
        while (true) {
            Object e = Array.get(arr, i++);
            sb.append(fullRecursionPrint0(e));
            if (i == len)
                return sb.append(']').toString();
            sb.append(',')
                    .append(' ');
        }
    }

}
