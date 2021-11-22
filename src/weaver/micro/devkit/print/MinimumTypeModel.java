package weaver.micro.devkit.print;

public interface MinimumTypeModel {

    boolean isMinimumType(Class<?> type);

    MinimumType get(Class<?> type);

}
