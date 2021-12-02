package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("all")
public class StringUtils {

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
        }
        return res;
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
     * java原生toString方式
     */
    public static String toStringNative(Object o) {
        if (o == null)
            return "null";

        return o.getClass().getName() + '@' + Integer.toHexString(o.hashCode());
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
            Object ele = it.next();
            if (ele == null && !nullAppendable)
                continue;

            if (appendSeparator)
                sb.append(separator);

            // null will be automatically converted to 'null'
            sb.append(ele);
            appendSeparator = true;
        }

        return sb.toString();
    }

    public static String toString(Iterator<?> it, String separator) {
        return toString(it, separator, false);
    }

    public static String toString(Iterator<?> it, boolean nullAppendable) {
        return toString(it, ", ", nullAppendable);
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
        } catch (IOException ignored) {
        }
        return writer.toString();
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
        Assert.notNull(str);
        return str.replace('.', '/');
    }

    /**
     * @since 2.0.1
     */
    public static String padLeft(String ori, int totalWidth, char paddingChar) {
        Assert.notNull(ori);
        int oriLen = ori.length();
        int padLen = totalWidth - oriLen;
        if (padLen <= 0)
            return ori;

        return toString(paddingChar, padLen) + ori;
    }

    /**
     * @since 2.0.1
     */
    public static String padRight(String ori, int totalWidth, char paddingChar) {
        Assert.notNull(ori);
        int oriLen = ori.length();
        int padLen = totalWidth - oriLen;
        if (padLen <= 0)
            return ori;

        return ori + toString(paddingChar, padLen);
    }

    public static String toString(char ch, int len) {
        char[] chars = new char[len];
        Arrays.fill(chars, ch);
        return String.valueOf(chars);
    }

}
