package weaver.interfaces.micro.devkit.api;

import weaver.conn.RecordSet;

/**
 * 所有模块都可以使用此接口简化开发
 *
 * @author ruan4261
 */
public interface CommonAPI {

    /**
     * 通过一条sql语句查询一个字段
     * 查询不到则返回空字符串
     */
    static String querySingleField(final String sql, final String field) {
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        rs.next();
        return rs.getString(field);
    }

}
