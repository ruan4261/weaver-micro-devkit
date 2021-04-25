<%@ page import="weaver.conn.RecordSet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    RecordSet rs = new RecordSet();
    rs.execute("select id, custompage from workflow_base");
%>
<html>
<head>
    <title>From Table(workflow_base)</title>
    <style>
    </style>
</head>
<body>
<table border="1px" cellpadding="10px" cellspacing="0">
    <thead>
    <tr>
        <th>Id</th>
        <th>Custompage</th>
    </tr>
    </thead>
    <tbody>
    <%
        while (rs.next()) {
            int id = rs.getInt("id");
            String custompage = rs.getString("custompage");
            boolean red = !custompage.startsWith("/micro/devkit/cprouter/router.jsp");
    %>
    <tr>
        <td><%=id%>
        </td>
        <td <%if (red){%>style="color: red"<%}%>>
            <%=custompage%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
</body>
</html>