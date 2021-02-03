package weaver.micro.devkit.api;

import weaver.conn.RecordSet;
import weaver.general.Util;
import weaver.interfaces.workflow.action.WorkflowToDoc;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.Cast;

import java.util.*;

/**
 * 流程操作工具。
 *
 * @author ruan4261
 */
public final class WorkflowAPI {
    static final String EMPTY = "";
    /** 流程中的签字意见类型映射 */
    static final Map<String, String> LOG_TYPE_MAPPER = new HashMap<String, String>() {
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
    public static String queryWorkflowTitle(final int requestId) {
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
     *         logtype 签字意见，已对应转化为确定类型，详情请查看{@link #LOG_TYPE_MAPPER}
     *         remark 签字意见
     *         operatedate 操作日期
     *         operatetime 操作时间
     */
    public static List<Map<String, String>> queryRemarkList(final int requestId) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        RecordSet rs = new RecordSet();
        String sql = "select a.nodeid,b.nodename,a.logid,a.operator,a.logtype,a.remark,a.operatedate,a.operatetime from workflow_requestLog a left outer join workflow_nodebase b on a.nodeid=b.id where a.requestid=" + requestId + " order by a.nodeid";
        rs.execute(sql);

        while (rs.next()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("logid", Util.null2String(rs.getString("logid")));// 签字日志id
            map.put("nodeid", Util.null2String(rs.getString("nodeid")));// 节点id
            map.put("nodename", Util.null2String(rs.getString("nodename")));// 节点名称
            map.put("operator", Util.null2String(rs.getString("operator")));// 操作者的id
            map.put("logtype", Util.null2String(LOG_TYPE_MAPPER.get(rs.getString("logtype"))));// 签字类型
            map.put("remark", Util.delHtml(Util.null2String(rs.getString("remark"))));// 签字意见
            map.put("operatedate", Util.null2String(rs.getString("operatedate")));// 操作日期
            map.put("operatetime", Util.null2String(rs.getString("operatetime")));// 操作时间
            result.add(map);
        }
        return result;
    }

    /**
     * 获取表单字段简写到id的映射
     * 只可获取表单名称为formtable_main_{billid}的表单及其明细表_dt{tableIdx}字段映射
     *
     * @param billId  流程表单id
     * @param orderId 0：主表，大于0为明细表
     * @return 字段映射
     */
    public static Map<String, String> queryFieldMapper(int billId, int orderId) {
        Map<String, String> result = new HashMap<String, String>();
        RecordSet rs = new RecordSet();

        String sql = "select id,fieldname from workflow_billfield where billid=-" + billId;
        if (orderId == 0)
            sql += " and (detailtable is null or detailtable = '') ";
        else {
            String detailTableName = getDetailTableNameByBillIdAndOrderId(billId, orderId);
            sql += " and detailtable='" + detailTableName + "'";
        }

        rs.execute(sql);
        while (rs.next()) {
            result.put(Util.null2String(rs.getString("fieldname")).toLowerCase(), Util.null2String(rs.getString("id")));
        }

        return result;
    }

    @Deprecated
    public static int getBillId(int workflowId) {
        return getBillIdByWorkflowId(workflowId);
    }

    public static int getBillIdByWorkflowId(int workflowId) {
        String sql = "select b.id from workflow_base a,workflow_bill b where a.formid=b.id and a.id =" + workflowId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "id"));
    }

    /** @param workflowId 流程id */
    @Deprecated
    public static String getBillTableName(int workflowId) {
        return getBillTableNameByWorkflowId(workflowId);
    }

    /**
     * 根据workflowid获取billTableName
     *
     * @param workflowId 流程id
     * @return 数据库表单名称
     */
    public static String getBillTableNameByWorkflowId(int workflowId) {
        String sql = "select b.tablename from workflow_base a,workflow_bill b where a.formid=b.id and a.id=" + workflowId;
        return CommonAPI.querySingleField(sql, "tablename");
    }

