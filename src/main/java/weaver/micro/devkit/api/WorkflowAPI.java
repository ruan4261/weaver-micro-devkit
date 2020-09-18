package weaver.micro.devkit.api;

import static org.r2.devkit.core.CacheBase.EMPTY;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.workflow.action.WorkflowToDoc;
import org.r2.devkit.Assert;
import org.r2.devkit.Cast;

import java.util.*;

/**
 * 流程操作工具。
 *
 * @author ruan4261
 */
public interface WorkflowAPI {

    /** 流程中的签字意见类型映射 */
    Map<String, String> logTypeMapper = new HashMap<String, String>() {
        public Map<String, String> construct() {
            super.put("0", "批准");
            super.put("1", "保存");
            super.put("2", "提交");
            super.put("3", "退回");
            super.put("4", "重新打开");
            super.put("5", "删除");
            super.put("6", "激活");
            super.put("7", "转发");
            super.put("9", "批注");
            super.put("e", "强制归档");
            super.put("t", "抄送");
            super.put("s", "督办");
            return this;
        }
    }.construct();

    /**
     * 根据requestId获取流程实例标题
     *
     * @param requestId 流程实例id
     * @return 流程实例标题
     */
    static String queryWorkflowTitle(final int requestId) {
        String sql = "select requestname from workflow_requestbase where requestid = " + requestId;
        return CommonAPI.querySingleField(sql, "requestname");
    }

    /**
     * 根据requestId获取流程实例签字意见列表
     *
     * @param requestId 流程实例id
     * @return logid 签字日志id
     *         nodeid 节点id
     *         nodename 节点名称
     *         operator 操作者id，如为1则是系统管理员，此id无法查询人力资源表
     *         logtype 签字意见，已对应转化为确定类型，详情请查看{@link #logTypeMapper}
     *         remark 签字意见
     *         operatedate 操作日期
     *         operatetime 操作时间
     */
    static List<Map<String, String>> queryRemarkList(final int requestId) {
        List<Map<String, String>> result = new ArrayList<>();

        RecordSet rs = new RecordSet();
        String sql = "select a.nodeid,b.nodename,a.logid,a.operator,a.logtype,a.remark,a.operatedate,a.operatetime from workflow_requestLog a left outer join workflow_nodebase b on a.nodeid=b.id where a.requestid=" + requestId + " order by a.nodeid";
        rs.execute(sql);
        if (!rs.next()) return result;

        while (rs.next()) {
            Map<String, String> map = new HashMap<>();
            map.put("logid", Util.null2String(rs.getString("logid")));// 签字日志id
            map.put("nodeid", Util.null2String(rs.getString("nodeid")));// 节点id
            map.put("nodename", Util.null2String(rs.getString("nodename")));// 节点名称
            map.put("operator", Util.null2String(rs.getString("operator")));// 操作者的id
            map.put("logtype", Util.null2String(logTypeMapper.get(rs.getString("logtype"))));// 签字类型
            map.put("remark", Util.delHtml(Util.null2String(rs.getString("remark"))));// 签字意见
            map.put("operatedate", Util.null2String(rs.getString("operatedate")));// 操作日期
            map.put("operatetime", Util.null2String(rs.getString("operatetime")));// 操作时间
            result.add(map);
        }
        return result;
    }

    /**
     * 通过部门id查询部门名称
     *
     * @param departId 部门id
     * @return 部门名称
     */
    static String queryDepartName(final int departId) {
        String sql = "select departmentname from hrmdepartment where id = " + departId;
        return CommonAPI.querySingleField(sql, "departmentname");
    }

    /**
     * 获取表单字段简写到id的映射
     * 只可获取表单名称为formtable_main_{billid}的表单字段映射
     *
     * @param billid 流程表单id
     * @param num    0：主表，大于0为明细表
     * @return 字段映射
     */
    static Map<String, String> queryFieldMapper(int billid, final String num) {
        Assert.notEmpty(num, "table number");
        Map<String, String> result = new HashMap<>();
        billid = Math.abs(billid);
        RecordSet rs = new RecordSet();
        String sql;
        if ("0".equals(num)) {
            sql = "select b.id,fieldname,detailtable from workflow_billfield b ,workflow_base a where b.billid=-"
                    + billid
                    + " and a.formid=b.billid and (detailtable is null or detailtable = '') ";
        } else {
            sql = "select b.id,fieldname,detailtable from workflow_billfield b ,workflow_base a where b.billid=-"
                    + billid
                    + " and a.formid=b.billid and detailtable='formtable_main_"
                    + billid + "_dt" + num + "'";
        }
        rs.execute(sql);
        while (rs.next()) {
            result.put(Util.null2String(rs.getString("fieldname")).toLowerCase(), Util.null2String(rs.getString("id")));
        }

        return result;
    }

