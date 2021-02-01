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
    workflowid  整形, 必填, 代表绑定的流程
    model       整形(可以优化为unsigned_tinyint), 必填, 当model为0时代表全匹配, 可不填写nodeid
    nodeid      长度可变字符串, 可选, 以半角逗号(',')分割的节点数组
    custompage  长度可变字符串, 必填, 绑定页面路径
    load_order  整形, 可选, 数值小则优先加载, 空值默认为-1
    disable     整形, 是否禁用, 为1代表禁用, 用于临时测试
    describe    长度可变字符串, 可选, 用作前端查看时描述custompage作用, 该字段不会被代码读取或修改
    uuid        长度可变字符串, 重构自动创建数据时会一次性地使用该字段

    # 部分字段详细说明
    ## model模式匹配
    case 0: 全节点匹配
    case 1: 指定节点匹配, nodeid以半角逗号','分隔, case1优先级高于case2
    case 2: 指定节点排除, nodeid以半角逗号','分隔

    ## load_order加载顺序
    越小越优先加载, order相同情况下顺序随机
    字段为空时默认为-1, 不推荐为空, 会导致性能降低

    注意: 不会校验custompage重复
    通过<jsp:include/>标签轮流加载

    -- END --
--%>
<%!
    /**
     * Check whether s1 include s2
     *
     * todo 需要性能优化
     */
    public static boolean isInclude(String s1, String s2) {
        int len = s1.length();
        int limit = s2.length();

        char first = s2.charAt(0);
        char separator = ',';
        char initial = '~';
        char prev = initial;

        int effectOffset = len - limit + 1;
        for (int i = 0; i < effectOffset; i++) {
            char ch = s1.charAt(i);
            if (ch == first && (prev == initial || prev == separator)) {
                int p1 = i;
                int p2 = 0;
                while (p1 < len && p2 < limit) {
                    if (s1.charAt(p1) != s2.charAt(p2))
                        break;

                    p1++;
                    p2++;

                    if (p2 == limit && (p1 == len || s1.charAt(p1) == separator))
                        return true;
                }
            }
            prev = ch;
        }

        return false;
    }
%>
<%
    int workflowid = Util.getIntValue(request.getParameter("workflowid"));
    String nodeid = request.getParameter("nodeid");

    RecordSet rs = new RecordSet();
    rs.execute("select model,nodeid,custompage from uf_cprouter where disable<>1 and workflowid=" + workflowid + " order by load_order asc");// 这玩意可以用缓存...
    while (rs.next()) {
        String path = rs.getString("custompage");
        int model = rs.getInt("model");
        // 是否include
        boolean include = false;

        if (model == 0) {
            include = true;
        } else {
            String nodeGroup = rs.getString("nodeid");
            boolean modelMatch = isInclude(nodeGroup, nodeid);

            if ((model == 1 && modelMatch) || (model == 2 && !modelMatch))
                include = true;
        }

        if (include) {
%>
<jsp:include page="<%=path%>"/>
<%
        }
    }
%>