package weaver.micro.devkit.api;

import weaver.general.Util;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.handler.StrictRecordSet;
import weaver.micro.devkit.util.ArrayUtil;

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

        StrictRecordSet rs = new StrictRecordSet();
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

    public static int queryDepartIdByHrmId(int hrmId) {
        return Cast.o2Integer(CommonAPI.querySingleField("select departmentid from hrmresource where id=" + hrmId, "departmentid"));
    }

    public static int querySubCompanyIdByHrmId(int hrmId) {
        return Cast.o2Integer(CommonAPI.querySingleField("select subcompanyid1 from hrmresource where id=" + hrmId, "subcompanyid1"));
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

    public static String queryDepartNameByHrmId(int hrmId) {
        String sql = "select departmentname from hrmdepartment a left outer join hrmresource b on a.id=b.departmentid where b.id=" + hrmId;
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

    public static String querySubCompanyNameById(int id) {
        return CommonAPI.querySingleField("select subcompanyname from hrmsubcompany where id=" + id, "subcompanyname");
    }

    public static String getJobLevelByHrmId(int hrmId) {
        return CommonAPI.querySingleField("select joblevel from hrmresource where id=" + hrmId, "joblevel");
    }

    public static int[] getDepartTraceByHrmId(int hrmId) {
        int depart = queryDepartIdByHrmId(hrmId);
        return getDepartTrace(depart);
    }

    public static int[] getDepartTrace(int depart) {
        int[] departTrace = new int[8];
        int idx = 0;
        StrictRecordSet rs = new StrictRecordSet();
        while (depart > 0) {
            if (idx == departTrace.length) {
                departTrace = ArrayUtil.arrayExtend(departTrace, idx + (idx >> 1));
            }
            departTrace[idx++] = depart;

            rs.execute("select supdepid from hrmdepartment where id = " + depart);
            rs.next();
            depart = rs.getInt("supdepid");
        }

        if (idx != departTrace.length)
            departTrace = ArrayUtil.arrayExtend(departTrace, idx);
        return departTrace;
    }

}
