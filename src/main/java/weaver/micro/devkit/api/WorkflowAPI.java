package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.micro.devkit.core.CacheBase;
import weaver.interfaces.workflow.action.WorkflowToDoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程操作工具。
 *
 * @author ruan4261
 */
public interface WorkflowAPI {

    String EMPTY = CacheBase.EMPTY;

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
    static String queryWorkflowTitle(final String requestId) {
        if (Util.getIntValue(requestId) == -1) return EMPTY;
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
     *         logtype 签字意见，已对应转化为确定类型，详情请查看{@link logTypeMapper}
     *         remark 签字意见
     *         operatedate 操作日期
     *         operatetime 操作时间
     */
    static List<Map<String, String>> queryRemarkList(final String requestId) {
        List<Map<String, String>> result = new ArrayList<>();
        if (Util.getIntValue(requestId) == -1) return result;

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
    static String queryDepartName(final String departId) {
        if (Util.getIntValue(departId) == -1) return EMPTY;
        String sql = "select departmentname from hrmdepartment where id = " + departId;
        return CommonAPI.querySingleField(sql, "departmentname");
    }

    /**
     * 获取表单字段简写到id的映射
     *
     * @param formid 主表id
     * @param num    0：主表，大于0为明细表
     * @return 字段映射
     */
    static Map<String, String> queryFieldMapper(int formid, final String num) {
        Map<String, String> result = new HashMap<>();
        formid = Math.abs(formid);
        RecordSet rs = new RecordSet();
        String sql;
        if ("0".equals(num)) {
            sql = "select b.id,fieldname,detailtable from workflow_billfield b ,workflow_base a where b.billid=-"
                    + formid
                    + " and a.formid=b.billid and (detailtable is null or detailtable = '') ";
        } else {
            sql = "select b.id,fieldname,detailtable from workflow_billfield b ,workflow_base a where b.billid=-"
                    + formid
                    + " and a.formid=b.billid and detailtable='formtable_main_"
                    + formid + "_dt" + num + "'";
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
    static String queryTableName(final String workflowId) {
        if (Util.getIntValue(workflowId) == -1) return EMPTY;
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
    static boolean workflowToDoc(final Integer requestId) {
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
     * @param requestid  请求id
     * @param workflowid 流程id
     * @param path       保存路径
     * @return 保存的全路径
     */
    static String saveWorkflowHtml(final String requestid, final String workflowid, final String path) {
        String sql = "select seccategoryid from workflowtodocprop where workflowid =" + workflowid;
        String docfolderid = CommonAPI.querySingleField(sql, "seccategoryid");
        if (EMPTY.equals(docfolderid)) return EMPTY;

        sql = "select id from DocDetail where seccategory = " + docfolderid + " and fromworkflow = " + requestid + " order by id desc";
        String docid = CommonAPI.querySingleField(sql, "id");
        if (EMPTY.equals(docid)) return EMPTY;

        return DocAPI.saveDocLocally(docid, path, requestid + ".html", null);
    }
}
