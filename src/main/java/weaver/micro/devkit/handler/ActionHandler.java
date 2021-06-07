package weaver.micro.devkit.handler;

import weaver.conn.RecordSetTrans;
import weaver.interfaces.workflow.action.Action;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.api.CommonAPI;
import weaver.micro.devkit.api.DocAPI;
import weaver.micro.devkit.api.WorkflowAPI;
import weaver.micro.devkit.util.StringUtils;
import weaver.soa.workflow.request.*;
import weaver.workflow.request.RequestManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程对象RequestInfo处理器。
 *
 * <h1>特别注意：在Ecology系统中，Action为无状态模型，使用单例控制！请注意自行控制内部各实例属性的创建销毁！</h1>
 *
 * @author ruan4261
 */
public abstract class ActionHandler implements Handler, Action, Loggable {

    /**
     * 该实例被执行次数
     */
    private int instanceRunTimes;

    /**
     * 每次的请求
     */
    private RequestInfo request;

    /**
     * 接口说明信息
     */
    private final String actionInfo;

    /**
     * 当前接口的返回值
     */
    private String endResult;

    /**
     * 当前接口的返回信息
     */
    private String endMessage;

    /**
     * 主表缓存
     */
    private Map<String, String> mainTableCache;

    /**
     * 明细表缓存
     */
    private Map<Integer, List<Map<String, String>>> detailTableListCache;

    /**
     * 明细表缓存，原型
     */
    private DetailTable[] detailTablesCache;

    /**
     * 字段校验成功标识
     */
    private boolean fieldVerifyFlag;

    /**
     * log process
     */
    private final Loggable logProcess;

    /**
     * log prefix
     */
    private String logPrefix;

    /**
     * 临时加入, 用于解决并发问题, 为true时代表该实例可执行action
     *
     * @since 1.1.6
     */
    private boolean realExecutor = false;

    {
        this.instanceRunTimes = 0;
        this.logPrefix = "ActionHandler(Initial)";
    }

    public ActionHandler(String actionInfo) {
        this.actionInfo = actionInfo;
        this.logProcess = LogEventProcessor.getInstance(this.actionInfo).setUsedLevel(2);
    }

    public ActionHandler() {
        this.actionInfo = this.getClass().getName();
        this.logProcess = LogEventProcessor.getInstance(this.actionInfo).setUsedLevel(2);
    }

    public RequestInfo requestInfo() {
        return this.request;
    }

    public RequestManager requestManager() {
        return this.request.getRequestManager();
    }

    /**
     * 获取流程主表数据，这是一个缓存值。
     * 如果在获取了该映射表后，对数据库中该流程信息进行了修改，再次调用此方法无法获取更新的信息。
     * 这个方法能满足大部分需求，如需获取最新信息请使用{@link #getMainTableNewest()}
     * 请注意NULL值处理。
     *
     * @return 流程主表对应的单行数据，字段名到流程数据的映射。
     */
    public Map<String, String> getMainTableCache() {
        if (this.mainTableCache != null)
            return this.mainTableCache;
        else this.mainTableCache = new HashMap<String, String>();

        Property[] properties = this.request.getMainTableInfo().getProperty();
        for (Property property : properties) {
            this.mainTableCache.put(property.getName(), property.getValue());
        }

        this.logLine("MAIN FORM DATA(Cache) : " + this.mainTableCache.toString());
        return this.mainTableCache;
    }

    /**
     * 获取明细表数据，这是一个缓存值。
     * 如果在获取了该映射表后，对数据库中该流程信息进行了修改，再次调用此方法无法获取更新的信息。
     * 这个方法能满足大部分需求，如需获取最新信息请使用{@link #getDetailTableNewest(int)} ()}
     * 请注意NULL值处理。
     *
     * @param table 明细表序号，从1开始
     * @return 明细表下标！流程第(tableIdx + 1)个明细表的对应多行数据，字段名到流程数据的映射，数据值可能为NULL。
     */
    public List<Map<String, String>> getDetailTableCache(int table) {
        if (this.detailTableListCache == null)
            this.detailTableListCache = new HashMap<Integer, List<Map<String, String>>>(8);

        List<Map<String, String>> data = this.detailTableListCache.get(table);
        if (data != null) return data;
        else {
            data = new ArrayList<Map<String, String>>();
            this.detailTableListCache.put(table, data);
        }

        if (this.detailTablesCache == null)
            this.detailTablesCache = this.request.getDetailTableInfo().getDetailTable();

        DetailTable dt = this.detailTablesCache[table - 1];
        Row[] rows = dt.getRow();
        for (Row row : rows) {
            Cell[] cells = row.getCell();
            Map<String, String> map = new HashMap<String, String>();
            for (Cell cell : cells) {
                map.put(cell.getName(), cell.getValue());
            }
            data.add(map);
        }

        this.logLine("DETAIL(dt_" + table + ") FORM DATA(Cache) : " + data.toString());
        return data;
    }

