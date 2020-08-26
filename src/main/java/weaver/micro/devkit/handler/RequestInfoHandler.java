package weaver.micro.devkit.handler;

import org.r2.devkit.util.Cast;
import weaver.conn.RecordSetTrans;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.micro.devkit.api.CommonAPI;
import weaver.micro.devkit.api.DocAPI;
import weaver.micro.devkit.api.WorkflowAPI;
import org.r2.devkit.core.CacheBase;
import org.r2.devkit.exception.runtime.ActionStopException;
import weaver.soa.workflow.request.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程对象RequestInfo处理器。
 *
 * @author ruan4261
 */
public class RequestInfoHandler extends BaseBean implements CacheBase {

    private int instanceRunTimes = 0;

    private RequestInfo request;

    private final String actionInfo;

    // 主表缓存
    private Map<String, String> mainTableCache;

    // 明细表缓存
    private Map<Integer, List<Map<String, String>>> detailTableListCache;

    // 明细表缓存，原型
    private DetailTable[] detailTablesCache;

    public RequestInfoHandler(String actionInfo) {
        this.actionInfo = actionInfo;
    }

    public void construct(RequestInfo request) {
        this.request = request;
        this.actionStart();
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
        if (mainTableCache != null) return mainTableCache;
        else mainTableCache = new HashMap<>();

        Property[] properties = this.request.getMainTableInfo().getProperty();
        for (Property property : properties) {
            mainTableCache.put(property.getName(), property.getValue());
        }

        logLine("MAIN FORM DATA(Cache) : " + mainTableCache.toString());
        return mainTableCache;
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
        if (detailTableListCache == null) detailTableListCache = new HashMap<>();

        List<Map<String, String>> data = detailTableListCache.get(table);
        if (data != null) return data;
        else {
            data = new ArrayList<>();
            detailTableListCache.put(table, data);
        }

        if (detailTablesCache == null)
            detailTablesCache = this.request.getDetailTableInfo().getDetailTable();

        DetailTable dt = detailTablesCache[table - 1];
        Row[] rows = dt.getRow();
        for (Row row : rows) {
            Cell[] cells = row.getCell();
            Map<String, String> map = new HashMap<>();
            for (Cell cell : cells) {
                map.put(cell.getName(), cell.getValue());
            }
            data.add(map);
        }

        logLine("DETAIL(dt_" + table + ") FORM DATA(Cache) : " + data.toString());
        return data;
    }

    /**
     * 通过数据库查询获取最新流程主表信息
     */
    public Map<String, String> getMainTableNewest() {
        Map<String, String> res = WorkflowAPI.queryRequestMainData(getTableNameUpper(), getRequestId());
        logLine("MAIN FORM DATA(Newest) : " + mainTableCache.toString());
        return res;
    }

    /**
     * 通过数据库查询获取最新流程明细表信息
     *
     * @param table 明细表序号，从1开始
     */
    public List<Map<String, String>> getDetailTableNewest(int table) {
        List<Map<String, String>> res = WorkflowAPI.queryRequestDetailData(getTableNameUpper(), getRequestId(), table);
        logLine("DETAIL(dt_" + table + ") FORM DATA(Newest) : " + res.toString());
        return res;
    }

    /**
     * @return 流程签字意见列表
     * @see WorkflowAPI#queryRemarkList(int)
     */
    public List<Map<String, String>> getRemarkList() {
        List<Map<String, String>> res = WorkflowAPI.queryRemarkList(this.getRequestId());
        logLine("Remark list : " + res.toString());
        return res;
    }

    /**
     * @return 流程相关最新文档
     * @see DocAPI#queryDocIdByRequestId(int)
     */
    public String getDocIdLatest() throws ActionStopException {
        String docId = DocAPI.queryDocIdByRequestId(this.getRequestId());
        log("Newest document id : " + docId);
        return docId;
    }

    public int getRequestId() {
        return Cast.toInteger(this.request.getRequestid(), -1);
    }

