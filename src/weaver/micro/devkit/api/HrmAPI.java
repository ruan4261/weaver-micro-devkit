package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.handler.StrictRecordSet;
import weaver.micro.devkit.util.ArrayUtils;

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
    public static String queryHrmName(int hrmId) {
        return CommonAPI.querySingleField("select lastname from HrmResource where id = ?", hrmId);
    }

    public static String queryHrmNameByHrmWorkCode(String workCode) {
        return CommonAPI.querySingleField("select lastname from hrmresource where workcode = ?", workCode);
    }

    public static int queryDepartIdByHrmId(int hrmId) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select departmentid from hrmresource where id = ?", hrmId));
    }

    public static int querySubCompanyIdByHrmId(int hrmId) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select subcompanyid1 from hrmresource where id = ?", hrmId));
    }

    /**
     * 通过部门id查询部门名称
     *
     * @param departId 部门id
     * @return 部门名称
     */
    public static String queryDepartName(int departId) {
        return CommonAPI.querySingleField("select departmentname from hrmdepartment where id = ?", departId);
    }

    public static String queryDepartNameByHrmId(int hrmId) {
        return CommonAPI.querySingleField("select departmentname from hrmdepartment a" +
                " left outer join hrmresource b on a.id=b.departmentid where b.id = ?", hrmId);
    }

    public static int queryHrmIdByHrmName(String hrmName) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select id from HrmResource where lastname = ?", hrmName));
    }

    public static int queryDepartIdByDepartName(String departName) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select id from HrmResource where departmentname = ?", departName));
    }

    public static String querySubCompanyNameById(int id) {
        return CommonAPI.querySingleField("select subcompanyname from hrmsubcompany where id = ?", id);
    }

    public static String getJobLevelByHrmId(int hrmId) {
        return CommonAPI.querySingleField("select joblevel from hrmresource where id = ?", hrmId);
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
                departTrace = ArrayUtils.arrayExtend(departTrace, idx + (idx >> 1));
            }
            departTrace[idx++] = depart;

            rs.execute("select supdepid from hrmdepartment where id = " + depart);
            rs.next();
            depart = rs.getInt("supdepid");
        }

        if (idx != departTrace.length)
            departTrace = ArrayUtils.arrayExtend(departTrace, idx);
        return departTrace;
    }

    /**
     * 获取一个分部的名称全路径, 分隔符为'>', 该字符串可以在同步时使用
     *
     * @since 2.0.1
     */
    public static String querySubCompanyFullPath(int subCompanyId) {
        RecordSet rs = new StrictRecordSet();
        StringBuilder pathBuilder = new StringBuilder();
        while (subCompanyId > 0) {
            rs.executeQuery("select supsubcomid, subcompanyname from hrmsubcompany where id = ?", subCompanyId);
            if (rs.next()) {
                subCompanyId = rs.getInt(1);
                if (pathBuilder.length() != 0)
                    pathBuilder.insert(0, '>');
                pathBuilder.insert(0, rs.getString(2));
            } else break;
        }

        return pathBuilder.toString();
    }

    /**
     * 获取一个部门的名称全路径, 分隔符为'>', 该字符串可以在同步时使用
     *
     * @since 2.0.1
     */
    public static String queryDepartmentFullPath(int departmentId) {
        RecordSet rs = new StrictRecordSet();
        StringBuilder pathBuilder = new StringBuilder();
        while (departmentId > 0) {
            rs.executeQuery("select supdepid, departmentname from hrmdepartment where id = ?", departmentId);
            if (rs.next()) {
                departmentId = rs.getInt(1);
                if (pathBuilder.length() != 0)
                    pathBuilder.insert(0, '>');
                pathBuilder.insert(0, rs.getString(2));
            } else break;
        }

        return pathBuilder.toString();
    }

}