    /**
     * 通过数据库查询获取最新流程主表信息
     */
    public Map<String, String> getMainTableNewest() {
        Map<String, String> res =
                WorkflowAPI.queryRequestMainData(this.getBillTableName(), this.getRequestId());
        this.logLine("MAIN FORM DATA(Newest) : " + res.toString());
        return res;
    }

    /**
     * 通过数据库查询获取最新流程明细表信息
     *
     * @param table 明细表序号，从1开始
     */
    public List<Map<String, String>> getDetailTableNewest(int table) {
        List<Map<String, String>> res =
                WorkflowAPI.queryRequestDetailData(this.getBillTableName(), this.getRequestId(), table);
        this.logLine("DETAIL(dt_" + table + ") FORM DATA(Newest) : " + res.toString());
        return res;
    }

    /**
     * @return 流程签字意见列表
     * @see WorkflowAPI#queryRemarkListNew(int, String[])
     */
    public List<Map<String, String>> getRemarkList() {
        List<Map<String, String>> res = WorkflowAPI.queryRemarkListNew(this.getRequestId(), null);
        this.logLine("Remark list : " + res.toString());
        return res;
    }

    /**
     * @param expandFields 自定义字段
     * @return 流程签字意见列表
     * @see WorkflowAPI#queryRemarkListNew(int, String[])
     */
    public List<Map<String, String>> getRemarkList(String[] expandFields) {
        List<Map<String, String>> res = WorkflowAPI.queryRemarkListNew(this.getRequestId(), expandFields);
        this.logLine("Remark list : " + res.toString());
        return res;
    }

    /**
     * @return 流程相关最新文档
     * @see DocAPI#queryDocIdByRequestId(int)
     */
    public String getDocIdLatest() {
        String docId = DocAPI.queryDocIdByRequestId(this.getRequestId());
        this.log("Newest document id : " + docId);
        return docId;
    }

    /**
     * 获取当前流程在主表单中的id
     */
    public int getMainId() {
        return WorkflowAPI.getMainId(this.getRequestId());
    }

    public int getRequestId() {
        return Cast.o2Integer(this.request.getRequestid());
    }

    /** 流程标题 */
    public String getRequestName() {
        return this.request.getRequestManager().getRequestname();
    }

    /**
     * 流程路径的id
     */
    public int getWorkflowId() {
        return Cast.o2Integer(this.request.getWorkflowid());
    }

    /**
     * 这个接口在流转时并不准确, 大概率获取到的是流程流转之后的节点
     */
    public int getCurrentNodeId() {
        return WorkflowAPI.getNodeIdByRequestId(this.getRequestId());
    }

    public String getCurrentNodeName() {
        int nodeId = this.getCurrentNodeId();
        return WorkflowAPI.getNodeNameByNodeId(nodeId);
    }

    /**
     * 流程路径名称
     */
    public String getWorkflowPathName() {
        String workflowPath = WorkflowAPI.getWorkflowPathName(this.getWorkflowId());
        this.log("workflow path: " + workflowPath);
        return workflowPath;
    }

    public int getBillId() {
        return WorkflowAPI.getBillIdByWorkflowId(this.getWorkflowId());
    }

    /**
     * 所有流程打印的日志前缀为 $request:?$
     * 用于便捷查询
     */
    private String getLogPrefix() {
        return this.logPrefix;
    }

    public final int getCreatorId() {
        return WorkflowAPI.getCreatorIdByRequestId(this.getRequestId());
    }

    /**
     * 可能返回null
     */
    public RecordSetTrans getRsTrans() {
        return this.request.getRsTrans();
    }

    @Deprecated
    public String getTableNameLower() {
        return this.getBillTableName().toLowerCase();
    }

    @Deprecated
    public String getTableNameUpper() {
        return this.getBillTableName().toUpperCase();
    }

    /** 明细order从1开始, 为0时返回主表名称, 不存在该明细表则会抛出运行时异常 */
    public String getDetailTableName(int order) {
        String name = WorkflowAPI.getDetailTableNameByBillIdAndOrderId(getBillId(), order);
        if ("".equals(name))
            throw new RuntimeException("BillTable [" + getBillTableName() + "] doesn't have such detail table which order number is " + order + ".");
        return name;
    }

    public String getBillTableName() {
        return WorkflowAPI.getBillTableNameByWorkflowId(this.getWorkflowId());
    }

    /**
     * full recursion
     */
    @Override
    public final void log(Object o) {
        this.logLine(StringUtils.fullRecursionPrint(o));
    }

