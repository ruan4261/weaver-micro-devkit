<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.micro.devkit.api.CommonAPI" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    RecordSet rs = new RecordSet();
    rs.execute("select * from uf_cprouter");
    while (rs.next()) {
        out.print(CommonAPI.mapFromRecordRow(rs));
        out.print("<br/><hr/>");
    }
%>