package weaver.micro.devkit.kvcs;

public interface ManagementStrategy {

    boolean isManagedClass(String className);

    boolean isManagedPackage(String packageName);

}
