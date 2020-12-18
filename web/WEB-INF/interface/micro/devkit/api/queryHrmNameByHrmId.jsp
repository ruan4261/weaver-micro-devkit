<%@ page import="weaver.micro.devkit.api.HrmAPI" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    int hrmId = Util.getIntValue(request.getParameter("hrmId"));
    if (hrmId == -1) return;

    JSONObject res = new JSONObject(2);// 无法改变负载因子，所以初始化容量为2
    res.put("hrmName", HrmAPI.queryHrmName(hrmId));

    out.print(res.toJSONString());
%>