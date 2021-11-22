package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.TimeUtil;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.handler.StrictRecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 建模处理
 *
 * @author ruan4261
 */
public final class ModeAPI {

    /**
     * 创建建模主表中的一行数据，带有权限重构
     *
     * @param modeMainTable 建模
     * @param modeId        建模id
     * @param creatorId     数据创建者
     * @param data          数据集，可为空, 值会直接toString()插表
     * @return 建模主表数据行id，方法执行失败返回-1
     */
    public static int createModeData(String modeMainTable, int modeId, int creatorId, Map<String, Object> data) {
        Assert.notEmpty(modeMainTable, "modeMainTable");
        RecordSet rs = new StrictRecordSet();
        String uuid = UUID.randomUUID().toString();
        rs.executeUpdate("insert into " + modeMainTable + "(uuid, modedatacreater, modedatacreatedate," +
                        " modedatacreatetime, formmodeid) " +
                        "values(?, ?, ?, ?, ?)",
                uuid, creatorId, TimeUtil.getCurrentDateString(), TimeUtil.getOnlyCurrentTimeString(), modeId);
        rs.executeQuery("select id from " + modeMainTable + " where uuid = ?", uuid);
        rs.next();
        int id = rs.getInt("id");

        if (id != -1) {
            if (data != null && !data.isEmpty()) {
                // 录入数据
                int size = data.size();
                List<Object> args = new ArrayList<Object>(size + 1);
                StringBuilder sql = new StringBuilder("update " + modeMainTable + " set ");
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String k = entry.getKey();
                    Object v = entry.getValue();

                    if (k == null || k.equals(""))
                        continue;

                    if (!args.isEmpty())
                        sql.append(',').append(' ');

                    sql.append(k).append(" = ?");
                    args.add(v);
                }

                if (!args.isEmpty()) {
                    sql.append(" where id = ?");
                    args.add(id);

                    // execute
                    rs.executeUpdate(sql.toString(), args.toArray(new Object[0]));
                }
            }

            // 权限重构
            ModeRightInfo right = new ModeRightInfo();
            right.editModeDataShare(creatorId, modeId, id);
        }
        return id;
    }

}
