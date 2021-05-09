<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.api.WorkflowAPI" %>
<%@ page import="weaver.micro.devkit.api.CommonAPI" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="weaver.micro.devkit.util.StringUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    try {
        int requestId = Util.getIntValue(request.getParameter("requestid"));
        int mainId = WorkflowAPI.getMainId(requestId);
        String table = WorkflowAPI.queryBillTableByRequest(requestId);
        Map<String, String> mtData = CommonAPI.query(table, null, "requestid=" + requestId).get(0);

        int detailCount = WorkflowAPI.getDetailTableCountByRequestId(requestId);
        List[] detailTables = new List[detailCount + 1];
        for (int i = 1; i <= detailCount; i++) {
            String dtTable = WorkflowAPI.getDetailTableNameByBillTableNameAndOrderId(table, i);
            detailTables[i] = CommonAPI.query(dtTable, null, "mainid=" + mainId);
        }
%>
<html>
<head>
    <title>Show Workflow Form Data</title>
    <style>
    </style>
</head>
<body>
<h2>Main Table Data</h2>
<table border="1px" cellpadding="10px" cellspacing="0">
    <thead>
    <tr>
        <th>Key</th>
        <th>Data</th>
    </tr>
    </thead>
    <tbody>
    <%
        Set<Map.Entry<String, String>> entrySet = mtData.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
    %>
    <tr>
        <td>
            <%=entry.getKey()%>
        </td>
        <td>
            <%=entry.getValue()%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<hr/>
<%
    for (int i = 1; i < detailTables.length; i++) {
        List<Map<String, String>> dtData = detailTables[i];
%>
<h2>Detail Table Data(Order <%=i%>, Row Count <%=dtData.size()%>)</h2>
<%
    if (dtData.isEmpty())
        continue;
%>
<table border="1px" cellpadding="10px" cellspacing="0">
    <thead>
    <tr>
        <%
            Set<String> keys = dtData.get(0).keySet();
            for (String key : keys) {
        %>
        <th>
            <%=key%>
        </th>
        <%
            }
        %>
    </tr>
    </thead>
    <tbody>
    <%
        for (Map<String, String> m : dtData) {
    %>
    <tr>
        <%
            for (String key : keys) {
        %>
        <td>
            <%=m.get(key)%>
        </td>
        <%
            }
        %>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
<hr/>
<%
    }
%>
</body>
</html>
<%
    } catch (Throwable t) {
        out.print(StringUtils.toString(t));
        return;
    }
%>