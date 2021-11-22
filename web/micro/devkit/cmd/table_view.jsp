<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.micro.devkit.Assert" %>
<%@ page import="weaver.micro.devkit.handler.StrictRecordSet" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
查看某张表全部的数据(注意别直接打崩了数据库)
--%>
<%
    String table = Assert.notEmpty(request.getParameter("table"));

    RecordSet rs = new StrictRecordSet();
    rs.executeQuery("select * from " + table);

    String[] cols = rs.getColumnName();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<table border="1">
    <thead>
    <tr>
        <%
            for (String col : cols) {
        %>
        <th><%=col%>
        </th>
        <%
            }
        %>
    </tr>
    </thead>
    <tbody>
    <%
        int len = rs.getColCounts();
        while (rs.next()) {
    %>
    <tr>
        <%
            for (int i = 1; i <= len; i++) {
        %>
        <td><%=rs.getString(i)%>
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
</body>
</html>