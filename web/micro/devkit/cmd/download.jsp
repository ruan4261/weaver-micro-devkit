<%@ page import="weaver.micro.devkit.Assert" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="weaver.general.Base64" %>
<%@ page import="java.io.FileInputStream" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%--
通过 Base64 编码, 然后打印在页面控制台里.
这样有两个好处
1. 不会导致文件损毁
2. 不会造成页面渲染的压力
坏处很多, 就不说了
--%>
<%
    String path = Assert.notEmpty(request.getParameter("path"));
    FileInputStream input = new FileInputStream(path);
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    byte[] buf = new byte[8192];
    int read;
    while ((read = input.read(buf)) != -1) {
        output.write(buf, 0, read);
    }

    byte[] bytes = Base64.encode(output.toByteArray());
    String base64 = new String(bytes);
%>
<script>
  console.log('<%=base64%>')
</script>