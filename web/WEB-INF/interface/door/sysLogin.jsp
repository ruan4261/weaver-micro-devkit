<%@ page language="java" contentType="text/html; charset=utf-8" %>
<html>
<head>
    <title>单点</title>
</head>
<body>
<div style="text-align: center;margin-top: 40px">
    <h3>OA Log In.</h3>
    <form id="form" action="casNEW.jsp" method="POST">

        Id : <input name="id" type="text"/>
        <br/>
        LoginId : <input name="loginid" type="text">
        <br/><br/>

        <%--登录id：<input name="loginid" type="text" id="txtLogin" tabindex="1" size="15" value=""/>
        <br/><br/>--%>
        <input type="submit" value="Log In">
    </form>
</div>
</body>
</html>






