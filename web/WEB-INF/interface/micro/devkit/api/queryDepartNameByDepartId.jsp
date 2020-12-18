<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page import="com.weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.api.HrmAPI" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    int departId = Util.getIntValue(request.getParameter("departId"));
    if (departId == -1) return;

    JSONObject res = new JSONObject(2);// 无法改变负载因子，所以初始化容量为2
    res.put("departName", HrmAPI.queryDepartName(departId));

    out.print(res.toJSONString());
%>