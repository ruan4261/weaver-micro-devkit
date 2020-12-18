<%@ page language="java" contentType="text/html; charset=GBK" %> 
<%@ page import="weaver.general.Util,weaver.hrm.*" %>
<%@ page import="java.util.Map,java.util.HashMap"%>
<%@ page import="weaver.systeminfo.template.UserTemplate"%>
<jsp:useBean id="rs" class="weaver.conn.RecordSet" scope="page"/>
<%
	response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); //HTTP 1.0
	response.setDateHeader ("Expires", 0);  //prevents caching at the proxy server 
	 
%>
<%
	String userid = Util.null2String(request.getParameter("id"));
	String loginid = Util.null2String(request.getParameter("loginid"));

	if("".equals(userid) && "".equals(loginid)){
		response.sendRedirect("/login/Login.jsp");
		return; 
	}
	rs.execute("select * from hrmresource where id = '"+userid+"' or loginid = '"+loginid+"'");
	if(rs.next()){
	    User user = new User();
		user.setUid(rs.getInt("id"));
		user.setLoginid(rs.getString("loginid"));

		//user.setFirstloginid(rs.getString("firstloginid"));
		//user.setLastloginid(rs.getString("lastloginid"));
		//user.setAliasloginid(rs.getString("aliasloginid"));
		user.setTitle(rs.getString("title"));
		//user.setTitlelocation(rs.getString("titlelocation"));
		user.setSex(rs.getString("sex"));
		user.setLastname(rs.getString("lastname"));
		user.setPwd(rs.getString("password"));
		String languageidweaver = rs.getString("systemlanguage");
		user.setLanguage(Util.getIntValue(languageidweaver, 0));
		user.setTelephone(rs.getString("telephone"));
		user.setMobile(rs.getString("mobile"));
		user.setMobilecall(rs.getString("mobilecall"));
		user.setEmail(rs.getString("email"));
		user.setCountryid(rs.getString("countryid"));
		user.setLocationid(rs.getString("locationid"));
		user.setResourcetype(rs.getString("resourcetype"));
		user.setStartdate(rs.getString("startdate"));
		user.setEnddate(rs.getString("enddate"));
		//user.setContractdate(rs.getString("contractdate"));
		user.setJobtitle(rs.getString("jobtitle"));
		//user.setJobgroup(rs.getString("jobgroup"));
		user.setJobactivity(rs.getString("jobactivity"));
		user.setJoblevel(rs.getString("joblevel"));
		user.setSeclevel(rs.getString("seclevel"));
		user.setUserDepartment(Util.getIntValue(rs.getString("departmentid"), 0));
		user.setUserSubCompany1(Util.getIntValue(rs.getString("subcompanyid1"), 0));
		user.setUserSubCompany2(Util.getIntValue(rs.getString("subcompanyid2"), 0));
		//user.setUserSubCompany3(Util.getIntValue(rs.getString("subcompanyid3"), 0));
		//user.setUserSubCompany4(Util.getIntValue(rs.getString("subcompanyid4"), 0));
		user.setManagerid(rs.getString("managerid"));
		user.setAssistantid(rs.getString("assistantid"));
		//user.setPurchaselimit(rs.getString("purchaselimit"));
		//user.setCurrencyid(rs.getString("currencyid"));
		//user.setLastlogindate(rs.getString("currentdate"));
		user.setLogintype("1");

		user.setAccount(rs.getString("account"));
		user.setLoginip(request.getRemoteAddr());
		request.getSession(true).setMaxInactiveInterval(60 * 60 * 24);
		request.getSession(true).setAttribute("weaver_user@bean", user);
		request.getSession(true).setAttribute("moniter", new OnLineMonitor(user.getUID()+"",user.getLoginip()));
		
		Util.setCookie(response, "loginfileweaver", "/main.jsp", 172800);
		Util.setCookie(response, "loginidweaver", user.getUID()+"", 172800);
		Util.setCookie(response, "languageidweaver", languageidweaver, 172800);
		//用户的登录后的页面
		UserTemplate ut = new UserTemplate();
		ut.getTemplateByUID(user.getUID(),user.getUserSubCompany1());
		int templateId = ut.getTemplateId();
		int extendTempletid = ut.getExtendtempletid();
		int extendtempletvalueid = ut.getExtendtempletvalueid();
		session.setAttribute("defaultHp",ut.getDefaultHp());
		//20190122<MANGO>新增,该文件固定跳转至查看流程详细的页面
		
		String tourl = "/wui/main.jsp";
		Map logmessages = (Map)application.getAttribute("logmessages");
		if (logmessages == null || logmessages.size() == 0) {
			logmessages = new HashMap();
			logmessages.put(user.getUID()+"","");
			application.setAttribute("logmessages",logmessages);
		}
		session.setAttribute("logmessage","");
		

		//request.getRequestDispatcher("/interface/zc/test/fytest.jsp?zcid=111").forward(request, response);

        response.sendRedirect(tourl);
	}else{
	//out.println("登陆名为("+loginid+")的用户在ecology中不存在。");
%>
	<script language="javascript">
        alert('loginid:'+"<%=loginid%>")
		alert("您不是系统的办公人员,不能登录！");
		window.location.href = "/login/login.jsp";
	</script>
<%
	}
	if(loginid == null){
		response.sendRedirect("/login/login.jsp");
	}
%>
