package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class StringUtils {

    /**
     * 用于{@link #fullRecursionPrint(Object)}方法内部去重
     */
    private final static ThreadLocal<Set<Object>> repeatObjects = new ThreadLocal<Set<Object>>() {

        @Override
        protected Set<Object> initialValue() {
            return new HashSet<Object>();
        }

    };

    public static String toString(Throwable t) {
        if (t == null)
            return "null";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out, true);
        t.printStackTrace(printStream);

        String res = out.toString();
        try {
            out.close();
        } catch (IOException ignored) {
            // ByteArrayOutputStream不会出现此类异常
        }
        return res;
    }

    /**
     * @see #toString(Throwable)
     */
    @Deprecated
    public static String makeStackTraceInfo(Throwable t) {
        return toString(t);
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
     * 元类型, 数值类型及字符序列类型直接输出<br>
     * 集合对象如array, collection, map会解析输出内部元素<br>
     * 非以上情况下如有toString方法会优先调用toString方法<br>
     * 否则递归输出对象内所有字段
     * <br><br>
     * 输出格式无特别规范
     *
     * @see #fullRecursionPrint0(Object)
     * @since 1.1.4
     * @deprecated new print function see {@link VisualPrintUtils}
     */
    @Deprecated
    public static String fullRecursionPrint(Object obj) {
        String str = fullRecursionPrint0(obj);
        repeatObjects.remove();
        return str;
    }

    /**
     * internal method
     *
     * @see #fullRecursionPrint(Object)
     */
    static String fullRecursionPrint0(Object obj) {
        Class<?> clazz;
        if (obj == null || (clazz = obj.getClass()) == null) {
            return "null";
        }

        // 该信息用于提示重复
        String nativeInfo = '<' + toStringNative(obj) + '>';
        Set<Object> repeated = repeatObjects.get();
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
     * internal method
     *
     * @see #fullRecursionPrint(Object)
     */
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

    /**
     * internal method
     *
     * @see #fullRecursionPrint(Object)
     */
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

    /**
     * internal method
     *
     * @see #fullRecursionPrint(Object)
     */
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

    /**
     * java原生toString方式
     */
    public static String toStringNative(Object o) {
        if (o == null)
            return "null";

        return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
    }

    /**
     * 集合类型的toString函数, 可自定义分隔符, 可对null进行过滤, 并不带边界符号<br>
     * 如果collection为空则返回空字符串
     *
     * @param nullAppendable 如果为true, 则集合中的空指针会转换为"null"字符串; 否则其将被过滤
     */
    public static String toString(Iterator<?> it, String separator, boolean nullAppendable) {
        StringBuilder sb = new StringBuilder();
        boolean appendSeparator = false;

        while (it.hasNext()) {
            if (appendSeparator) {
                sb.append(separator);
                appendSeparator = false;
            }

            Object ele = it.next();
            if (ele == null) {
                if (nullAppendable)
                    sb.append("null");
                else
                    continue;
            } else sb.append(ele.toString());

            appendSeparator = true;
        }

        return sb.toString();
    }

    public static String toString(Iterator<?> it, String separator) {
        return toString(it, separator, false);
    }

    public static String toString(Iterator<?> it) {
        return toString(it, ", ");
    }

    /**
     * @see #toString(Iterator, String, boolean)
     * @deprecated 保持兼容性
     */
    @Deprecated
    public static String toString(Collection<?> collection, String separator, boolean nullAppendable) {
        Iterator<?> it = collection.iterator();
        return toString(it, separator, nullAppendable);
    }

    public static String escapeString(String str) {
        StringWriter writer = new StringWriter(str.length() << 1);
        try {
            escapeString(writer, str);
            return writer.toString();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * 还原出java代码中的格式
     */
    public static void escapeString(Writer out, String str) throws IOException {
        Assert.notNull(out);
        Assert.notNull(str);
        int sz = str.length();
        for (int i = 0; i < sz; ++i) {
            char ch = str.charAt(i);
            switch (ch) {
                case '\b':
                    out.write(92);
                    out.write(98);
                    break;
                case '\t':
                    out.write(92);
                    out.write(116);
                    break;
                case '\n':
                    out.write(92);
                    out.write(110);
                    break;
                case '\f':
                    out.write(92);
                    out.write(102);
                    break;
                case '\r':
                    out.write(92);
                    out.write(114);
                    break;
                case '"':
                    out.write(92);
                    out.write(34);
                    break;
                case '\\':
                    out.write(92);
                    out.write(92);
                    break;
                default:
                    out.write(ch);
                    break;
            }
        }
    }

    public static String dotToSlash(String str) {
        return str.replace('.', '/');
    }

}
