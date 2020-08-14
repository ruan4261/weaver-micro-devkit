package weaver.interfaces.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.TimeUtil;
import weaver.general.Util;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 建模处理
 *
 * @author ruan4261
 */
public interface ModeAPI {

    /**
     * 创建建模主表中的一行数据，带有权限重构
     *
     * @param modeTableMain 建模
     * @param modeId        建模id
     * @param creatorId     数据创建者
     * @param data          数据集
     * @return 建模主表数据行id，方法执行失败返回-1
     */
    static int createModeData(String modeTableMain, int modeId, int creatorId, Map<String, Object> data) {
        int id = -1;
        RecordSet rs = new RecordSet();
        String uuid = UUID.randomUUID().toString();
        if (rs.execute("insert into " + modeTableMain + "(uuid,modedatacreater,modedatacreatedate,modedatacreatetime,formmodeid) values('" + uuid + "'," + creatorId + ",'" + TimeUtil.getCurrentDateString() + "','" + TimeUtil.getOnlyCurrentTimeString() + "'," + modeId + ")")) {
            rs.execute("select id from " + modeTableMain + " where uuid='" + uuid + "'");
            rs.next();
            id = Util.getIntValue(rs.getString("id"));

            if (id >= 0) {
                // 录入数据
                StringBuilder sql = new StringBuilder("update " + modeTableMain + " set ");
                Set<Map.Entry<String, Object>> set = data.entrySet();
                for (Map.Entry entry : set) {
                    sql.append(entry.getKey()).append("='").append(entry.getValue().toString()).append("',");
                }
                if (sql.charAt(sql.length() - 1) == ',') {
                    sql = new StringBuilder(sql.substring(0, sql.length() - 1));
                    sql.append(" where id=").append(id);
                    rs.execute(sql.toString());
                }
                // 权限重构
                ModeRightInfo right = new ModeRightInfo();
                right.editModeDataShare(creatorId, modeId, id);
            }
        }
        return id;
    }

}
