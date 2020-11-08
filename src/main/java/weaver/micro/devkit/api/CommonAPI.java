package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.micro.devkit.Assert;

import java.util.HashMap;
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
     */
    public static Map<String, String> mapFromRecordRow(final RecordSet rs) {
        Assert.notNull(rs, "RecordSet");
        Map<String, String> result = new HashMap<String, String>();
        String[] cols = rs.getColumnName();
        for (String key : cols) {
            String value = rs.getString(key);
            result.put(key, value);
        }
        return result;
    }
}
