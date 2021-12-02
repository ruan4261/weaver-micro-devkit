<%@ page import="weaver.conn.RecordSet" %>
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
<%@ page import="weaver.micro.devkit.handler.StrictRecordSet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    static final String mainTable = "uf_cprouter";
    static final String detailTable = "uf_cprouter_dt1";
    static final String dest = "/micro/devkit/cprouter/router.jsp";
    static final String dest4mobile = "/micro/devkit/cprouter/router.jsp?isMobile=1";

    Loggable loggable = LogEventProcessor.getInstance("WeaverMicroDevkit[cprouter]: refactor");
    JspWriter out;

    void setOut(JspWriter out) {
        this.out = out;
    }

    void doLogLine() throws IOException {
        this.out.print("<hr/>");
    }

    void doLog(String... msgs) throws IOException {
        for (String msg : msgs) {
            this.out.print(msg);
            this.out.print("<br/>");
            this.loggable.log(msg);
        }
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
        int modeid = Cast.o2Integer(request.getParameter("modeid"));
        String workflowIds = Cast.o2String(request.getParameter("workflowIds"), "-1");// 只更新其中流程, 如果为*则更新全部
        String auth = Cast.o2String(request.getParameter("auth"));

        RecordSet rs = new StrictRecordSet();
        RecordSet exe = new StrictRecordSet();

        String workflowCondition = workflowIds.equals("*") ? "" : "id in (" + workflowIds + ") and";
        String template = "select %s from workflow_base where "
                + workflowCondition
                + " ((custompage is null or custompage not like '" + dest + "%%') or"
                + " (custompage4emoble is null or custompage4emoble not like '" + dest + "%%'))";
        String sql = String.format(template, "id, custompage, custompage4emoble");
        doLog(sql);

        // log count
        String countSql = String.format(template, "count(*) cnt");
        doLog("data count: " + CommonAPI.querySingleField(countSql));

        // confirm start program
        if (auth.equals("1") && modeid != -1) {
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
            String custompage4emoble = rs.getString("custompage4emoble");
            String sameCheckbox = custompage.equals(custompage4emoble) ? "1" : null;

            doLog("Origin: workflowid=" + id + ", custompage=" + custompage + ", custompage4emoble=" + custompage4emoble + "<br>");
            try {
                int mainId = getWorkflowMainId(id, modeid);

                // 录入 uf_cprouter_dt1 数据
                // custompage
                if (!custompage.startsWith(dest)) {
                    if (!custompage.equals("")) {
                        exe.executeUpdate("insert into " + detailTable +
                                        "(mainid, model, load_order, custompage, used4pc, used4mobile, disable, describe)" +
                                        " values(?, 0, 10, ?, 1, ?, 0, ?)",
                                mainId, custompage, sameCheckbox,
                                "Automatic created by refactoring program.[" + TimeUtil.getCurrentTimeString() + "]");
                    }

                    String updateSql = "update workflow_base set custompage = ? where id = " + id;
                    doLog(updateSql, dest);
                    exe.executeUpdate(updateSql, dest);
                }

                // custompage4emoble
                if (!custompage4emoble.startsWith(dest)) {
                    if (!custompage4emoble.equals("") && sameCheckbox == null) {
                        exe.executeUpdate("insert into " + detailTable +
                                        "(mainid, model, load_order, custompage, used4pc, used4mobile, disable, describe)" +
                                        " values(?, 0, 10, ?, 0, 1, 0, ?)",
                                mainId, custompage4emoble,
                                "Automatic created by refactoring program.[" + TimeUtil.getCurrentTimeString() + "]'");
                    }

                    String updateSql = "update workflow_base set custompage4emoble = ? where id = " + id;
                    doLog(updateSql, dest4mobile);
                    exe.executeUpdate(updateSql, dest4mobile);
                }
            } catch (Throwable t) {
                doLogLine();
                doLog(t);
                doLog("<h2 style=\"color: red\">Program has been stopped! Please roll back manually.</h2><br>");
                return;
            }

            doLog("Success: Workflowid=" + id + "<br>");
        }

        doLog("重构完成");
    } catch (Throwable t) {
        doLogLine();
        doLog(t);
    } finally {
        clearLogStream();
    }
%>