    /** 流程标题 */
    public String getRequestName() {
        return this.request.getRequestManager().getRequestname();
    }

    public int getWorkflowId() {
        return Cast.toInteger(this.request.getWorkflowid(), -1);
    }

    public int getBillId() {
        return this.request.getRequestManager().getBillid();
    }

    /**
     * 所有打印的日志前缀为 StackTrace@requestid~
     * 用于便捷查询
     */
    private String getLogPrefix() {
        return this.actionInfo + "$request:" + this.getRequestId() + '$';
    }

    public int getCreatorId() {
        return Cast.toInteger(this.request.getCreatorid(), 1);
    }

    public RecordSetTrans getRsTrans() {
        return this.request.getRsTrans();
    }

    public String getTableNameLower() {
        return this.request.getRequestManager().getBillTableName().toLowerCase();
    }

    public String getTableNameUpper() {
        return this.request.getRequestManager().getBillTableName().toUpperCase();
    }

    public void log(String msg) {
        writeLog(getLogPrefix() + " -> " + msg);
    }

    public void logLine(String msg) {
        writeLog(getLogPrefix() + " -> " + LINE_SEPARATOR + msg);
    }

    public void log(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        StackTraceElement[] trace = throwable.getStackTrace();
        builder.append(throwable.getClass().getTypeName()).append(':').append(throwable.getMessage());
        for (StackTraceElement traceElement : trace) {
            builder.append(LINE_SEPARATOR).append("\tat ").append(traceElement);
        }
        logLine(builder.toString());
    }

    /**
     * 流程退回
     *
     * @param msg 前端显示信息
     * @return FAILURE_AND_CONTINUE
     */
    public String requestFail(String msg) {
        log(msg);
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(msg + "【参考信息:" + getLogPrefix() + '】');
        return this.actionEnd(Action.FAILURE_AND_CONTINUE);
    }

    /** 接口执行完毕 */
    public String requestSuccess() {
        return this.actionEnd(Action.SUCCESS);
    }

    private void actionStart() {
        log(" start, bill table is " + this.getTableNameLower() +
                ", workflow title is " + this.getRequestName() +
                ", creator hrmId is " + this.getCreatorId() +
                ", runTimes of this action instance is " + (++this.instanceRunTimes));
    }

    private String actionEnd(String result) {
        log(" end with result:" + result);
        clearCache();
        return result;
    }

    public void clearCache() {
        this.mainTableCache = null;
        this.detailTableListCache = null;
        this.detailTablesCache = null;
    }

    /**
     * 校验字段长度
     * 正常情况下返回EMPTY空字符串
     * case:
     * 1.如无法获取该字段，返回空字符串
     * 2.字段在长度限制内，返回空字符串
     * 3.字段超长，不通过校验，返回信息为可供显示的信息(包括引号) -> '字段名称'
     *
     * @param table     为0时选择主表字段，非0时为明细表序号
     * @param field     字段数据库名
     * @param maxlength 限制长度
     * @return 字符串信息
     */
    public String fieldLengthLimit(int table, String field, int maxlength) {
        if (table < 0) return EMPTY;
        boolean overLimit = false;
        if (table == 0) {
            // 主表
            Map<String, String> mainTable = getMainTableCache();
            String v = mainTable.get(field);
            if (v.length() > maxlength) overLimit = true;
        } else {
            // 明细表
            List<Map<String, String>> detailTable = getDetailTableCache(table);
            for (Map<String, String> map : detailTable) {
                String v = map.get(field);
                if (v.length() > maxlength) {
                    overLimit = true;
                    break;
                }
            }
        }

        if (!overLimit) return EMPTY;
        // 通过数据库字段名获取显示字段名
        String sql = "select indexdesc from htmllabelindex a left outer join workflow_billfield b on a.id=b.fieldlabel where b.fieldname='" + field + "' and b.billid='" + getBillId() + "'";
        return '\'' + CommonAPI.querySingleField(sql, "indexdesc") + '\'';
    }

}
