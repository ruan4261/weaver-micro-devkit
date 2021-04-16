<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.general.Util" %>
<%@ page import="weaver.micro.devkit.util.StringUtils" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
    -- START --
    全局custompage路由页面, 请在建模引擎中创建表单uf_cprouter, 并绑定模块(modeid在refactor时有用)
    custompage推荐使用绝对路径, 相对路径请以该文件(/micro/devkit/cprouter/router.jsp)为基准

    # 表结构
    表名: uf_cprouter
    字段: (请全部使用小写) 以下字段必须全部存在, 可选表示数据可为空, 必填表示如果数据为空会引发异常
    workflowid      整形, 必填, 代表绑定的流程
    model           整形(可以优化为unsigned_tinyint), 必填, 当model为0时代表全匹配, 可不填写nodeid
    nodeid          长度可变字符串, 可选, 以半角逗号(',')分割的节点数组
    custompage      长度可变字符串, 必填, 绑定页面路径, 如果文件是jsp, 仅允许头部为contentType="text/html;charset=UTF-8" language="java"
    file_type       整形, 可选, 区分文件类型, 通过不同方式加载, 详情见下方说明
    load_order      整形, 可选, 数值小则优先加载, null值根据数据库规则(最小或最大)
    disable         整形, 可选, 是否禁用, 为1代表禁用, 用于临时测试
    describe        长度可变字符串, 可选, 用作前端查看时描述custompage作用, 该字段不会被代码读取或修改
    uuid            长度可变字符串, 请保证该字段存在, 但不要使用, 重构自动创建数据时会一次性地使用该字段

    # 部分字段详细说明
    ## model模式匹配
    case 0: 全节点匹配
    case 1: 指定节点匹配, nodeid以半角逗号','分隔, case1优先级高于case2
    case 2: 指定节点排除, nodeid以半角逗号','分隔

    ## file_type文件类型
    case 1: jsp
    case 2: js
    case 3: css
    default: jsp

    ## load_order加载顺序
    越小越优先加载, order相同情况下顺序随机
    字段为null时根据数据库规则排序(可能会排在最前也可能排在最后, 根据实际数据库而定)
    不推荐为空

    注意: 不会校验custompage重复
    jsp会通过<jsp:include/>标签加载, 不存在变量名重复及头部声明重复的问题

    -- END --
--%>
<%
    RecordSet rs = new RecordSet();
    int workflowid = Util.getIntValue(request.getParameter("workflowid"));
    int nodeid = Util.getIntValue(request.getParameter("nodeid"));
    rs.execute("select model,nodeid,custompage,file_type from uf_cprouter where (disable<>1 or disable is null or disable='') and workflowid=" + workflowid + " order by load_order asc");// 这玩意可以用缓存...

    while (rs.next()) {
        // 是否生效
        int model = rs.getInt("model");
        String nodeGroup = rs.getString("nodeid");

        if (model == 0
                || (model == 1 && StringUtils.isInclude(nodeGroup, nodeid))
                || (model == 2 && !StringUtils.isInclude(nodeGroup, nodeid))) {
            String path = rs.getString("custompage");
            int fileType = rs.getInt("file_type");
            switch (fileType) {
                case 1:
                default: {
%>
<jsp:include page="<%=path%>"/>
<%
    }
    break;
    case 2: {
%>
<script src="<%=path%>"></script>
<%
    }
    break;
    case 3: {
%>
<link href="<%=path%>" type="text/css" rel="stylesheet">
<%
                }
                break;
            }
        }
    }// record set loop tail
%>