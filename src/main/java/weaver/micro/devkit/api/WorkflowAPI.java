package weaver.micro.devkit.api;

import weaver.general.Util;
import weaver.interfaces.workflow.action.WorkflowToDoc;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.handler.StrictRecordSet;
import weaver.micro.devkit.util.ArrayUtil;
import weaver.soa.workflow.request.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 流程操作接口.
 * <hr/>
 * 注意: 因历史原因, 部分变量名可能会有多重含义, 例如:<br>
 * 1) BillId: 在本类中, billId一般指流程业务表的id, 也就是formId,
 * 在RequestManager类中, billId则代表当前流程在bill表中的id, 即本类中的mainId.
 *
 * <hr/>
 * 也许有用的对照表:<br>
 * 本类 | RequestManager<br>
 * BillId | FormId<br>
 * MainId | BillId<br>
 *
 * @author ruan4261
 */
public final class WorkflowAPI {

    static final String EMPTY = "";

    /** 流程中的签字意见类型映射 */
    static final Map<String, String> LOG_TYPE_MAPPER;

    static {
        Map<String, String> m = new HashMap<String, String>(32);
        m.put("0", "批准");
        m.put("1", "保存");
        m.put("2", "提交");
        m.put("3", "退回");
        m.put("4", "重新打开");
        m.put("5", "删除");
        m.put("6", "激活");
        m.put("7", "转发");
        m.put("9", "批注");
        m.put("a", "意见征询");
        m.put("b", "意见征询回复");
        m.put("h", "转办");
        m.put("i", "干预");
        m.put("j", "转办反馈");
        m.put("e", "强制归档");
        m.put("s", "督办");
        m.put("t", "抄送");
        LOG_TYPE_MAPPER = Collections.unmodifiableMap(m);
    }

    /**
     * 可能返回null值
     */
    public static String queryLogTypeMean(String type) {
        return LOG_TYPE_MAPPER.get(type);
    }

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
     * @deprecated 意义不明
     */
    @Deprecated
    public static List<Map<String, String>> queryRemarkList(final int requestId) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        StrictRecordSet rs = new StrictRecordSet();
        String sql = "select a.nodeid,b.nodename,a.logid,a.operator,a.logtype,a.remark,a.operatedate,a.operatetime" +
                " from workflow_requestLog a" +
                " left outer join workflow_nodebase b on a.nodeid=b.id" +
                " where a.requestid=" + requestId + " order by a.nodeid";
        rs.execute(sql);

