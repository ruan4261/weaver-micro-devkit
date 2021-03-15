<%@ page import="weaver.micro.devkit.Cast" %>
<%@ page import="weaver.hrm.User" %>
<%@ page import="weaver.micro.devkit.api.CommonAPI" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // query hrm id
    int hrmId = Cast.o2Integer(request.getParameter("hrmId"), -1);
    if (hrmId == -1) {
        User user = (User) request.getSession().getAttribute("weaver_user@bean");
        hrmId = user.getUID();
    }

    // show fields
    String fields = request.getParameter("fields");
    List<Map<String, String>> result = CommonAPI.query("hrmresource", fields, "id=" + hrmId);

    if (result.isEmpty()) {
        response.sendError(418);
    } else {
        out.print(JSONObject.toJSONString(result.get(0)));
    }
%>