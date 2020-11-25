package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.micro.devkit.Cast;

import java.util.HashMap;
import java.util.Map;

/**
 * 人力资源处理接口，包括组织架构各模块。
 *
 * @author ruan4261
 */
public final class HrmAPI {

    /**
     * 根据id查询人员姓名
     *
     * @param hrmId 人员id
     * @return lastname
     */
    public static String queryHrmName(final int hrmId) {
        String sql = "select lastname from HrmResource where id = " + hrmId;
        return CommonAPI.querySingleField(sql, "lastname");
    }

    /**
     * 根据id查询人力资源信息
     *
     * @param hrmId 人员id
     * @return lastname 人力资源名称
     *         managerid 上级领导id
     *         jobtitlename 职位、岗位
     *         departmentname 部门名称
     *         subcompanyname 分部名称
     */
    public static Map<String, String> queryHrmInfo(final int hrmId) {
        Map<String, String> result = new HashMap<String, String>();

        RecordSet rs = new RecordSet();
        String sql = "select a.lastname,a.managerid,d.jobtitlename,b.departmentname,c.subcompanyname from HrmResource a left outer join hrmdepartment b on a.departmentid=b.id left outer join HrmSubCompany c on a.subcompanyid1=c.id left outer join HrmJobTitles d on a.jobtitle=d.id where a.id=" + hrmId;
        rs.execute(sql);
        if (!rs.next()) return result;

        result.put("lastname", Util.null2String(rs.getString("lastname")));// 全名
        result.put("managerid", Util.null2String(rs.getString("managerid")));// 上级领导id
        result.put("jobtitlename", Util.null2String(rs.getString("jobtitlename")));// 职位、岗位
        result.put("departmentname", Util.null2String(rs.getString("departmentname")));// 部门名称
        result.put("subcompanyname", Util.null2String(rs.getString("subcompanyname")));// 分部名称
        return result;
    }

    /**
     * 通过部门id查询部门名称
     *
     * @param departId 部门id
     * @return 部门名称
     */
    public static String queryDepartName(final int departId) {
        String sql = "select departmentname from hrmdepartment where id = " + departId;
        return CommonAPI.querySingleField(sql, "departmentname");
    }

    public static int queryHrmIdByHrmName(String hrmName) {
        String sql = "select id from HrmResource where lastname='" + hrmName + "'";
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "id"));
    }

    public static int queryDepartIdByDepartName(String departName) {
        String sql = "select id from HrmResource where departmentname='" + departName + "'";
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "id"));
    }
}
