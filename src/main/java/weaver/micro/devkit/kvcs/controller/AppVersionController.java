package weaver.micro.devkit.kvcs.controller;

import weaver.micro.devkit.kvcs.ManagementStrategy;
import weaver.micro.devkit.kvcs.ClassLoaderFactory;
import weaver.micro.devkit.kvcs.VersionController;
import weaver.micro.devkit.kvcs.loader.BaseClassLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对于每个类, 都使用单独的类加载器进行定义, 保证更新影响到其他依赖其的类 todo 可行性待验证
 *
 * @author ruan4261
 */
public class AppVersionController implements VersionController {

    private AppVersionController() {
    }

    public static AppVersionController getInstance(ManagementStrategy managementStrategy) {
        AppVersionController manager = new AppVersionController();
        manager.managementStrategy = managementStrategy;
        return manager;
    }

    /* ---------------------------- Instance scope ---------------------------- */

    /**
     * 当前版本管理器主要缓存
     */
    private final Map<String, BaseClassLoader> versionCache = new ConcurrentHashMap<String, BaseClassLoader>();

    /**
     * 管理策略, 用于判断接受加载的目标类可否通过当前版本控制器进行管理
     */
    private ManagementStrategy managementStrategy;

    /**
     * 类加载器配对注册中心
     */
    private ClassLoaderFactory classLoaderFactory;

    /**
     * 加载器接口, 不会返回空值, 仅会抛异常
     * 先判断目标类能否进行管理, 并优先提供缓存
     * 如缓存不存在, 通过提前配置的注册器进行加载, 并将结果加入缓存
     */
    @Override
    public Class<?> load(String name) throws ClassNotFoundException {
        if (this.isManaged(name)) {
            BaseClassLoader cache = versionCache.get(name);
            if (cache == null) {
                // get target class loader
                // todo
            }
            return cache.loadClass(name);
        } else return this.externalLoad(name);// unable to manage, delegate to external method
    }

    /**
     * 目标类是否被当前版本管理器管理
     */
    @Override
    public boolean isManaged(String name) {
        return this.managementStrategy.isManaged(name);
    }

    /**
     * 无法管理, 使用系统的双亲委派机制
     */
    protected Class<?> externalLoad(String name) throws ClassNotFoundException {
        return ClassLoader.getSystemClassLoader().loadClass(name);
    }

}
