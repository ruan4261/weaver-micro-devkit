package weaver.micro.devkit.kvcs;

/**
 * Klass Version Controller
 */
public interface VersionController {

    /**
     * 判断目标类是否可由当前版本控制器管理
     *
     * @param className 类的全限定名
     */
    boolean isManagedClass(String className);

    boolean isManagedPackage(String packageName);

    /**
     * 加载目标类
     *
     * @param name 类的全限定名
     * @throws ClassNotFoundException 无法找到加载目标类的方式
     */
    Class<?> load(String name) throws ClassNotFoundException;

    /**
     * 清除所有版本缓存
     */
    void clearAll();

    /**
     * 清除指定类缓存
     *
     * @param name 类的全限定名
     */
    void clear(String name);

}