    @Override
    public final void log(String msg) {
        this.logProcess.log(this.getLogPrefix() + msg);
    }

    /**
     * 打印实际内容时另起一行
     */
    public final void logLine(String msg) {
        this.logProcess.log(this.getLogPrefix() + "\n" + msg);
    }

    @Override
    public final void log(Throwable cause) {
        this.logProcess.log(this.getLogPrefix(), cause);
    }

    /**
     * @since 1.0.5
     */
    @Override
    public final void log(String msg, Throwable cause) {
        this.logProcess.log(this.getLogPrefix() + msg, cause);
    }

    /**
     * 流程退回
     *
     * @param msg 前端显示信息
     * @return FAILURE_AND_CONTINUE
     */
    public final String fail(String msg) {
        String mes = this.actionInfo + " :: " + msg;
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(mes);
        this.endResult = Action.FAILURE_AND_CONTINUE;
        this.endMessage = mes;
        return Action.FAILURE_AND_CONTINUE;
    }

    public final String fail(Throwable throwable) {
        String mes = this.actionInfo + " :: " + throwable.toString();
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(mes);
        this.endResult = Action.FAILURE_AND_CONTINUE;
        this.endMessage = mes;
        return Action.FAILURE_AND_CONTINUE;
    }

    /**
     * 将流程打回, 不显示参考信息
     *
     * @param msg 前端显示信息
     */
    public final String failWithOnlyMessage(String msg) {
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(msg);
        this.endResult = Action.FAILURE_AND_CONTINUE;
        this.endMessage = msg;
        return Action.FAILURE_AND_CONTINUE;
    }

    /** 接口执行完毕 */
    public final String success() {
        this.endResult = Action.SUCCESS;
        this.endMessage = "OK";
        return Action.SUCCESS;
    }

    public final void actionStart() throws Throwable {
        this.logPrefix = "$request:" + this.getRequestId() + "$ -> ";
        this.log(" action start" +
                ", bill main id is " + this.getMainId() +
                ", bill table name is " + this.getTableNameLower() +
                ", request name is " + this.getRequestName() +
                ", creator is " + this.getCreatorId() +
                ", current node is " + this.getCurrentNodeName() +
                ", run times of this action instance is " + (++this.instanceRunTimes));
    }

    /**
     * 自定义解耦
     */
    public void init() {
    }

    /** 每次执行execute结束时必然执行此方法 */
    public final void end() {
        this.log(" end with result:" + this.endResult + ", message:" + this.endMessage);
        try {
            this.clearCache();
        } catch (Throwable e) {
            // action is over, exception cannot be output to the front end
            this.ifExceptionAfterEnd(e);
        }
    }

    /**
     * 可以通过复写该方法的方式取消缓存清理
     * 但请注意控制实例的作用域(E8,9默认使用单例)
     */
    public void clearCache() throws Throwable {
        this.request = null;
        this.endResult = null;
        this.endMessage = null;
        this.mainTableCache = null;
        this.detailTableListCache = null;
        this.detailTablesCache = null;
        this.log("Auto clear cache success.");
        this.logPrefix = "";
    }

    /** 发生异常情况下在action结束时执行，该方法用于自定义重写 */
    public String ifException(Throwable e) {
        this.log("Auto catch exception by ActionHandler.", e);
        return this.fail(e);
    }

    /**
     * action执行结束后抛出的异常处理
     *
     * @since 1.1.2
     */
    public void ifExceptionAfterEnd(Throwable e) {
        this.log("Throw exception through #end()", e);
    }

    /**
     * 校验字段长度<br>
     * 正常情况下返回常量池中的EMPTY空字符串<br>
     * 如字段超长则会返回字段显示名(从1.0.4版本开始不自动添加引号)
     * case:
     * <ul>
     * <li>1.如无法获取该字段，返回空字符串</li>
     * <li>2.字段在长度限制内，返回空字符串</li>
     * <li>3.字段超长，不通过校验，返回信息</li>
     * </ul>
     *
     * @param table     为0时选择主表字段，非0时为明细表序号
     * @param field     字段数据库名
     * @param maxlength 限制长度
     * @return 字符串信息
     */
    public String fieldLengthLimit(int table, String field, int maxlength) {
        if (table < 0)
            return "";

        boolean overLimit = false;
        if (table == 0) {
            // 主表
            Map<String, String> mainTable = this.getMainTableCache();
            String v = mainTable.get(field);
            if (v.length() > maxlength)
                overLimit = true;
        } else {
            // 明细表
            List<Map<String, String>> detailTable = this.getDetailTableCache(table);
            for (Map<String, String> map : detailTable) {
                String v = map.get(field);
                if (v.length() > maxlength) {
                    overLimit = true;
                    break;
                }
            }
        }

        if (!overLimit)
            return "";
        // 通过数据库字段名获取显示字段名
        String sql = "select indexdesc from htmllabelindex a left outer join workflow_billfield b on a.id=b.fieldlabel where b.fieldname='" + field + "' and b.billid='" + getBillId() + "'";
        return CommonAPI.querySingleField(sql, "indexdesc");
    }

