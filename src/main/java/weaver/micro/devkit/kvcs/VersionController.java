package weaver.micro.devkit.kvcs;

/**
 * Klass Version Controller
 */
public interface VersionController {

    /**
     * 判断目标类是否可由当前版本控制器管理
     *
     * @param name 类的全限定名
     */
    boolean isManaged(String name);

    /**
     * 加载目标类
     *
     * @param name 类的全限定名
     * @throws ClassNotFoundException 无法找到加载目标类的方式
     */
    Class<?> load(String name) throws ClassNotFoundException;

}