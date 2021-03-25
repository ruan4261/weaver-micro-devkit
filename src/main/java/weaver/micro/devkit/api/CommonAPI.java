package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.micro.devkit.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有模块都可以使用此接口简化开发
 *
 * @author ruan4261
 */
public final class CommonAPI {

    /**
     * 通过一条sql语句查询一个字段
     * 查询不到则返回空字符串
     */
    public static String querySingleField(final String sql, final String field) {
        Assert.notEmpty(sql, "execute sql");
        Assert.notEmpty(sql, "field");
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        rs.next();
        return rs.getString(field);
    }

    /**
     * 从RecordSet当前行读取一张键值映射表
     * <hr>
     * 所有键值将会使用小写!!
     */
    public static Map<String, String> mapFromRecordRow(final RecordSet rs) {
        Assert.notNull(rs, "RecordSet");
        String[] cols = rs.getColumnName();
        Map<String, String> result = new HashMap<String, String>(cols.length + (cols.length >> 1));
        for (String key : cols) {
            if (key != null && !key.equals("")) {
                String value = rs.getString(key);
                result.put(key.toLowerCase(), value);
            }
        }
        return result;
    }

    /**
     * @param table      目标表名
     * @param fields     目标字段名, 为空时默认获取所有字段
     * @param conditions 查询字符串: xx='xx' 自动用and连接
     */
    public static List<Map<String, String>> query(String table, String fields, String... conditions) {
        Assert.notEmpty(table);
        if (fields == null || fields.equals(""))
            fields = "*";

        StringBuilder builder = null;
        if (conditions != null && conditions.length > 0) {
            builder = new StringBuilder(" where ");
            for (int i = 0; i < conditions.length; i++) {
                if (i > 0)
                    builder.append(" and ");

                builder.append(conditions[i]);
            }
        }

        RecordSet rs = new RecordSet();
        rs.execute("select " + fields + " from " + table + (builder == null ? "" : builder.toString()));

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        while (rs.next()) {
            result.add(mapFromRecordRow(rs));
        }

        return result;
    }

}
