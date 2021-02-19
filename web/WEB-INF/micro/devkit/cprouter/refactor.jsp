<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.api.ModeAPI" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String verify = Util.null2String(request.getParameter("auth"));
    if (verify.equals("1")) {
        out.print("认证通过, 开始重构.<br>");
    } else {
        out.print("请通过auth参数确认执行重构.");
        return;
    }

    String mode = "uf_cprouter";
    int modeid = -1;// 需要手动修改
    String dest = "/micro/devkit/cprouter/router.jsp";

    RecordSet exe = new RecordSet();// 执行
    RecordSet rs = new RecordSet();// 查询

    String sql = String.format("select id,custompage from workflow_base where custompage<>'%s'", dest);
    out.print(sql + "<br>");
    rs.execute(sql);

    while (rs.next()) {
        int id = rs.getInt("id");
        String custompage = rs.getString("custompage");
        out.print("Origin: workflowid=" + id + ",custompage=" + custompage + "<br>");

        // 数据录入建模
        try {
            if (!custompage.equals("")) {
                // 原先存在custompage
                Map<String, Object> data = new HashMap<String, Object>(8);
                data.put("workflowid", id);
                data.put("model", 0);
                data.put("load_order", 0);
                data.put("custompage", custompage);
                data.put("disable", 0);
                data.put("first_active", 1);
                data.put("describe", "Automatic created by refactoring program.");

                ModeAPI.createModeData(mode, modeid, 1, data);
            }
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
    exe.execute(sql);

    out.print("<br>重构完成");
%>