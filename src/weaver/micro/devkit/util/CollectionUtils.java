package weaver.micro.devkit.util;

import weaver.conn.RecordSet;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.handler.StrictRecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 2.0.1
 */
public class CollectionUtils {

    public static <K, V> void transferField(Map<K, V> m, K ori, K dest) {
        V val = m.get(ori);
        m.remove(ori);
        m.put(dest, val);
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
