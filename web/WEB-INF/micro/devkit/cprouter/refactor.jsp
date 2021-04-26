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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    Loggable loggable = LogEventProcessor.getInstance("WeaverMicroDevkit[cprouter]: refactor");

    void log(JspWriter out, String mes) throws IOException {
        out.print(mes);
        out.print("<br>");
        loggable.log(mes);
    }

    void log(JspWriter out, Throwable t) throws IOException {
        String mes = StringUtils.toString(t);
        out.print(mes);
        out.print("<br>");
        loggable.log(mes);
    }
%>
<%
    try {
        // custom page router mode refactoring
        int modeid = Util.getIntValue(request.getParameter("modeid"));
        String workflowIds = request.getParameter("workflowIds");// 只更新其中流程, 如果为null则更新全部
        String mode = "uf_cprouter";
        String dest = "/micro/devkit/cprouter/router.jsp";

        RecordSet rs = new RecordSet();
        RecordSet exe = new RecordSet();

        String template = "select %s from workflow_base where %s (custompage is null or custompage<>'%s')";
        String sql = String.format(template,
                "id, custompage",
                workflowIds == null ? "" : "id in (" + workflowIds + ") and",
                dest);
        log(out, sql);
        String countSql = String.format(template,
                "count(*) cnt",
                workflowIds == null ? "" : "id in (" + workflowIds + ") and",
                dest);
        rs.execute(countSql);
        rs.next();
        log(out, "data count: " + rs.getInt("cnt"));

        // confirm start program
        String verify = Util.null2String(request.getParameter("auth"));
        if (verify.equals("1") && modeid != -1) {
            log(out, "认证通过, 开始重构, 模块id: " + modeid + ".<br>");
        } else {
            log(out, "modeid: " + modeid);
            log(out, "<h1>请通过auth参数确认执行重构.</h1>");
            return;
        }

        log(out, "<hr>");

        rs.execute(sql);
        while (rs.next()) {
            int id = rs.getInt("id");
            String custompage = rs.getString("custompage");

            log(out, "Origin: workflowid=" + id + ",custompage=" + custompage + "<br>");
            try {
                if (!custompage.equals("")) {
                    // 原先存在custompage
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("workflowid", id);
                    data.put("model", 0);
                    data.put("load_order", 0);
                    data.put("custompage", custompage);
                    data.put("disable", 0);
                    data.put("describe", "Automatic created by refactoring program.[" + TimeUtil.getCurrentTimeString() + "]");

                    ModeAPI.createModeData(mode, modeid, 1, data);
                }

                // 更新workflow_base
                String sqlUpdate = "update workflow_base set custompage='" + dest + "' where id=" + id;
                log(out, sqlUpdate);
                exe.execute(sqlUpdate);
            } catch (Throwable t) {
                log(out, "<hr>");
                log(out, t);
                log(out, "<h2 style=\"color: red\">Program has been stopped! Please roll back manually.</h2><br>");
                return;
            }

            log(out, "Success: Workflowid=" + id + "<br>");
        }

        log(out, "重构完成");
    } catch (Throwable t) {
        log(out, "<hr>");
        log(out, t);
    }
%>