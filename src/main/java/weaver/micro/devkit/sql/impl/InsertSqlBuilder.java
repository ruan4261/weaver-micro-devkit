package weaver.micro.devkit.sql.impl;

import weaver.micro.devkit.sql.SqlBuilder;
import weaver.micro.devkit.sql.Table;

import java.util.LinkedHashMap;
import java.util.Map;

public class InsertSqlBuilder implements SqlBuilder {

    private final Map<String, Object> data = new LinkedHashMap<String, Object>(8);

    @Override
    public void addTable(Table table) {
        // todo
    }

    @Override
    public String build() {
        return build(false);
    }

    @Override
    public String build(boolean colon) {
        // todo
        return "";
    }

    String buildColName() {
        StringBuilder builder = new StringBuilder(data.size() << 3);
        for (String key : data.keySet()) {
            builder.append(key).append(",");
        }

        if (builder.charAt(builder.length() - 1) == ',') builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    String buildValue() {
        StringBuilder builder = new StringBuilder(data.size() << 3);
        for (Object value : data.values()) {
            if (value instanceof CharSequence) {
                builder.append("'").append(value.toString()).append("'");
            } else if (value instanceof Number) {
                builder.append(value.toString());
            }// else do nothing

            builder.append(",");
        }

        if (builder.charAt(builder.length() - 1) == ',') builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}