    /**
     * 根据workflowid获取billTableName
     *
     * @param workflowId 流程id
     * @return 数据库表单名称
     */
    static String queryTableName(final int workflowId) {
        String sql = "select b.tablename from workflow_base a,workflow_bill b where a.formid = b.id and a.id = " + workflowId;
        return CommonAPI.querySingleField(sql, "tablename");
    }

    /**
     * 流程存为文档
     * 在E8系统中，这个接口是同步的。
     * 在E9系统中，这个接口是异步的，无法保证何时完成文档转换，调用完本接口再通过接口查询文档不能及时获取到文档。
     *
     * @param requestId 请求id
     * @return 是否成功
     */
    static boolean workflowToDoc(final int requestId) {
        RecordSet rs = new RecordSet();
        RecordSet rs2 = new RecordSet();
        rs.execute("select workflowid,requestname,creater from workflow_requestbase where requestid =" + requestId);
        if (rs.next()) {
            String workflowid = rs.getString("workflowid");
            String requestname = rs.getString("requestname");
            String creater = rs.getString("creater");
            rs2.execute("select status from hrmresource where id=" + Util.getIntValue(creater));
            if (!rs2.next() || 5 == Util.getIntValue(rs2.getString("status"))) return false;
            return new WorkflowToDoc().Start(Util.null2String(requestId), creater, requestname, workflowid);
        } else return false;
    }

    /**
     * 保存流程页面（请提前配置流程存为文档，并至少提前一个节点触发使其生成文档）
     * 服务器保存的文件名为{requestid}.html
     *
     * @param requestId 请求id
     * @param path      保存路径
     * @return 保存的全路径
     */
    static String saveWorkflowHtml(final int requestId, final String path) {
        Assert.notEmpty(path, "path");

        String sql = "select max(id) id from DocDetail where fromworkflow = " + requestId;
        String docid = CommonAPI.querySingleField(sql, "id");
        if (EMPTY.equals(docid)) return EMPTY;

        return DocAPI.saveDocLocally(Cast.o2Integer(docid), path, requestId + ".html", null);
    }

    /**
     * 根据请求查询billTable
     *
     * @param requestId 流程请求id
     */
    static String queryBillTableByRequest(final int requestId) {
        String sql = "select tablename from workflow_bill bill left outer join workflow_base base on bill.id=base.formid left outer join workflow_requestbase req on base.id=req.workflowid where req.requestid='" + requestId + "'";
        return CommonAPI.querySingleField(sql, "tablename");
    }

    /**
     * 查询某条请求的主表信息
     * 不知道数据库表单可以使用{@link #queryBillTableByRequest(int)}方法
     *
     * @param billTableName 数据库表单，必须为formtable_main_{num}格式
     *                      选择明细表请使用{@link #queryRequestDetailData(String, int, int)}方法
     * @param requestId     流程的请求id
     * @return 主表信息映射
     */
    static Map<String, String> queryRequestMainData(final String billTableName, final int requestId) {
        Assert.notEmpty(billTableName);
        Map<String, String> result = new HashMap<>();
        RecordSet rs = new RecordSet();
        String sql = "select * from " + billTableName + " where requestid = '" + requestId + "'";
        rs.execute(sql);
        if (!rs.next()) return result;
        return CommonAPI.mapFromRecordRow(rs);
    }

    /**
     * 查询某条请求的明细表信息
     * 不知道数据库表单可以使用{@link #queryBillTableByRequest(int)}方法
     *
     * @param billTableName 数据库表单，必须为formtable_main_{num}格式，无需写dt参数
     * @param requestId     流程的请求id
     * @param table         明细表从1开始，实际上就是表名_dt后面的数
     * @return 多行明细映射信息
     */
    static List<Map<String, String>> queryRequestDetailData(final String billTableName, final int requestId, final int table) {
        Assert.notEmpty(billTableName);
        List<Map<String, String>> result = new ArrayList<>();
        RecordSet rs = new RecordSet();

        // 获取主表id，用于明细表关联
        String sql = "select id from " + billTableName + " where requestid = '" + requestId + "'";
        rs.execute(sql);
        if (!rs.next()) return result;
        String mainid = rs.getString("id");

        // 查询明细表
        sql = "select * from " + billTableName + "_dt" + table + " where mainid= '" + mainid + "'";
        rs.execute(sql);
        while (rs.next()) {
            result.add(CommonAPI.mapFromRecordRow(rs));
        }
        return result;
    }

}
