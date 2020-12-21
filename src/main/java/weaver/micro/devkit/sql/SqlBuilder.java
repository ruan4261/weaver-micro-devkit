package weaver.micro.devkit.sql;

public interface SqlBuilder {

    void addTable(Table table);

    String build();

    String build(boolean colon);
}
