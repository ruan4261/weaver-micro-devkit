package weaver.micro.devkit.kvcs.controller;

import weaver.micro.devkit.kvcs.ManagementStrategy;
import weaver.micro.devkit.kvcs.ClassLoaderFactory;
import weaver.micro.devkit.kvcs.VersionController;
import weaver.micro.devkit.kvcs.loader.BaseClassLoader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 对于每个类, 都使用单独的类加载器进行定义, 保证更新影响到其他依赖其的类 todo 可行性待验证
 *
 * @author ruan4261
 */
public class AppVersionController implements VersionController {

    private AppVersionController() {
    }

    public static AppVersionController getInstance(ClassLoaderFactory classLoaderFactory) {
        AppVersionController controller = new AppVersionController();
        controller.classLoaderFactory = classLoaderFactory;
        controller.managementStrategy = classLoaderFactory.getAdaptableManagementStrategy();
        return controller;
    }

    /* ---------------------------- Instance scope ---------------------------- */

    /**
     * 当前版本管理器主要缓存
     *
     * key: 全限定类名
     * value: 加载器实例
     */
    private final Map<String, BaseClassLoader> versionCache = new HashMap<String, BaseClassLoader>();

    /**
     * 排除管理的类
     */
    private final Set<String> exclusion = new HashSet<String>();

    /**
     * 管理策略, 用于判断接受加载的目标类可否通过当前版本控制器进行管理
     */
    private ManagementStrategy managementStrategy;

    /**
     * 类加载器配对注册中心
     */
    private ClassLoaderFactory classLoaderFactory;

    private final Object lock = new Object();

    /**
     * 加载器接口, 不会返回空值, 仅会抛异常
     * 先判断目标类能否进行管理, 并优先提供缓存
     * 如缓存不存在, 通过提前配置的注册器进行加载, 并将结果加入缓存
     */
    @Override
    public Class<?> load(String name) throws ClassNotFoundException {
        // has class loader and is not excluded managed
        if (this.isManagedClass(name) && !this.exclusion.contains(name)) {
            BaseClassLoader cache = this.versionCache.get(name);
            if (cache == null) {
                // no cache
                synchronized (this.lock) {
                    // get target class loader
                    Class<? extends BaseClassLoader> loader = this.classLoaderFactory.getClassLoader(name);
                    if (loader == null)
                        throw new RuntimeException("Class[" + name + "] is managed by the version controller, but can not get target loader.");

                    // new class loader instance
                    try {
                        cache = loader.newInstance();
                    } catch (InstantiationException ignored) {// 注册器应当测试类有效性
                    } catch (IllegalAccessException ignored) {// 注册器应当测试权限
                    }

                    if (cache == null)
                        throw new RuntimeException("Loader[" + loader + "] can not construct new instance.");

                    cache.init(this, name);
                    this.versionCache.put(name, cache);
                }
            }

            return cache.loadClass(name);
        } else return this.externalLoad(name);// unable to manage, delegate to external method
    }

    @Override
    public void unload(String name) {
        this.versionCache.remove(name);
    }

    @Override
    public void clear() {
        this.versionCache.clear();
    }

    @Override
    public void excludeClass(String name) {
        synchronized (this.lock) {
            this.exclusion.add(name);
            // 排除已有缓存
            this.versionCache.remove(name);
        }
    }

    @Override
    public void cancelExclusion(String name) {
        this.exclusion.remove(name);
    }

    /**
     * 目标类是否被当前版本管理器管理
     */
    @Override
    public boolean isManagedClass(String className) {
        return this.managementStrategy.isManagedClass(className);
    }

    @Override
    public boolean isManagedPackage(String packageName) {
        return this.managementStrategy.isManagedPackage(packageName);
    }

    /**
     * 无法管理, 交由外部加载
     * 该类策略为使用系统类加载器(appclassloader)
     */
    protected Class<?> externalLoad(String name) throws ClassNotFoundException {
        return ClassLoader.getSystemClassLoader().loadClass(name);
    }

}
