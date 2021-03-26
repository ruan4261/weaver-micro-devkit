<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.api.ModeAPI" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // custom page router mode refactoring
    int modeid = Util.getIntValue(request.getParameter("modeid"));
    String mode = "uf_cprouter";
    String dest = "/micro/devkit/cprouter/router.jsp";

    // confirm start program
    String verify = Util.null2String(request.getParameter("auth"));
    if (verify.equals("1") && modeid != -1) {
        out.print("认证通过, 开始重构, 模块id: " + modeid + ".<br>");
    } else {
        out.print("<h1>请通过auth, modeid参数确认执行重构.</h1>");
        return;
    }

    RecordSet rs = new RecordSet();

    // 一般数据库使用null做比较时会返回null, 所以为null的数据不会被查询出来
    String sql = String.format("select id,custompage from workflow_base where custompage<>'%s'", dest);
    out.print(sql + "<br>");
    rs.execute(sql);

    while (rs.next()) {
        int id = rs.getInt("id");
        String custompage = rs.getString("custompage");
        if (custompage.equals(""))
            continue;

        out.print("Origin: workflowid=" + id + ",custompage=" + custompage + "<br>");
        // 数据录入建模
        try {
            // 原先存在custompage
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("workflowid", id);
            data.put("model", 0);
            data.put("load_order", 0);
            data.put("custompage", custompage);
            data.put("disable", 0);
            data.put("describe", "Automatic created by refactoring program.");

            ModeAPI.createModeData(mode, modeid, 1, data);
        } catch (Exception e) {
            out.print(e.toString() + "<br>");
            out.print("<h2 style=\"color: red\">Please roll back manually.</h2><br>");
            return;
        }

        out.print("Success: Workflowid=" + id + "<br>");
        out.print("<br>");
    }

    // 覆盖所有流程的custompage
    sql = String.format("update workflow_base set custompage='%s'", dest);
    out.print(sql + "<br>");
    rs.execute(sql);

    out.print("<br>重构完成");
%>