    /**
     * 根据workflowid获取billTableName
     *
     * @param workflowId 流程id
     * @return 数据库表单名称
     * @deprecated 修改方法名称使其更加语义化，见{@link #getBillTableName(int)}
     */
    @Deprecated
    public static String queryTableName(final int workflowId) {
        String sql = "select b.tablename from workflow_base a,workflow_bill b where a.formid=b.id and a.id=" + workflowId;
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
    public static boolean workflowToDoc(final int requestId) {
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
    public static String saveWorkflowHtml(final int requestId, final String path) {
        Assert.notEmpty(path, "path");

        String sql = "select max(id) id from DocDetail where fromworkflow = " + requestId;
        int docid = Util.getIntValue(CommonAPI.querySingleField(sql, "id"));
        if (docid == -1) return EMPTY;

        return DocAPI.saveDocLocally(Cast.o2Integer(docid), path, requestId + ".html", null);
    }

    /**
     * 根据请求查询billTable
     *
     * @param requestId 流程请求id
     */
    public static String queryBillTableByRequest(int requestId) {
        String sql = "select bill.tablename as tablename"
                + " from workflow_bill bill"
                + " left outer join workflow_base base on bill.id=base.formid"
                + " left outer join workflow_requestbase req on base.id=req.workflowid"
                + " where req.requestid=" + requestId;
        return CommonAPI.querySingleField(sql, "tablename");
    }

    public static int getBillIdByRequestId(int requestId) {
        String sql = "select base.formid as formid"
                + " from workflow_base base"
                + " left outer join workflow_requestbase req on base.id=req.workflowid"
                + " where req.requestid=" + requestId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "formid"));
    }

