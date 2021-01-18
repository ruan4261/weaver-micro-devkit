package weaver.micro.devkit.kvcs;

import weaver.micro.devkit.kvcs.loader.BaseClassLoader;

public interface ClassLoaderFactoryRegister {

    void registerClassLoader(String name, Class<? extends BaseClassLoader> loader);

    void registerPackageLoader(String name, Class<? extends BaseClassLoader> loader);

    Class<? extends BaseClassLoader> removeClassLoader(String name);

    Class<? extends BaseClassLoader> removePackageLoader(String name);

}