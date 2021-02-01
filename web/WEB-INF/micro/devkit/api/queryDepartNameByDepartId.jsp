<%@ page import="com.weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.api.HrmAPI" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    int departId = Util.getIntValue(request.getParameter("departId"));
    if (departId == -1) return;

    out.print("{\"departName\": \"" + HrmAPI.queryDepartName(departId) + "\"}");
%>