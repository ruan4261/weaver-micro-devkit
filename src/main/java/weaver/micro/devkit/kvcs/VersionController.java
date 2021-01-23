package weaver.micro.devkit.kvcs;

/**
 * Klass Version Controller
 */
public interface VersionController extends ManagementStrategy {

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
     * 卸载指定类的当前版本
     * 同时会卸载依赖当前类的其他类
     *
     * @param name 类的全限定名
     */
    void unload(String name);

    /**
     * 清除所有版本缓存
     */
    void clear();

    /**
     * 排除指定类的管理
     * 通常用来排除包内接口
     * 权限高于注册器
     *
     * @param name 类的全限定名
     */
    void excludeClass(String name);

    /**
     * 取消排除指定类的管理
     * 与{@link #excludeClass(String)}相对应
     *
     * @param name 类的全限定名
     */
    void cancelExclusion(String name);
}