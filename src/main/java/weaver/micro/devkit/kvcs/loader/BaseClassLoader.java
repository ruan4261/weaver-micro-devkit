package weaver.micro.devkit.kvcs.loader;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.kvcs.VersionController;

/**
 * kvcs内部加载器基类
 * 所有管理类加载将使用自定义的委派机制
 * 支持类加载器的并行加载
 */
public abstract class BaseClassLoader extends ClassLoader {

    /**
     * 当前加载器实例所属版本管理器
     */
    private VersionController controller;

    /**
     * 当前加载器定义类
     */
    private String own;

    /**
     * 无参构造主要是方便newInstance
     */
    public BaseClassLoader() {
        this.controller = null;
        this.own = null;
    }

    public void init(VersionController controller, String own) {
        this.checkAlreadyInit();
        Assert.notNull(controller);
        Assert.notNull(own);
        this.controller = controller;
        this.own = own;
    }

    private void checkAlreadyInit() {
        if (this.controller != null || this.own != null)
            throw new IllegalArgumentException("Repeat init!");
    }

    private void initCheck() {
        Assert.notNull(this.controller);
        Assert.notNull(this.own);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        this.initCheck();
        Assert.notNull(name);

        // 依赖类, 委派给所属版本管理器进行处理
        if (!name.equals(this.own))
            return this.controller.load(name);

        // own, 自行加载
        synchronized (name.intern()) {
            Class<?> c = findLoadedClass(name);

            if (c == null)// miss current loader in jvm
                c = findClass(name);

            return c;
        }
    }

    protected abstract Class<?> findClass(String name) throws ClassNotFoundException;

}