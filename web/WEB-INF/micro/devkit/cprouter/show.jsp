<%@ page import="weaver.conn.RecordSet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    RecordSet rs = new RecordSet();
    rs.execute("select id, custompage, custompage4emoble from workflow_base");
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
        <th>Custompage4Mobile</th>
    </tr>
    </thead>
    <tbody>
    <%
        while (rs.next()) {
            int id = rs.getInt("id");
            String custompage = rs.getString("custompage");
            String custompage4emoble = rs.getString("custompage4emoble");
            boolean notice4pc = !custompage.startsWith("/micro/devkit/cprouter/router.jsp");
            boolean notice4mobile = !custompage.startsWith("/micro/devkit/cprouter/router.jsp");
    %>
    <tr>
        <td><%=id%>
        </td>
        <td <%if (notice4pc){%>style="color: red"<%}%>>
            <%=custompage%>
        </td>
        <td <%if (notice4mobile){%>style="color: red"<%}%>>
            <%=custompage4emoble%>
        </td>
    </tr>
    <%
        }
    %>
    </tbody>
</table>
</body>
</html>