        while (rs.next()) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("logid", rs.getString("logid"));// 签字日志id
            map.put("nodeid", rs.getString("nodeid"));// 节点id
            map.put("nodename", rs.getString("nodename"));// 节点名称
            map.put("operator", rs.getString("operator"));// 操作者的id
            map.put("logtype", LOG_TYPE_MAPPER.get(rs.getString("logtype")));// 操作类型
            map.put("remark", Util.delHtml(rs.getString("remark")));// 签字意见
            map.put("operatedate", rs.getString("operatedate"));// 操作日期
            map.put("operatetime", rs.getString("operatetime"));// 操作时间
            result.add(map);
        }
        return result;
    }

    /**
     * 新的签字意见列表获取方法
     * 排序按照时间从后往前, 可自定义字段查询
     *
     * @param expandFields 拓展字段, 只能来自workflow_requestLog表
     */
    public static List<Map<String, String>> queryRemarkListNew(int requestId, String[] expandFields) {
        StringBuilder enhance = new StringBuilder();
        if (expandFields != null && expandFields.length > 0) {
            for (String field : expandFields) {
                enhance.append(",a.")
                        .append(field);
            }
        }

        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute("select a.nodeid,b.nodename,a.logid,a.operator,a.logtype,a.remark," +
                "a.operatedate,a.operatetime" + enhance.toString() +
                " from workflow_requestLog a" +
                " left outer join workflow_nodebase b on a.nodeid=b.id" +
                " where a.requestid=" + requestId + " order by a.logid desc");
        while (rs.next()) {
            result.add(CommonAPI.mapFromRecordRow(rs));
        }

        return result;
    }

    /**
     * 获取表单字段数据库名称到id的映射
     *
     * @param billId  流程表单id
     * @param orderId 0：主表，大于0为明细表
     * @return 字段映射
     * @see #getFieldIdMapperByBillIdAndOrderId(int, int) 泛型优化
     */
    public static Map<String, String> queryFieldMapper(int billId, int orderId) {
        Map<String, String> result = new HashMap<String, String>();

        String sql = constructFieldMapperSql(billId, orderId);
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute(sql);
        while (rs.next()) {
            result.put(rs.getString("fieldname"), rs.getString("id"));
        }

        return result;
    }

    /**
     * 获取表单字段数据库名称到id的映射
     *
     * @param billId  表单id
     * @param orderId 明细表序号, 0代表主表
     */
    public static Map<String, Integer> getFieldIdMapperByBillIdAndOrderId(int billId, int orderId) {
        Map<String, Integer> result = new HashMap<String, Integer>();

        String sql = constructFieldMapperSql(billId, orderId);
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute(sql);
        while (rs.next()) {
            result.put(rs.getString("fieldname"), rs.getInt("id"));
        }

        return result;
    }

    /**
     * 获取表单字段数据库名称到id的映射
     *
     * @param workflowId 流程路径id
     * @param orderId    明细表序号, 0代表主表
     */
    public static Map<String, Integer> getFieldIdMapperByWorkflowIdAndOrderId(int workflowId, int orderId) {
        int billId = getBillIdByWorkflowId(workflowId);
        return getFieldIdMapperByBillIdAndOrderId(billId, orderId);
    }

    static String constructFieldMapperSql(int billId, int orderId) {
        String sql = "select id,fieldname from workflow_billfield where billid=" + billId;
        if (orderId == 0) {
            sql += " and (detailtable is null or detailtable = '') ";
        } else {
            String detailTableName = getDetailTableNameByBillIdAndOrderId(billId, orderId);
            sql += " and detailtable='" + detailTableName + "'";
        }
        return sql;
    }

    @Deprecated
    public static int getBillId(int workflowId) {
        return getBillIdByWorkflowId(workflowId);
    }

    public static int getBillIdByWorkflowId(int workflowId) {
        String sql = "select formid from workflow_base where id =" + workflowId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "formid"));
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
        return getBillTableNameByWorkflowId(workflowId);
    }

    /**
     * 流程存为文档.
     * 在E9系统中，接口是异步执行的，无法保证何时完成文档转换.
     * 调用本接口可实现同步操作.
     *
     * @param requestId 请求id
     * @return 是否成功
     */
    public static boolean workflowToDoc(final int requestId) {
        StrictRecordSet rs = new StrictRecordSet();
        StrictRecordSet rs2 = new StrictRecordSet();
        rs.execute("select workflowid,requestname,creater from workflow_requestbase where requestid =" + requestId);
        if (rs.next()) {
            String workflowid = rs.getString("workflowid");
            String requestname = rs.getString("requestname");
            String creater = rs.getString("creater");
            rs2.execute("select status from hrmresource where id=" + Util.getIntValue(creater));
            if (!rs2.next() || 5 == rs2.getInt("status"))
                return false;

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
        if (docid == -1)
            return EMPTY;

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
     * @see #queryRequestMainData(String, int)
     */
    public static Map<String, String> queryRequestMainData(int requestId) {
        String billTableName = queryBillTableByRequest(requestId);
        return queryRequestMainData(billTableName, requestId);
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
        Assert.notEmpty(billTableName, "billTableName");
        Map<String, String> result = new HashMap<String, String>();
        StrictRecordSet rs = new StrictRecordSet();
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
     * @see #queryRequestDetailData(String, int, int)
     */
    public static List<Map<String, String>> queryRequestDetailData(int requestId, int orderId) {
        String billTableName = queryBillTableByRequest(requestId);
        return queryRequestDetailData(billTableName, requestId, orderId);
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
        Assert.notEmpty(billTableName, "billTableName");
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        StrictRecordSet rs = new StrictRecordSet();

        // 通过billTableName及requestId获取mainid，用于明细表关联
        String sql = "select id from " + billTableName + " where requestid = '" + requestId + "'";
        rs.execute(sql);
        if (!rs.next()) return result;
        int mainid = rs.getInt("id");

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
        int nodeid = Util.getIntValue(
                CommonAPI.querySingleField(
                        "select nodeid from workflow_requestlog where logid=" + logId,
                        "nodeid"));
        if (nodeid == -1)
            return EMPTY;

        return CommonAPI.querySingleField(
                "select nodename from workflow_nodebase where id=" + nodeid,
                "nodename");
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
        String sql = constructSql2GetFieldIdByFieldName(billId, orderId, name);
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute(sql);
        if (!rs.next())
            throw new RuntimeException("No such field, input : billId=" + billId
                    + ", orderId=" + orderId + ", field(notExist)=" + name);

        return rs.getInt("id");
    }

    public static int getFieldIdByFieldNameQuietly(int billId, int orderId, String name) {
        String sql = constructSql2GetFieldIdByFieldName(billId, orderId, name);
        return Cast.o2Integer(CommonAPI.querySingleField(sql));
    }

    static String constructSql2GetFieldIdByFieldName(int billId, int orderId, String name) {
        String sql = "select id from workflow_billfield where fieldname='" + name + "' and billid=" + billId;
        if (orderId == 0)
            sql += " and (detailtable is null or detailtable = '') ";
        else {
            String detailTableName = getDetailTableNameByBillIdAndOrderId(billId, orderId);
            sql += " and detailtable='" + detailTableName + "'";
        }
        return sql;
    }

    /**
     * 获取表单下拉框的值
     *
     * @param fieldId  表单字段id
     * @param valueIdx 选择的value
     */
    public static String getDropdownBoxValue(int fieldId, int valueIdx) {
        String sql = constructSql2GetDropdownBoxValue(fieldId, valueIdx);
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute(sql);
        if (!rs.next())
            throw new RuntimeException("No such select item, input : fieldId=" + fieldId
                    + ", selectvalue=" + valueIdx);

        return rs.getString("selectname");
    }

    public static String getDropdownBoxValueQuietly(int fieldId, int valueIdx) {
        return CommonAPI.querySingleField(constructSql2GetDropdownBoxValue(fieldId, valueIdx));
    }

    static String constructSql2GetDropdownBoxValue(int fieldId, int valueIdx) {
        return "select selectname" +
                " from workflow_selectitem" +
                " where fieldid=" + fieldId +
                " and selectvalue=" + valueIdx;
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
        if (orderId == 0)
            return getBillTableNameByBillId(billId);

        return CommonAPI.querySingleField(
                "select tablename from workflow_billdetailtable where billid=" + billId
                        + " and orderid=" + orderId,
                "tablename");
    }

    public static String getDetailTableNameByBillTableNameAndOrderId(String billTableName, int orderId) {
        if (orderId == 0)
            return billTableName;

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

    /**
     * 获取某一流程当前节点id
     */
    public static int getNodeIdByRequestId(int requestId) {
        String sql = "select nownodeid from workflow_nownode where requestid=" + requestId;
        return Cast.o2Integer(CommonAPI.querySingleField(sql, "nownodeid"));
    }

    /**
     * 通过节点id获取节点名称
     */
    public static String getNodeNameByNodeId(int nodeId) {
        String sql = "select nodename from workflow_nodebase where id=" + nodeId;
        return CommonAPI.querySingleField(sql, "nodename");
    }

    /**
     * 获取流程当前的操作人<br>
     * 不分操作类型(会签, 抄送等), 不分是否已操作<br>
     * 返回数组已经去重
     */
    public static int[] getCurrentNodeOperatorByRequestId(int requestId) {
        int nodeId = getNodeIdByRequestId(requestId);
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute("select userid" +
                " from workflow_currentoperator" +
                " where groupdetailid in" +
                " (select id from workflow_groupdetail where groupid in" +
                " (select id from workflow_nodegroup where nodeid = " + nodeId + "))" +
                " and requestid = " + requestId);

        // result
        int[] users = new int[8];
        int idx = 0;// 实际长度
        while (rs.next()) {
            if (idx == users.length)
                users = ArrayUtil.arrayExtend(users, idx + 4);

            users[idx++] = rs.getInt("userid");
        }

        users = idx == users.length ? users : ArrayUtil.arrayExtend(users, idx);
        return (int[]) ArrayUtil.delRepeat(users);
    }

    /**
     * @deprecated 方法名称意义不明确
     */
    @Deprecated
    public static int getCreator(int requestId) {
        return getCreatorIdByRequestId(requestId);
    }

    /**
     * 获取流程创建人的hrmid
     *
     * @param requestId 流程唯一标识
     */
    public static int getCreatorIdByRequestId(int requestId) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select creater from workflow_requestbase where requestid=" + requestId,
                "creater"));
    }

    /**
     * 获取某流程在主表单中的id
     */
    public static int getMainId(int requestId) {
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select id from " + queryBillTableByRequest(requestId) + " where requestid=" + requestId,
                "id"));
    }

    public static void clearDetailTableDataByRequestIdAndOrder(int requestId, int order) {
        int mainId = getMainId(requestId);
        int billId = getBillIdByRequestId(requestId);
        String dt = getDetailTableNameByBillIdAndOrderId(billId, order);
        new StrictRecordSet().execute("delete from " + dt + " where mainid=" + mainId);
    }

    public static void clearAllDetailTableDataByRequestId(int requestId) {
        StrictRecordSet rs = new StrictRecordSet();
        int mainId = getMainId(requestId);// main table id
        int billId = getBillIdByRequestId(requestId);
        int[] orderSeq = getDetailTableOrderSequenceByFormId(billId);

        for (int orderId : orderSeq) {
            String dt = getDetailTableNameByBillIdAndOrderId(billId, orderId);
            rs.execute("delete from " + dt + " where mainid=" + mainId);
        }
    }

    public static int getDetailTableCountByWorkflowId(int workflowId) {
        int billId = getBillIdByWorkflowId(workflowId);
        return getDetailTableCountByFormId(billId);
    }

    public static int getDetailTableCountByRequestId(int requestId) {
        int billId = getBillIdByRequestId(requestId);
        return getDetailTableCountByFormId(billId);
    }

    /**
     * 返回值是最后一张明细表的orderId, 中间可能存在某个明细表不存在的情况
     */
    public static int getDetailTableCountByFormId(int formId) {
        return Cast.o2Integer(
                CommonAPI.querySingleField(
                        "select max(orderid) cnt from workflow_billdetailtable where billid=" + formId,
                        "cnt"),
                0);
    }

    public static int getDetailTableCountByWorkflowIdRealExist(int workflowId) {
        int billId = getBillIdByWorkflowId(workflowId);
        return getDetailTableCountByFormIdRealExist(billId);
    }

    public static int getDetailTableCountByRequestIdRealExist(int requestId) {
        int billId = getBillIdByRequestId(requestId);
        return getDetailTableCountByFormIdRealExist(billId);
    }

    /**
     * 返回真实存在的明细表数量, 返回值可能小于某张明细表的orderId
     */
    public static int getDetailTableCountByFormIdRealExist(int formId) {
        return Cast.o2Integer(
                CommonAPI.querySingleField(
                        "select count(orderid) cnt from workflow_billdetailtable where billid=" + formId,
                        "cnt"),
                0);
    }

    public static int[] getDetailTableOrderSequenceByWorkflowId(int workflowId) {
        int billId = getBillIdByWorkflowId(workflowId);
        return getDetailTableOrderSequenceByFormId(billId);
    }

    public static int[] getDetailTableOrderSequenceByRequestId(int requestId) {
        int billId = getBillIdByRequestId(requestId);
        return getDetailTableOrderSequenceByFormId(billId);
    }

    /**
     * 获取真实存在的明细表的order序列
     */
    public static int[] getDetailTableOrderSequenceByFormId(int formId) {
        int count = getDetailTableCountByFormIdRealExist(formId);
        int[] orderSeq = new int[count];
        int idx = 0;

        StrictRecordSet rs = new StrictRecordSet();
        rs.execute("select orderid from workflow_billdetailtable where billid=" + formId +
                " order by orderid asc");
        while (rs.next()) {
            if (idx == orderSeq.length) {
                // 并发问题: 其他程序新增明细表, 动态调整数组长度+1
                orderSeq = ArrayUtil.arrayExtend(orderSeq, orderSeq.length + 1);
            }
            orderSeq[idx++] = rs.getInt("orderid");
        }

        if (idx != orderSeq.length) {
            // 并发问题: 其他程序删除明细表, 动态调整数组长度
            orderSeq = ArrayUtil.arrayExtend(orderSeq, idx);
        }
        return orderSeq;
    }

    public static String queryDetailTableNameByWorkflowIdAndOrderId(int workflowId, int orderId) {
        if (orderId == 0)
            return getBillTableNameByWorkflowId(workflowId);

        return CommonAPI.querySingleField(
                "select a.tablename from workflow_billdetailtable a left outer join workflow_base b"
                        + " on a.billid=b.formid where b.id=" + workflowId + " and a.orderid=" + orderId,
                "tablename");
    }

    /**
     * <ul>
     *     <li>requestid > 0	创建流程成功，返回请求id</li>
     *     <li>requestid = -1	创建流程失败</li>
     *     <li>requestid = -2	用户没有流程创建权限</li>
     *     <li>requestid = -3	创建流程基本信息失败</li>
     *     <li>requestid = -4	保存表单主表信息失败</li>
     *     <li>requestid = -5	更新紧急程度失败</li>
     *     <li>requestid = -6	流程操作者失败</li>
     *     <li>requestid = -7	流转至下一节点失败</li>
     *     <li>requestid = -8	节点附加操作失败</li>
     * </ul>
     *
     * @param workflowId  发起的流程id
     * @param creator     流程发起人
     * @param title       系统流程标题 request name
     * @param isNext      是否直接流转至下一个节点
     * @param mainInfo    主表数据
     * @param detailInfos 明细表数据, 数组下标与明细表orderid对应, 从1开始,
     *                    任何元素可以为null, 下标为0的元素应该永远为空
     * @return requestId
     * @throws Exception 创建流程时可能出现的各种问题?
     * @since 1.1.4
     */
    public static int createWorkflow(int workflowId,
                                     int creator,
                                     String title,
                                     boolean isNext,
                                     Map<String, String> mainInfo,
                                     List<Map<String, String>>[] detailInfos) throws Exception {
        if (title == null) {
            String pathName = getWorkflowPathName(workflowId);
            String creatorName = creator == 1 ? "系统管理员" : HrmAPI.queryHrmName(creator);
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            title = pathName + "-" + creatorName + "-" + date;
        }

        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setCreatorid(String.valueOf(creator));// 流程创建人
        requestInfo.setWorkflowid(String.valueOf(workflowId));// 流程ID
        requestInfo.setDescription(title);// request name
        requestInfo.setIsNextFlow(isNext ? "1" : "0");// 是否提交下个节点 0: 否 | 非0: 是
        requestInfo.setRequestlevel("0");
        requestInfo.setRemindtype("0");

        // 主表信息录入
        MainTableInfo mainTableInfo = new MainTableInfo();
        requestInfo.setMainTableInfo(mainTableInfo);
        if (mainInfo != null) {
            Set<Map.Entry<String, String>> entrySet = mainInfo.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (key == null || key.equals("")
                        || value == null || value.equals(""))
                    continue;

                Property property = new Property();
                property.setName(key);
                property.setValue(value);
                mainTableInfo.addProperty(property);
            }
        }

        // 明细表信息录入
        DetailTableInfo detailTableInfos = new DetailTableInfo();
        requestInfo.setDetailTableInfo(detailTableInfos);
        if (detailInfos != null) {
            // 明细表1到明细表n, 下标0默认为null
            for (int i = 1; i < detailInfos.length; i++) {
                List<Map<String, String>> detailInfo = detailInfos[i];
                if (detailInfo != null) {
                    DetailTable detailTable = new DetailTable();
                    // 调用createRequest时需要设置此项
                    detailTable.setId(String.valueOf(i));// orderId
                    // 调用saveRequest方法时需要设置此项
                    //detailTable.setTableDBName("");

                    // 创建明细行
                    for (Map<String, String> detailRow : detailInfo) {
                        Row row = new Row();

                        // 构造明细行中每一列
                        Set<Map.Entry<String, String>> entrySet = detailRow.entrySet();
                        for (Map.Entry<String, String> entry : entrySet) {
                            String key = entry.getKey();
                            String value = entry.getValue();

                            if (key == null || key.equals("")
                                    || value == null || value.equals(""))
                                continue;

                            Cell cell = new Cell();
                            cell.setName(key);
                            cell.setValue(value);
                            row.addCell(cell);
                        }

                        detailTable.addRow(row);
                    }
                    detailTableInfos.addDetailTable(detailTable);
                }
            }
        }

        return Util.getIntValue(new RequestService().createRequest(requestInfo));
    }

    /**
     * 找到所有停留在某个节点的流程
     * <hr/>
     * 流程可能已经被删除, 请注意抑制相关异常
     *
     * @since 1.1.7
     */
    public static int[] findRequestOnNode(int nodeId) {
        int[] requests = new int[32];
        int idx = 0;
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute("select requestid from workflow_nownode where nownodeid='" + nodeId + "'");
        while (rs.next()) {
            if (idx >= requests.length) {
                if (requests.length == 0x7fffffff)
                    throw new RuntimeException("int overflow");

                int newLen = requests.length < 0x3fffffff ? requests.length << 1 : 0x7fffffff;
                requests = ArrayUtil.arrayExtend(requests, newLen);
            }

            int requestId = rs.getInt("requestid");
            requests[idx++] = requestId;
        }

        if (idx != requests.length)
            requests = ArrayUtil.arrayExtend(requests, idx);

        return requests;
    }

    /**
     * 获取所有人力资源在某节点待办数量
     * <hr/>
     * key: hrmId<br>
     * value: todoWorkflow count on this node
     *
     * @since 1.1.7
     */
    public static Map<Integer, Integer> findTodoCountOnNode(int node) {
        Map<Integer, Integer> m = new HashMap<Integer, Integer>();// key: hrm, value: to do workflow
        int[] requests = findRequestOnNode(node);
        for (int request : requests) {
            int[] operators = WorkflowAPI.getCurrentNodeOperatorByRequestId(request);
            for (int operator : operators) {
                Integer originCount = m.get(operator);
                if (originCount == null) {
                    m.put(operator, 1);
                } else {
                    m.put(operator, originCount + 1);
                }
            }
        }
        return m;
    }

    /**
     * 查询当前人员在指定节点的待办列表
     * 待办指当前人员需对流程进行操作且暂未操作的情况下(即当前人员对流程流转有一定作用)
     * 归档接收, 转发接收, 抄送接收等不算在内
     *
     * @return requestId数组
     */
    public static int[] findTodoRequestWithSpecialNodeAndUser(int uid, int node) {
        StrictRecordSet rs = new StrictRecordSet();
        rs.execute("select a.requestid\n" +
                "from workflow_currentoperator a\n" +
                "left outer join workflow_nownode b on a.requestid = b.requestid and a.nodeid = b.nownodeid\n" +
                "where a.userid = " + uid + " and b.nownodeid = " + node + " and a.isremark = 0");
        int[] ret = new int[10];
        int idx = 0;
        while (rs.next()) {
            if (idx == ret.length) {
                ret = ArrayUtil.arrayExtend(ret, idx + (idx >> 1));// floor(1.5 * length)
            }

            ret[idx++] = rs.getInt("requestid");
        }

        if (idx != ret.length) {
            ret = ArrayUtil.arrayExtend(ret, idx);
        }
        return ret;
    }

}
