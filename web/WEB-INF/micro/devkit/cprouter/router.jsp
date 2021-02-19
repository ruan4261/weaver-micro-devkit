<%@ page import="weaver.conn.RecordSet" %>
<%@ page import="weaver.general.Util" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
    -- START --
    全局custompage路由页面, 请在建模引擎中创建表单uf_cprouter, 并绑定模块(modeid在refactor时有用)
    custompage推荐使用绝对路径, 相对路径请以该文件(/micro/devkit/cprouter/router.jsp)为基准

    # 表结构
    表名: uf_cprouter
    字段: (请全部使用小写)
    workflowid      整形, 必填, 代表绑定的流程
    model           整形(可以优化为unsigned_tinyint), 必填, 当model为0时代表全匹配, 可不填写nodeid
    nodeid          长度可变字符串, 可选, 以半角逗号(',')分割的节点数组
    custompage      长度可变字符串, 必填, 绑定页面路径, 仅允许头部为contentType="text/html;charset=UTF-8" language="java"的jsp文件
    file_type       整形, 可选, 区分文件类型, 通过不同方式加载, 详情见下方说明
    load_order      整形, 可选, 数值小则优先加载, 空值默认为-1
    disable         整形, 是否禁用, 为1代表禁用, 用于临时测试
    describe        长度可变字符串, 可选, 用作前端查看时描述custompage作用, 该字段不会被代码读取或修改
    first_active    首节点是否启动, 整形, 可选
    uuid            长度可变字符串, 重构自动创建数据时会一次性地使用该字段

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
    字段为空时默认为-1, 不推荐为空, 会导致性能降低

    ## first_active首节点是否启动
    当值为空或小于1时不在首节点生效, 即正整数都代表其在首节点生效
    首节点因为ec原因, 无法获取到节点值, 故无法获取到节点值的情况下使用该条件判断是否生效
    优先级高于模式匹配(首节点仅通过该值判断, 不会进行模式匹配), 优先级低于disable

    注意: 不会校验custompage重复
    jsp会通过<jsp:include/>标签加载, 请注意头部声明

    -- END --
--%>
<%!
    /**
     * Check whether s1 include s2(val.toString())
     */
    boolean isInclude(String s1, int val) {
        int offset = 0;
        int len = s1.length();
        String s2 = Integer.toString(val);
        int wid = s2.length();
        int lastPossible = len - wid;

        while (offset <= lastPossible) {
            offset = s1.indexOf(s2, offset);
            if (offset == -1)
                return false;

            int end = offset + wid;
            // check start
            if (offset > 0) {
                char prev = s1.charAt(offset - 1);
                if (prev >= '0' && prev <= '9') {
                    offset = end;
                    continue;
                }
            }

            // check end
            if (end < len) {
                char next = s1.charAt(end);
                if (next >= '0' && next <= '9') {
                    offset = end;
                    continue;
                }
            }

            return true;
        }

        return false;
    }

%>
<%
    RecordSet rs = new RecordSet();
    int workflowid = Util.getIntValue(request.getParameter("workflowid"));
    int nodeid = Util.getIntValue(request.getParameter("nodeid"));
    boolean autoActive = nodeid <= 0;// 首节点通过sql判断, 所有查询结果自动生效

    if (!autoActive)
        rs.execute("select model,nodeid,custompage,file_type,first_active from uf_cprouter where disable<>1 and workflowid=" + workflowid + " order by load_order asc");// 这玩意可以用缓存...
    else
        rs.execute("select custompage,file_type from uf_cprouter where disable<>1 and first_active>0 order by load_order asc");

    while (rs.next()) {
        // 是否生效
        boolean active = autoActive;

        if (!active) {
            int model = rs.getInt("model");

            if (model == 0) {
                active = true;
            } else {
                String nodeGroup = rs.getString("nodeid");
                boolean modelMatch = isInclude(nodeGroup, nodeid);

                if ((model == 1 && modelMatch) || (model == 2 && !modelMatch))
                    active = true;
            }
        }

        if (active) {
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