    public static int getWorkflowIdByRequestId(int requestId) {
        String sql = "select req.workflowid as workflowid"
                + " from workflow_requestbase req"
                + " where req.requestid=" + requestId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "workflowid"));
    }

    /**
     * 查询某条请求的主表信息
     * 不知道数据库表单可以使用{@link #queryBillTableByRequest(int)}方法，
     * 推荐使用{@link #getBillTableNameByBillId(int)}或{@link #getBillTableNameByWorkflowId(int)}
     *
     * @param billTableName 数据库表单，必须为formtable_main_{num}格式
     *                      选择明细表请使用{@link #queryRequestDetailData(String, int, int)}方法
     * @param requestId     流程的请求id
     * @return 主表信息映射
     */
    public static Map<String, String> queryRequestMainData(final String billTableName, final int requestId) {
        Assert.notEmpty(billTableName);
        Map<String, String> result = new HashMap<String, String>();
        RecordSet rs = new RecordSet();
        String sql = "select * from " + billTableName + " where requestid = '" + requestId + "'";
        rs.execute(sql);
        if (!rs.next()) return result;
        return CommonAPI.mapFromRecordRow(rs);
    }

    /**
     * @param billId    主表billId
     * @param requestId 请求标识
     */
    public static Map<String, String> getRequestMainTableData(int billId, int requestId) {
        String billTableName = getBillTableNameByBillId(billId);
        return queryRequestMainData(billTableName, requestId);
    }

    /**
     * 查询某条请求的明细表信息
     * 不知道数据库表单可以使用{@link #queryBillTableByRequest(int)}方法
     *
     * @param billTableName 数据库表单，必须为formtable_main_{num}格式，无需写dt参数
     * @param requestId     流程的请求id
     * @param orderId       明细表序号
     * @return 多行明细映射信息
     */
    public static List<Map<String, String>> queryRequestDetailData(String billTableName, int requestId, int orderId) {
        Assert.notEmpty(billTableName);
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        RecordSet rs = new RecordSet();

        // 通过billTableName及requestId获取mainid，用于明细表关联
        String sql = "select id from " + billTableName + " where requestid = '" + requestId + "'";
        rs.execute(sql);
        if (!rs.next()) return result;
        int mainid = Util.getIntValue(rs.getString("id"));

        // 获取明细表名
        String detailTableName = getDetailTableNameByBillTableNameAndOrderId(billTableName, orderId);

        // 查询明细表
        sql = "select * from " + detailTableName + " where mainid= '" + mainid + "'";
        rs.execute(sql);
        while (rs.next()) {
            result.add(CommonAPI.mapFromRecordRow(rs));
        }
        return result;
    }

    /**
     * @param billId    主表billId
     * @param requestId 请求标识
     * @param orderId   表序号，0未主表，其余为明细表序号
     */
    public static List<Map<String, String>> getRequestDetailData(int billId, int requestId, int orderId) {
        String billTableName = getBillTableNameByBillId(billId);
        return queryRequestDetailData(billTableName, requestId, orderId);
    }

    /**
     * 根据logid获取节点名称
     */
    public static String getNodeNameByLogId(int logId) {
        int nodeid = Util.getIntValue(CommonAPI.querySingleField("select nodeid from workflow_requestlog where logid=" + logId, "nodeid"));
        if (nodeid == -1) return EMPTY;
        return CommonAPI.querySingleField("select nodename from workflow_nodebase where id=" + nodeid, "nodename");
    }

    /**
     * 获取表单某字段的id
     *
     * @param billId  主表billId
     * @param orderId 0代表主表，其余代表明细表
     * @param name    表单字段数据库名
     * @return 表单字段id
     */
    public static int getFieldIdByFieldName(int billId, int orderId, String name) {
        RecordSet rs = new RecordSet();

        String sql = "select id from workflow_billfield where fieldname='" + name + "' and billid=" + billId;
        if (orderId == 0)
            sql += " and (detailtable is null or detailtable = '') ";
        else {
            String detailTableName = getDetailTableNameByBillIdAndOrderId(billId, orderId);
            sql += " and detailtable='" + detailTableName + "'";
        }

        rs.execute(sql);
        if (!rs.next())
            throw new RuntimeException("No such field, input : billId=" + billId + ", orderId=" + orderId + ", field(notExist)=" + name);

        return rs.getInt("id");
    }

    /**
     * 获取表单下拉框的值
     *
     * @param fieldId  表单字段id
     * @param valueIdx 选择的value
     */
    public static String getDropdownBoxValue(int fieldId, int valueIdx) {
        RecordSet rs = new RecordSet();
        rs.execute("select selectvalue,selectname from workflow_selectitem where fieldid=" + fieldId + " and selectvalue=" + valueIdx);
        if (!rs.next())
            throw new RuntimeException("No such select item, input : fieldId=" + fieldId + ", selectvalue=" + valueIdx);

        return rs.getString("selectname");
    }

    public static String getBillTableNameByBillId(int billId) {
        String sql = "select tablename from workflow_bill where id=" + billId;
        return CommonAPI.querySingleField(sql, "tablename");
    }

    public static int getBillIdByBillTableName(String billTableName) {
        String sql = "select id from workflow_bill where tablename='" + billTableName + "'";
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "id"));
    }

    /**
     * @param billId  主表billid
     * @param orderId 明细表序号，为0时取主表名
     */
    public static String getDetailTableNameByBillIdAndOrderId(int billId, int orderId) {
        if (orderId == 0) return getBillTableNameByBillId(billId);
        return CommonAPI.querySingleField("select tablename from workflow_billdetailtable where billid=" + billId + " and orderid=" + orderId, "tablename");
    }

    public static String getDetailTableNameByBillTableNameAndOrderId(String billTableName, int orderId) {
        if (orderId == 0) return billTableName;
        int billId = getBillIdByBillTableName(billTableName);
        return getDetailTableNameByBillIdAndOrderId(billId, orderId);
    }

    /**
     * 获取流程路径的名称
     *
     * @param workflowId 流程id, 表workflow_base中的id
     */
    public static String getWorkflowPathName(int workflowId) {
        String sql = "select workflowname from workflow_base where id=" + workflowId;
        return CommonAPI.querySingleField(sql, "workflowname");
    }
}
