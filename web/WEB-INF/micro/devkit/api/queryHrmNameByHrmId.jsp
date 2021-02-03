<%@ page import="weaver.micro.devkit.api.HrmAPI" %>
<%@ page import="weaver.general.Util" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    int hrmId = Util.getIntValue(request.getParameter("hrmId"));
    if (hrmId == -1) return;

    out.print("{\"hrmName\": \"" + HrmAPI.queryHrmName(hrmId) + "\"}");
%>