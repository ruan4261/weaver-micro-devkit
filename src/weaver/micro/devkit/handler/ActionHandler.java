package weaver.micro.devkit.handler;

import weaver.conn.RecordSetTrans;
import weaver.interfaces.workflow.action.Action;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.annotation.Autowired;
import weaver.micro.devkit.annotation.NotNull;
import weaver.micro.devkit.api.DocAPI;
import weaver.micro.devkit.api.WorkflowAPI;
import weaver.micro.devkit.util.ArrayUtils;
import weaver.micro.devkit.util.ReflectUtils;
import weaver.micro.devkit.util.StringUtils;
import weaver.micro.devkit.util.VisualPrintUtils;
import weaver.soa.workflow.request.*;
import weaver.workflow.request.RequestManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程流转集成基类.
 * <hr/>
 * 在Ecology系统中的流程流转集成功能中, Action为无状态(单例)模型.<br>
 * 请注意控制内部各实例属性的创建销毁.<br>
 * <br>
 * 在1.1.6版本添加的并发控制中, 此类实例已被设置为默认一实例仅执行一次.
 * 如需使用并发实例模型, 可以在构造函数中调用{@link #setRealExecutor()}.
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

    protected RequestInfo requestInfo() {
        return this.request;
    }

    protected RequestManager requestManager() {
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
    protected Map<String, String> getMainTableCache() {
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
    protected List<Map<String, String>> getDetailTableCache(int table) {
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

        this.logLine("DETAIL(dt_" + table + ") FORM DATA(Cache) : " + data);
        return data;
    }

    /**
     * 通过数据库查询获取最新流程主表信息
     */
    protected Map<String, String> getMainTableNewest() {
        Map<String, String> res = WorkflowAPI.queryRequestMainData(this.getRequestId());
        this.logLine("MAIN FORM DATA(Newest) : " + res);
        return res;
    }

    /**
     * 通过数据库查询获取最新流程明细表信息
     *
     * @param table 明细表序号，从1开始
     */
    protected List<Map<String, String>> getDetailTableNewest(int table) {
        List<Map<String, String>> res = WorkflowAPI.queryRequestDetailData(this.getRequestId(), table);
        this.logLine("DETAIL(dt_" + table + ") FORM DATA(Newest) : " + res);
        return res;
    }

    /**
     * @return 流程签字意见列表
     * @see WorkflowAPI#queryRemarkListNew(int, String[])
     */
    protected List<Map<String, String>> getRemarkList() {
        List<Map<String, String>> res = WorkflowAPI.queryRemarkListNew(this.getRequestId(), null);
        this.logLine("Remark list : " + res);
        return res;
    }

    /**
     * @param expandFields 自定义字段
     * @return 流程签字意见列表
     * @see WorkflowAPI#queryRemarkListNew(int, String[])
     */
    protected List<Map<String, String>> getRemarkList(String[] expandFields) {
        List<Map<String, String>> res = WorkflowAPI.queryRemarkListNew(this.getRequestId(), expandFields);
        this.logLine("Remark list : " + res);
        return res;
    }

    /**
     * @return 流程相关最新文档
     * @see DocAPI#queryDocIdByRequestId(int)
     * @deprecated 请使用#getDocIdLatest()
     */
    @Deprecated
    protected String getDocIdLatest() {
        String docId = DocAPI.queryDocIdByRequestId(this.getRequestId());
        this.log("Newest document id : " + docId);
        return docId;
    }

    /**
     * @see #getDocIdLatest() 字符串返回值版本
     */
    protected int getDocIdLatestNew() {
        int docId = DocAPI.getDocIdByRequestId(this.getRequestId());
        this.log("Newest document id : " + docId);
        return docId;
    }

    /**
     * 获取当前流程在主表单中的id
     */
    protected int getMainId() {
        return WorkflowAPI.getMainId(this.getRequestId());
    }

    protected int getRequestId() {
        return Cast.o2Integer(this.request.getRequestid());
    }

    /** 流程标题 */
    protected String getRequestName() {
        return this.request.getRequestManager().getRequestname();
    }

    /**
     * 流程路径的id
     */
    protected int getWorkflowId() {
        return Cast.o2Integer(this.request.getWorkflowid());
    }

    /**
     * 这个接口在流转时并不准确, 大概率获取到的是流程流转之后的节点
     */
    protected int getCurrentNodeId() {
        return WorkflowAPI.getNodeIdByRequestId(this.getRequestId());
    }

    protected String getCurrentNodeName() {
        int nodeId = this.getCurrentNodeId();
        return WorkflowAPI.getNodeNameByNodeId(nodeId);
    }

    /**
     * 流程路径名称
     */
    protected String getWorkflowPathName() {
        String workflowPath = WorkflowAPI.getWorkflowPathName(this.getWorkflowId());
        this.log("workflow path: " + workflowPath);
        return workflowPath;
    }

    protected int getBillId() {
        return WorkflowAPI.getBillIdByWorkflowId(this.getWorkflowId());
    }

    /**
     * 所有流程打印的日志前缀为 $request:?$
     * 用于便捷查询
     */
    protected String getLogPrefix() {
        return this.logPrefix;
    }

    protected int getCreatorId() {
        return WorkflowAPI.getCreatorIdByRequestId(this.getRequestId());
    }

    /**
     * 可能返回null
     */
    protected RecordSetTrans getRsTrans() {
        return this.request.getRsTrans();
    }

    @Deprecated
    protected String getTableNameLower() {
        return this.getBillTableName().toLowerCase();
    }

    @Deprecated
    protected String getTableNameUpper() {
        return this.getBillTableName().toUpperCase();
    }

    /** 明细order从1开始, 为0时返回主表名称, 不存在该明细表则会抛出运行时异常 */
    protected String getDetailTableName(int order) {
        String name = WorkflowAPI.getDetailTableNameByBillIdAndOrderId(getBillId(), order);
        if ("".equals(name))
            throw new RuntimeException("BillTable [" + getBillTableName() + "] doesn't have such detail table which order number is " + order + ".");
        return name;
    }

    protected String getBillTableName() {
        return WorkflowAPI.getBillTableNameByWorkflowId(this.getWorkflowId());
    }

    /**
     * full recursion
     */
    @Override
    public void log(Object o) {
        String tree = VisualPrintUtils.getPrintInfo(o);
        this.logProcess.log(this.getLogPrefix() + '\n' + tree);
    }

    @Override
    public void log(String msg) {
        this.logProcess.log(this.getLogPrefix() + msg);
    }

    /**
     * 打印实际内容时另起一行
     */
    public void logLine(String msg) {
        this.logProcess.log(this.getLogPrefix() + '\n' + msg);
    }

    @Override
    public void log(Throwable cause) {
        this.logProcess.log(this.getLogPrefix(), cause);
    }

    /**
     * @since 1.0.5
     */
    @Override
    public void log(String msg, Throwable cause) {
        this.logProcess.log(this.getLogPrefix() + msg, cause);
    }

    /**
     * 流程退回
     *
     * @param msg 前端显示信息
     * @return FAILURE_AND_CONTINUE
     */
    protected final String fail(String msg) {
        String mes = this.actionInfo + " :: " + msg;
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(mes);
        this.endResult = Action.FAILURE_AND_CONTINUE;
        this.endMessage = mes;
        return Action.FAILURE_AND_CONTINUE;
    }

    protected final String fail(Throwable throwable) {
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
    protected final String failWithOnlyMessage(String msg) {
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(msg);
        this.endResult = Action.FAILURE_AND_CONTINUE;
        this.endMessage = msg;
        return Action.FAILURE_AND_CONTINUE;
    }

    /** 接口执行完毕 */
    protected final String success() {
        this.endResult = Action.SUCCESS;
        this.endMessage = "OK";
        return Action.SUCCESS;
    }

    protected final void actionStart() throws Throwable {
        this.logPrefix = "$request:" + this.getRequestId() + "$ -> ";
        this.log(" action start" +
                ", bill main id is " + this.getMainId() +
                ", bill table name is " + this.getBillTableName() +
                ", request name is " + this.getRequestName() +
                ", creator is " + this.getCreatorId() +
                ", current node is " + this.getCurrentNodeName() +
                ", run times of this action instance is " + (++this.instanceRunTimes));
    }

    /**
     * 自定义解耦
     */
    protected void init() {
    }

    /** 每次执行execute结束时必然执行此方法 */
    protected final void end() {
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
    protected void clearCache() throws Throwable {
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
    protected String ifException(Throwable t) {
        return this.fail(t);
    }

    /**
     * action执行结束后抛出的异常处理
     *
     * @since 1.1.2
     */
    protected void ifExceptionAfterEnd(Throwable t) {
        this.log("Throw exception through #end()", t);
    }

    @Override
    public final String execute(RequestInfo requestInfo) {
        // 流程流转集成中的接口为单例, 为解决并发问题, 设置 realExecutor 变量
        // 只有 realExecutor 为 true 时代表当前实例安全, 可用于实际action处理
        if (!this.realExecutor) {
            log("ActionHandlerAgent#" + StringUtils.toStringNative(this)
                    + " :: " + StringUtils.toStringNative(requestInfo));
            try {
                return executeInNewHandler(requestInfo);
            } catch (Throwable t) {
                log("ActionHandler#executeInNewHandler", t);
                return this.fail("ActionHandler#executeInNewHandler exception: " + t);
            }
        }

        this.request = requestInfo;
        try {
            this.actionStart();
            this.init();
            // 检查参数注入, 包含注解 @NotNull 的变量为空时会引发异常
            this.checkRequiredArguments();

            return this.handle(requestInfo);
        } catch (Throwable t) {
            this.log("Auto catch exception by ActionHandler.", t);
            return this.ifException(t);
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
    protected int getFiledId(int tableIdx, String name) {
        return this.getFieldId(tableIdx, name);
    }

    /**
     * @param tableIdx 0代表主表，其余代表明细表
     * @param name     字段数据库名
     * @return 字段id
     */
    protected int getFieldId(int tableIdx, String name) {
        return WorkflowAPI.getFieldIdByFieldName(this.getBillId(), tableIdx, name);
    }

    /**
     * @param tableIdx 0代表主表，其余代表明细表
     * @param name     字段数据库名
     * @return 字段显示值
     * @deprecated cannot get detail table values
     */
    @Deprecated
    protected String getDropdownBoxValue(int tableIdx, String name) {
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
    protected String getDropdownBoxShowValue(int tableIdx, String name, int value) {
        int fieldId = this.getFieldId(tableIdx, name);
        return WorkflowAPI.getDropdownBoxValue(fieldId, value);
    }

    /**
     * 获取明细表数量, 返回值为最后一张明细表的orderId, 中间可能会有空缺明细表
     *
     * @deprecated 推荐使用获取order序列的方法
     */
    @Deprecated
    protected int getDetailTableCount() {
        int count = WorkflowAPI.getDetailTableCountByRequestId(getRequestId());
        this.log("max order of detail table: " + count);
        return count;
    }

    /**
     * 获取真实存在的明细表数量
     */
    protected int getDetailTableCountRealExist() {
        int count = WorkflowAPI.getDetailTableCountByRequestIdRealExist(getRequestId());
        this.log("detail table count(real exist): " + count);
        return count;
    }

    /**
     * 获取第order张明细表的orderId, orderId <= order
     */
    protected int getDetailTableOrderId(int order) {
        Assert.notNeg(order, "Detail table order cannot be negative, yours: " + order);
        if (order == 0)
            return 0;

        int[] orderSeq = getDetailTableOrderSequence();
        int len = orderSeq.length;
        if (order > len)
            throw new IllegalArgumentException("Detail table is not exist which its order is " + order +
                    ", detail table count is " + len);

        return orderSeq[order - 1];
    }

    /**
     * @see WorkflowAPI#getDetailTableOrderSequenceByRequestId(int)
     */
    protected int[] getDetailTableOrderSequence() {
        return WorkflowAPI.getDetailTableOrderSequenceByRequestId(getRequestId());
    }

    /**
     * 清除指定明细表数据
     */
    protected void clearDetailTableData(int order) {
        WorkflowAPI.clearDetailTableDataByRequestIdAndOrder(getRequestId(), order);
        this.log("The detail table " + order + " has been cleaned.");
    }

    /**
     * 清除所有明细表数据
     */
    protected void clearAllDetailTableData() {
        WorkflowAPI.clearAllDetailTableDataByRequestId(getRequestId());
        this.log("All detail tables have been cleaned.");
    }

    protected void setRealExecutor() {
        this.realExecutor = true;
    }

    /**
     * @since 1.1.11
     */
    protected void setAgentExecutor() {
        this.realExecutor = false;
    }

    /**
     * 新建一个本类实例, 设置为realExecutor, 用其执行流程流转接口的实际逻辑
     *
     * @since 1.1.6
     */
    protected String executeInNewHandler(RequestInfo requestInfo) throws IllegalAccessException, InstantiationException {
        Class<? extends ActionHandler> clazz = this.getClass();
        ActionHandler newHandler = clazz.newInstance();// 最外层类一定要有空参构造
        fillFieldQuietly(newHandler);// 注入参数
        newHandler.setRealExecutor();// 设置为realExecutor, 即可使用

        log("RealExecutor#" + StringUtils.toStringNative(newHandler)
                + " :: " + StringUtils.toStringNative(requestInfo));
        return newHandler.execute(requestInfo);
    }

    /**
     * 填充handler的实例属性, 仅填充被标记的属性或被标记的类中的全部属性.
     * 标记为{@link Autowired}, 一般这些属性由流程流转集成模块注入).
     * <hr/>
     * 1.1.6 ~ 1.1.10的bug: <br>
     * 如果不在构造中将当前handler设置为realExecutor, 则无法获取外部注入参数.<br>
     * 修复方案: 在1.1.11或之后的版本中使用@Autowired标注字段或类.
     *
     * @since 1.1.11
     */
    protected void fillFieldQuietly(ActionHandler handler) {
        if (handler.getClass() != this.getClass())
            throw new IllegalArgumentException("Inconsistent handler type.");

        log("Fill field quietly start >>");
        Class<? extends ActionHandler> clazz = this.getClass();
        Class<?>[] all = ReflectUtils.getAllSuper(clazz);
        // 最后5个元素为Object, ActionHandler以及其3个接口, 不存在参数
        all = ArrayUtils.arrayExtend(all, all.length - 5);

        for (Class<?> c : all) {
            // filter static and final
            Field[] fields = ReflectUtils.queryFields(c, 8 + 16, false);
            if (fields.length == 0) continue;

            // 在类上进行标注会对该类所有声明的字段生效(除static和final)
            boolean allFieldsAutowired = c.isAnnotationPresent(Autowired.class);
            for (Field f : fields) {
                String fSign = f.toString();
                if (allFieldsAutowired || f.isAnnotationPresent(Autowired.class)) {
                    // auto wire
                    try {
                        f.setAccessible(true);
                        Object v = f.get(this);
                        f.set(handler, v);
                        log("Field already be filled >> " + fSign + " : " + v);
                    } catch (IllegalAccessException e) {
                        log("Exception field >> " + fSign, e);
                    }
                }
            }
        }
        log("Fill field quietly end >>");
    }

    /**
     * 校验当前实例中所有注解为 @NotNull 的参数, 如果有为 null 的则抛出异常
     *
     * @since 2.0.2
     */
    private void checkRequiredArguments() {
        Class<? extends ActionHandler> clazz = this.getClass();
        Class<?>[] all = ReflectUtils.getAllSuper(clazz);
        // 最后5个元素为Object, ActionHandler以及其3个接口, 不存在参数
        all = ArrayUtils.arrayExtend(all, all.length - 5);

        try {
            for (Class<?> c : all) {
                // filter static and final
                Field[] fields = ReflectUtils.queryFields(c, 8 + 16, false);
                if (fields.length == 0) continue;

                for (Field f : fields) {
                    if (f.isAnnotationPresent(NotNull.class)) {
                        f.setAccessible(true);
                        if (f.get(this) == null)
                            throw new IllegalArgumentException("The required argument is not set: " + f);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log("Cannot check params!", e);
        }
    }

}