    @Override
    public final String execute(RequestInfo requestInfo) {
        // 并发问题解决(临时), 只有realExecutor为true时代表当前实例安全, 可用于实际action处理
        if (!this.realExecutor) {
            log("ActionHandlerRouter#" + StringUtils.toStringNative(this)
                    + " :: " + StringUtils.toStringNative(requestInfo));
            try {
                return executeInNewHandler(requestInfo);
            } catch (Throwable t) {
                log(t);
                return this.fail("ActionHandler#executeInNewHandler exception: " + t.toString());
            }
        }

        this.request = requestInfo;
        try {
            this.actionStart();
            // 字段校验
            String mes = this.fieldVerify();
            if (!this.fieldVerifyFlag)
                return this.failWithOnlyMessage(mes);

            // 正常流程
            this.init();
            return this.handle(requestInfo);
        } catch (Throwable e) {
            return this.ifException(e);
        } finally {
            this.end();
        }
    }

    /**
     * @param tableIdx 0代表主表，其余代表明细表
     * @param name     字段数据库名
     * @return 字段id
     * @deprecated 错拼
     */
    @Deprecated
    public int getFiledId(int tableIdx, String name) {
        return this.getFieldId(tableIdx, name);
    }

    /**
     * @param tableIdx 0代表主表，其余代表明细表
     * @param name     字段数据库名
     * @return 字段id
     */
    public int getFieldId(int tableIdx, String name) {
        return WorkflowAPI.getFieldIdByFieldName(this.getBillId(), tableIdx, name);
    }

    /**
     * @param tableIdx 0代表主表，其余代表明细表
     * @param name     字段数据库名
     * @return 字段显示值
     * @deprecated cannot get detail table values
     */
    @Deprecated
    public String getDropdownBoxValue(int tableIdx, String name) {
        // tableIdx can only be zero
        int fieldId = this.getFieldId(tableIdx, name);
        int fieldValue = Cast.o2Integer(this.getMainTableCache().get(name));
        return WorkflowAPI.getDropdownBoxValue(fieldId, fieldValue);
    }

    /**
     * 明细表有多行, 需要指定值
     *
     * @param tableIdx 0代表主表，其余代表明细表
     * @param name     字段数据库名
     * @param value    字段下标值
     * @return 字段显示值
     */
    public String getDropdownBoxShowValue(int tableIdx, String name, int value) {
        int fieldId = this.getFieldId(tableIdx, name);
        return WorkflowAPI.getDropdownBoxValue(fieldId, value);
    }

    /**
     * 请覆盖本方法，通过{@link #setFieldVerifyFlag(boolean)}方法设置校验成功与否
     * 如果{@link #fieldVerifyFlag}为true则通过校验，如果为false则此方法应该返回信息用于显示给用户
     */
    public String fieldVerify() {
        // 该方法用于被覆盖
        setFieldVerifyFlag(true);
        return "";
    }

    public void setFieldVerifyFlag(boolean fieldVerifyFlag) {
        this.fieldVerifyFlag = fieldVerifyFlag;
    }

    /**
     * 获取明细表数量
     */
    public int getDetailTableCount() {
        int count = WorkflowAPI.getDetailTableCountByRequestId(getRequestId());
        this.log("detail table count: " + count);
        return count;
    }

    /**
     * 清除指定明细表数据
     */
    public void clearDetailTableData(int order) {
        WorkflowAPI.clearDetailTableDataByRequestIdAndOrder(getRequestId(), order);
        this.log("The detail table " + order + " has been cleaned.");
    }

    /**
     * 清除所有明细表数据
     */
    public void clearAllDetailTableData() {
        WorkflowAPI.clearAllDetailTableDataByRequestId(getRequestId());
        this.log("All detail tables have been cleaned.");
    }

    public void setRealExecutor() {
        this.realExecutor = true;
    }

    /**
     * @since 1.1.6
     */
    private String executeInNewHandler(RequestInfo requestInfo) throws IllegalAccessException, InstantiationException {
        Class<? extends ActionHandler> clazz = this.getClass();
        ActionHandler newHandler = clazz.newInstance();
        newHandler.setRealExecutor();
        log("RealExecutor#" + StringUtils.toStringNative(newHandler)
                + " :: " + StringUtils.toStringNative(requestInfo));
        return newHandler.execute(requestInfo);
    }

}