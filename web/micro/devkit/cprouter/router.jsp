<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.micro.devkit.util.StringUtils" %>
<%@ page import="weaver.micro.devkit.Cast" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
    -- START --
    全局custompage路由页面, 请在建模引擎中创建表单uf_cprouter, 并绑定模块(modeid在refactor时有用)
    custompage推荐使用绝对路径, 相对路径请以该文件(/micro/devkit/cprouter/router.jsp)为基准

    # 表结构
    表名: uf_cprouter
    字段: (请全部使用小写) 以下字段必须全部存在, 可选表示数据可为空, 必填表示如果数据为空会引发异常
    workflowid      整形, 必填, 代表绑定的流程

    明细表1字段: (1.1.12版本开始使用明细表)
    model           整形(可以优化为unsigned_tinyint), 必填, 当model为0时代表全匹配, 可不填写nodeid
    nodeid          长度可变字符串, 可选, 以半角逗号(',')分割的节点数组
    custompage      长度可变字符串, 必填, 绑定页面路径, 如果文件是jsp, 仅允许头部为contentType="text/html;charset=UTF-8" language="java"
    file_type       整形, 可选, 区分文件类型, 通过不同方式加载, 详情见下方说明
    load_order      整形, 可选, 数值小则优先加载, null值根据数据库规则(最小或最大)
    used4pc         整形, 必填, 为1时生效
    used4mobile     整形, 必填, 为1时生效
    disable         整形, 可选, 是否禁用(优先级高于used4pc&used4mobile), 为1代表禁用, 用于临时测试
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

    ## Enhance
    部分字段可以使用更加语义化的类型, 如浏览按钮等, 在值不变的情况下改变显示文本, 以下推荐使用:
    1. model, file_type, disable 等字段使用选择框
    2. workflowid 字段使用数据展现集成(单选), 显示流程路径名称
      > 数据展现集成SQL:
      > select id, workflowname from workflow_base
    3. nodeid 字段使用数据展现集成(多选), 显示节点名称
      > 数据展现集成SQL:
      > select b.id, b.NODENAME
      > from workflow_flownode a
      > left outer join workflow_nodebase b on a.nodeid = b.ID
      > where a.WORKFLOWID = $workflowid$
    4. used4pc 和 used4mobile 使用 checkbox 组件

    -- END --
--%>
<%
    // version >= 1.1.12
    RecordSet rs = new RecordSet();
    int workflowid = Cast.o2Integer(request.getParameter("workflowid"));
    int nodeid = Cast.o2Integer(request.getParameter("nodeid"));
    boolean isMobile = !Cast.o2String(request.getParameter("isMobile")).equals("");
    rs.execute("select dt.model, dt.nodeid, dt.custompage, dt.file_type" +
            "\nfrom uf_cprouter_dt1 dt" +
            "\nleft outer join uf_cprouter mt on mt.id = dt.mainid" +
            "\nwhere mt.workflowid = " + workflowid +
            "\nand dt." + (isMobile ? "used4mobile" : "used4pc") + " = 1" +
            "\nand (dt.disable <> 1 or dt.disable is null or dt.disable='')" +
            "\norder by dt.load_order asc");

    while (rs.next()) {
        // model + nodeid 判断是否生效
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