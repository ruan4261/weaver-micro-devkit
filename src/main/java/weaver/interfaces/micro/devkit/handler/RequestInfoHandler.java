package weaver.interfaces.micro.devkit.handler;

import weaver.conn.RecordSetTrans;
import weaver.general.BaseBean;
import weaver.interfaces.micro.devkit.api.DocAPI;
import weaver.interfaces.micro.devkit.api.Formatter;
import weaver.interfaces.micro.devkit.api.WorkflowAPI;
import weaver.interfaces.micro.devkit.core.CacheBase;
import weaver.interfaces.micro.devkit.exception.runtime.ActionStopException;
import weaver.interfaces.workflow.action.Action;
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

    private RequestInfo request;

    private final StringBuilder logInfo;

    private Map<String, String> mainTable;

    private final String actionInfo;

    public RequestInfoHandler(String actionInfo) {
        this.logInfo = new StringBuilder(LINE_SEPARATOR);
        this.actionInfo = actionInfo;
    }

    public void construct(RequestInfo request) {
        this.request = request;
        this.actionStart();
    }

    /**
     * 获取流程主表数据
     *
     * @return 流程主表对应的单行数据，字段名到流程数据的映射，数据值可能为NULL。
     */
    public Map<String, String> getMainTable() {
        if (mainTable != null) return mainTable;
        mainTable = new HashMap<String, String>() {
            @Override
            public String get(Object key) {
                return Formatter.toString(super.get(key));
            }
        };
        Property[] properties = this.request.getMainTableInfo().getProperty();
        for (Property property : properties) {
            mainTable.put(property.getName(), property.getValue());
        }
        this.logInfo.append(this.getLogPrefix()).append(mainTable.toString()).append(LINE_SEPARATOR);
        return mainTable;
    }

    /**
     * 获取明细表数据
     *
     * @param tableIdx 下标从0开始
     * @return 流程第(tableIdx - 1)个明细表的对应多行数据，字段名到流程数据的映射，数据值可能为NULL。
     */
    public List<Map<String, String>> getDetailTable(int tableIdx) {
        ArrayList<Map<String, String>> data = new ArrayList<>();
        DetailTable[] dts = this.request.getDetailTableInfo().getDetailTable();
        DetailTable dt = dts[tableIdx];
        Row[] rows = dt.getRow();
        for (Row row : rows) {
            Cell[] cells = row.getCell();
            Map<String, String> map = new HashMap<>();
            for (Cell cell : cells) {
                map.put(cell.getName(), cell.getValue());
            }
            data.add(map);
        }
        this.logInfo.append(this.getLogPrefix()).append(data.toString()).append(LINE_SEPARATOR);
        return data;
    }

    /**
     * @return 流程标题
     * @see WorkflowAPI#queryWorkflowTitle(String)
     */
    public String getWorkflowTitle() {
        return WorkflowAPI.queryWorkflowTitle(this.getRequestId());
    }

    /**
     * @return 流程签字意见列表
     * @see WorkflowAPI#queryRemarkList(String)
     */
    public List<Map<String, String>> getRemarkList() {
        return WorkflowAPI.queryRemarkList(this.getRequestId());
    }

    /**
     * @return 流程相关文档
     * @see DocAPI#queryDocIdByRequestId(String)
     */
    public String getDocIdLatest() throws ActionStopException {
        return DocAPI.queryDocIdByRequestId(this.getRequestId());
    }

    public String getRequestId() {
        return this.request.getRequestid();
    }

    public String getRequestName() {
        return this.request.getRequestManager().getRequestname();
    }

    public String getWorkflowId() {
        return this.request.getWorkflowid();
    }

    /**
     * 所有打印的日志前缀为 StackTrace@requestid~
     * 用于便捷查询
     */
    private String getLogPrefix() {
        return this.actionInfo + "$request:" + this.getRequestId() + '$';
    }

    public String getCreatorId() {
        return this.request.getCreatorid();
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
        this.logInfo.append(msg).append(LINE_SEPARATOR);
    }

    public void log(Throwable throwable) {
        StackTraceElement[] trace = throwable.getStackTrace();
        log(throwable.getMessage());
        for (StackTraceElement traceElement : trace) {
            log("\tat " + traceElement);
        }
    }

    /**
     * 流程退回
     *
     * @param msg 前端显示信息
     * @return FAILURE_AND_CONTINUE
     */
    public String requestFail(String msg) {
        this.request.getRequestManager().setMessageid("0");
        this.request.getRequestManager().setMessagecontent(msg);
        return this.actionEnd(Action.FAILURE_AND_CONTINUE);
    }

    /** 接口执行完毕 */
    public String requestSuccess() {
        return this.actionEnd(Action.SUCCESS);
    }

    private void actionStart() {
        this.logInfo
                .append(getLogPrefix())
                .append(" start, bill table is ").append(this.getTableNameLower())
                .append(", workflow title is ").append(this.getWorkflowTitle())
                .append(", creator hrmId is ").append(this.getCreatorId())
                .append(LINE_SEPARATOR);
    }

    private String actionEnd(String result) {
        this.logInfo
                .append(getLogPrefix())
                .append(" end with result:").append(result)
                .append(LINE_SEPARATOR);
        this.writeLog(this.logInfo.toString());
        return result;
    }

}
