package weaver.micro.devkit.api;

public final class FnaAPI {

    /**
     * 查询系统标准成本中心定义表
     */
    public static String getCostCenter(int id) {
        String sql = "select name from fnacostcenter where id=" + id;
        return CommonAPI.querySingleField(sql, "name");
    }

    /**
     * 查询系统标准预算科目定义表
     */
    public static String getBudgetFeeType(int id) {
        String sql = "select name from fnabudgetfeetype where id=" + id;
        return CommonAPI.querySingleField(sql, "name");
    }

}
