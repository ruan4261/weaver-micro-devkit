package weaver.micro.devkit.kvcs;

import weaver.micro.devkit.kvcs.loader.BaseClassLoader;

public interface ClassLoaderFactory {

    /**
     * 获取指定类的加载器
     * 该方法应该可以获取所属包的加载器
     *
     * @param className 全限定类名
     */
    Class<? extends BaseClassLoader> getClassLoader(String className);

    /**
     * 获取指定包的加载器
     * 该方法不会获取到与包同名的类的加载器
     *
     * @param packageName 包名
     */
    Class<? extends BaseClassLoader> getPackageClassLoader(String packageName);

    /**
     * 通过工厂生成管理策略, 减少重复步骤
     */
    ManagementStrategy getAdaptableManagementStrategy();

    /**
     * 获取当前实例注册器接口, 可视情况实现
     */
    ClassLoaderFactoryRegister getRegister();

}
