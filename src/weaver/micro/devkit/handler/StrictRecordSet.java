package weaver.micro.devkit.handler;

import weaver.conn.RecordSet;
import weaver.micro.devkit.DatabaseRuntimeException;

public class StrictRecordSet extends RecordSet {

    @Override
    public boolean executeProc(String s, String s1, String s2) {
        return $(super.executeProc(s, s1, s2));
    }

//    @Override
//    public boolean executeProc2(String s, String s1, String s2) {
//        return $(super.executeProc2(s, s1, s2));
//    }

//    public boolean executeProcNew(String s, String s1, List<ProcBean> list) {
//        return $(super.executeProcNew(s, s1, list));
//    }

    @Override
    public boolean executeSql(String s, boolean b, String s1, boolean b1, Object... objects) {
        return $(super.executeSql(s, b, s1, b1, objects));
    }

//    public boolean executeBatchSql(String s, List<?> list, String s1) {
//        return $(super.executeBatchSql(s, list, s1));
//    }

    @Override
    public boolean executeSql(String s, String s1) {
        return $(super.executeSql(s, s1));
    }

    @Override
    public boolean executeSqlWithDataSource(String s, String s1) {
        return $(super.executeSqlWithDataSource(s, s1));
    }

    boolean $(boolean result) {
        if (!result)
            throw new DatabaseRuntimeException();

        return true;
    }

}
