package weaver.micro.devkit.kvcs;

import weaver.micro.devkit.kvcs.loader.BaseClassLoader;

public interface ClassLoaderFactory {

    /**
     * 获取指定类的加载器
     */
    Class<? extends BaseClassLoader> getClassLoader(String name);

    /**
     * 通过工厂生成管理策略, 减少重复步骤
     */
    ManagementStrategy getAdaptableStrategy();

    interface Register {

        void registerClassLoader(String name, Class<? extends BaseClassLoader> loader);

        void registerPackageLoader(String name, Class<? extends BaseClassLoader> loader);

    }

}
