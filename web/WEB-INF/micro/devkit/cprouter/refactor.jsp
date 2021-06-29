<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.api.ModeAPI" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="weaver.micro.devkit.util.StringUtils" %>
<%@ page import="java.io.IOException" %>
<%@ page import="weaver.micro.devkit.handler.Loggable" %>
<%@ page import="weaver.micro.devkit.handler.LogEventProcessor" %>
<%@ page import="weaver.general.TimeUtil" %>
<%@ page import="weaver.micro.devkit.Cast" %>
<%@ page import="weaver.micro.devkit.api.CommonAPI" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final String mainTable = "uf_cprouter";
    static final String detailTable = "uf_cprouter_dt1";
    static final String dest = "/micro/devkit/cprouter/router.jsp";

    Loggable loggable = LogEventProcessor.getInstance("WeaverMicroDevkit[cprouter]: refactor");
    JspWriter out;

    void setOut(JspWriter out) {
        this.out = out;
    }

    void doLog(String mes) throws IOException {
        this.out.print(mes);
        this.out.print("<br>");
        this.loggable.log(mes);
    }

    void doLog(Throwable t) throws IOException {
        String mes = StringUtils.toString(t);
        this.out.print(mes);
        this.out.print("<br>");
        this.loggable.log(mes);
    }

    void clearLogStream() {
        this.out = null;
    }

    /**
     * 根据流程编号获取uf_cprouter中的id, 如果不存在则新建后返回新建的id
     */
    int getWorkflowMainId(int workflowId, int modeId) {
        int id = Cast.o2Integer(CommonAPI.query(
                "select id from uf_cprouter where workflowid = " + workflowId
        ));
        if (id != -1) return id;

        // insert into
        Map<String, Object> data = new HashMap<String, Object>(1, 1f);
        data.put("workflowid", workflowId);
        return ModeAPI.createModeData(
                mainTable,
                modeId,
                1,
                data
        );
    }
%>
<%
    setOut(out);
    try {
        // custom page router mode refactoring
        int modeid = Util.getIntValue(request.getParameter("modeid"));
        String workflowIds = Cast.o2String(request.getParameter("workflowIds"), "-1");// 只更新其中流程, 如果为*则更新全部

        RecordSet rs = new RecordSet();
        RecordSet exe = new RecordSet();

        String template = "select %s from workflow_base where %s (custompage is null or custompage<>'%s')";
        String inWorkflow = workflowIds.equals("*") ? "" : "id in (" + workflowIds + ") and";
        String sql = String.format(template,
                "id, custompage",
                inWorkflow,
                dest);
        doLog(sql);
        String countSql = String.format(template,
                "count(*) cnt",
                inWorkflow,
                dest);
        rs.execute(countSql);
        rs.next();
        doLog("data count: " + rs.getInt("cnt"));

        // confirm start program
        String verify = Util.null2String(request.getParameter("auth"));
        if (verify.equals("1") && modeid != -1) {
            doLog("认证通过, 开始重构, 模块id: " + modeid + ".<br>");
        } else {
            doLog("modeid: " + modeid);
            doLog("<h1>请通过auth参数确认执行重构.</h1>");
            return;
        }

        doLog("<hr>");

        rs.execute(sql);
        while (rs.next()) {
            int id = rs.getInt("id");
            String custompage = rs.getString("custompage");

            doLog("Origin: workflowid=" + id + ", custompage=" + custompage + "<br>");
            try {
                int mainId = getWorkflowMainId(id, modeid);
                // 如果原先custompage不存在则新增数据
                if (!custompage.equals("")) {
                    exe.execute("insert into " + detailTable + "(mainid, model, load_order, custompage, disable, describe)" +
                            "\nvalues(" + mainId + ", '0', '10', '" + custompage + "', '0'," +
                            " 'Automatic created by refactoring program.[" + TimeUtil.getCurrentTimeString() + "]')");
                }

                // 更新workflow_base
                String sqlUpdate = "update workflow_base set custompage='" + dest + "' where id=" + id;
                doLog(sqlUpdate);
                exe.execute(sqlUpdate);
            } catch (Throwable t) {
                doLog("<hr>");
                doLog(t);
                doLog("<h2 style=\"color: red\">Program has been stopped! Please roll back manually.</h2><br>");
                return;
            }

            doLog("Success: Workflowid=" + id + "<br>");
        }

        doLog("重构完成");
    } catch (Throwable t) {
        doLog("<hr>");
        doLog(t);
    } finally {
        clearLogStream();
    }
%>