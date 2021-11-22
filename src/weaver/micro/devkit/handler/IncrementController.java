package weaver.micro.devkit.handler;

import weaver.conn.RecordSet;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.Cast;
import weaver.micro.devkit.api.CommonAPI;
import weaver.micro.devkit.api.ModeAPI;
import weaver.micro.devkit.util.RandomUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认表结构
 * <pre>
 * + uf_increment
 *   - keyword
 *   - uuid
 * + uf_increment_dt1
 *   - incr
 *   - link
 *   - uuid
 * </pre>
 * 请注意此处的keyword有被注入的风险
 */
public final class IncrementController {

    private final int modeId;
    private final String mainTable;
    private final String detailTable;

    /* main table fields */
    private final String keyField;

    /* detail table fields */
    private final String incrField;
    private final String linkField;

    /**
     * keyword
     */
    private final String keyword;

    /**
     * @return 0 if the last value does not exist
     */
    public int getLastValue() {
        int mainId = getMainId();
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select max(" + this.incrField + ") from " + this.detailTable + " where mainid = ?",
                mainId
        ), 0);
    }

    /**
     * @return 1 if the keyword is created for the first time
     */
    public int getAndSetNewValue() {
        return this.getAndSetNewValue(null);
    }

    public int getAndSetNewValue(String link) {
        if (this.getPrevCount() == 0)
            return this.setValueFirstTime();

        int mainId = getMainId();
        String uuid = RandomUtils.UUID();
        String insertFields = "mainid, uuid, " + this.incrField;
        String insertValues = mainId + ", '" + uuid + "'," +
                " (select max(" + this.incrField + ") + 1 from " + this.detailTable + " where mainid = " + mainId + ")";

        RecordSet rs = new RecordSet();
        if (rs.execute("insert into " + this.detailTable + "(" + insertFields + ")" +
                "values(" + insertValues + ")")) {
            rs.execute("select " + this.incrField + ", id from " + this.detailTable + " where uuid = '" + uuid + "'");
            rs.next();
            int ret = rs.getInt(1);
            int id = rs.getInt(2);

            if (link != null && this.linkField != null) {
                rs.executeUpdate("update " + this.detailTable + " set " + this.linkField + " = ? where id = ?",
                        link, id);
            }

            return ret;
        }

        throw new RuntimeException("Failed to insert value");
    }

    /**
     * @return mainId
     */
    private int setKeywordFirstTime() {
        Map<String, Object> data = new HashMap<String, Object>(2);
        data.put(this.keyField, this.keyword);
        int mainId = ModeAPI.createModeData(this.mainTable, this.modeId, 1, data);
        if (mainId == -1)
            throw new RuntimeException("Cannot generate mode data, input: " + data);

        return mainId;
    }

    private int setValueFirstTime() {
        int mainId = this.getMainId();
        if (new RecordSet().execute("insert into " + this.detailTable + "(mainid, " + this.incrField + ") " +
                "values(" + mainId + ", 1)")) {
            return 1;
        }
        throw new RuntimeException("Failed to insert value for the first time");
    }

    private int getPrevCount() {
        int mainId = this.getMainId();
        return Cast.o2Integer(CommonAPI.querySingleField(
                "select count(1) from " + this.detailTable + " where mainid = " + mainId
        ), 0);
    }

    private int getMainId() {
        int mainId = Cast.o2Integer(CommonAPI.querySingleField(
                "select id from " + this.mainTable + "\n" +
                        "where " + this.keyField + " = '" + this.keyword + "'"
        ));
        return mainId != -1 ? mainId : this.setKeywordFirstTime();
    }

    public IncrementController(int modeId,
                               String mainTable,
                               String detailTable,
                               String keyField,
                               String incrField,
                               String linkField,
                               String keyword) {
        this.modeId = modeId;
        this.mainTable = mainTable;
        this.detailTable = detailTable;
        this.keyField = keyField;
        this.incrField = incrField;
        this.linkField = linkField;
        this.keyword = keyword;
    }

    public static class Builder {

        private int modeId;
        private String mainTable = "uf_increment";
        private String detailTable = "uf_increment_dt1";
        private String keyField = "keyword";
        private String incrField = "incr";
        private String linkField = "link";

        public Builder() {
        }

        public Builder setModeId(int modeId) {
            this.modeId = modeId;
            return this;
        }

        public Builder setMainTable(String mainTable) {
            this.mainTable = mainTable;
            return this;
        }

        public Builder setDetailTable(String detailTable) {
            this.detailTable = detailTable;
            return this;
        }

        public Builder setKeyField(String keyField) {
            this.keyField = keyField;
            return this;
        }

        public Builder setIncrField(String incrField) {
            this.incrField = incrField;
            return this;
        }

        public Builder setLinkField(String linkField) {
            this.linkField = linkField;
            return this;
        }

        public IncrementController build(String keyword) {
            if (this.modeId == 0) Assert.fail("Failed to build IncrementController");
            Assert.notEmpty(keyword, "Failed to build IncrementController");

            return new IncrementController(
                    this.modeId,
                    this.mainTable,
                    this.detailTable,
                    this.keyField,
                    this.incrField,
                    this.linkField,
                    keyword
            );
        }

    }

}
