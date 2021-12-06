package weaver.micro.devkit.util;

import weaver.conn.RecordSet;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.handler.StrictRecordSet;

import java.util.*;

/**
 * @since 2.0.1
 */
public class CollectionUtils {

    public static <K, V> void transferField(Map<K, V> m, K ori, K dest) {
        V val = m.get(ori);
        m.remove(ori);
        m.put(dest, val);
    }

    /**
     * <pre>
     * 请注意
     * 如果字符串的最后一个字符为 '_', 该字符会被直接删除, 不进行其他操作
     * 如以下两个字符串经过处理后, 会变为同一个字符串, 导致映射被覆盖
     * 'hello_world' >> 'helloWorld'
     * 'hello_world_' >> 'helloWorld'
     * </pre>
     */
    public static <V> void snakeCase2CamelCase(Map<String, V> m) {
        Map<String, V> camelCaseReplace = new HashMap<String, V>(m.size());
        Iterator<Map.Entry<String, V>> it = m.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, V> entry = it.next();
            String k = entry.getKey();
            V v = entry.getValue();

            if (k != null && k.contains("_")) {
                StringBuilder builder = new StringBuilder(k);
                for (int i = 0; i < builder.length(); ) {
                    int idx = builder.indexOf("_", i);
                    if (idx < 0)
                        break;

                    builder.deleteCharAt(idx);

                    if (idx < builder.length())
                        builder.setCharAt(idx, Character.toUpperCase(builder.charAt(idx)));

                    i = idx;// 此处不用 +1, 因为 builder 中的该下标已被动态移除
                }

                it.remove();
                camelCaseReplace.put(builder.toString(), v);
            }
        }
        m.putAll(camelCaseReplace);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> mapValueObj2String(Map<String, Object> m) {
        for (Map.Entry<String, Object> entry : m.entrySet()) {
            Object val = entry.getValue();
            if (val != null) {
                if (!(val instanceof String)) {
                    m.put(entry.getKey(), val.toString());
                }
            }
        }
        return ((Map<String, String>) (Object) m);
    }

    public static void insert(String table, Map<String, String> m) {
        Assert.notEmpty(table);
        Assert.notEmpty(m);
        int len = m.size();
        List<Object> args = new ArrayList<Object>(len);
        StringBuilder fields = new StringBuilder(len << 3);
        StringBuilder placeHolder = new StringBuilder((len << 2) - len);// ?, ?, ? -> 3n - 2

        boolean flag = false;
        for (Map.Entry<String, String> entry : m.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key != null && !key.isEmpty()) {
                if (flag) {
                    fields.append(',').append(' ');
                    placeHolder.append(',').append(' ');
                }

                flag = true;
                fields.append(key);
                placeHolder.append('?');
                args.add(value);
            }
        }

        RecordSet rs = new StrictRecordSet();
        rs.executeUpdate("insert into " + table + "(" + fields + ") values(" + placeHolder + ")",
                args.toArray(new Object[0]));
    }

}
