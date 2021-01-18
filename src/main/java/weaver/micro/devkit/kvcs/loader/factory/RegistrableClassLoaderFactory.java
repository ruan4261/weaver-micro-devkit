package weaver.micro.devkit.kvcs.loader.factory;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.kvcs.ClassLoaderFactory;
import weaver.micro.devkit.kvcs.ClassLoaderFactoryRegister;
import weaver.micro.devkit.kvcs.ManagementStrategy;
import weaver.micro.devkit.kvcs.loader.BaseClassLoader;
import weaver.micro.devkit.kvcs.util.StringUtils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 内部维护注册器及管理策略的加载器工厂
 */
public class RegistrableClassLoaderFactory implements ClassLoaderFactory {

    private final AssociatedManagementStrategy managementStrategy;
    private final Map<String, Class<? extends BaseClassLoader>> packageMap;
    private final Map<String, Class<? extends BaseClassLoader>> classMap;

    {
        this.managementStrategy = new AssociatedManagementStrategy();
        this.packageMap = new HashMap<String, Class<? extends BaseClassLoader>>();
        this.classMap = new HashMap<String, Class<? extends BaseClassLoader>>();
    }

    /**
     * 拿到优先级最高的加载器
     * 可能会返回null, 不会抛出异常
     *
     * @param className 全限定类名
     */
    @Override
    public Class<? extends BaseClassLoader> getClassLoader(String className) {
        Class<? extends BaseClassLoader> loader = classMap.get(className);

        if (loader == null) {
            int lastDot = className.lastIndexOf('.');
            if (lastDot != -1)
                loader = getPackageClassLoader(className.substring(0, lastDot));
        }

        return loader;
    }

    /**
     * 拿到优先级最高的加载器
     * 可能会返回null, 不会抛出异常
     *
     * @param packageName 包名
     */
    @Override
    public Class<? extends BaseClassLoader> getPackageClassLoader(String packageName) {
        Class<? extends BaseClassLoader> loader = packageMap.get(packageName);

        int lastDot;
        while (loader == null && (lastDot = packageName.lastIndexOf('.')) != -1) {
            packageName = packageName.substring(0, lastDot);
            loader = packageMap.get(packageName);
        }

        return loader;
    }

    @Override
    public ManagementStrategy getAdaptableManagementStrategy() {
        return this.managementStrategy;
    }

    @Override
    public ClassLoaderFactoryRegister getRegister() {
        return this.new Register();
    }

    /**
     * 可对一个类注册包深度 n + 1 个加载器
     * 以注册key细粒度低的加载器优先
     */
    class Register implements ClassLoaderFactoryRegister {

        @Override
        public void registerClassLoader(String className, Class<? extends BaseClassLoader> loader) {
            this.checkClassName(className);
            this.checkLoader(loader);
            RegistrableClassLoaderFactory.this.classMap.put(className, loader);
        }

        @Override
        public void registerPackageLoader(String packageName, Class<? extends BaseClassLoader> loader) {
            this.checkPackageName(packageName);
            this.checkLoader(loader);
            RegistrableClassLoaderFactory.this.packageMap.put(packageName, loader);
        }

        @Override
        public Class<? extends BaseClassLoader> removeClassLoader(String name) {
            return RegistrableClassLoaderFactory.this.classMap.remove(name);
        }

        @Override
        public Class<? extends BaseClassLoader> removePackageLoader(String name) {
            return RegistrableClassLoaderFactory.this.packageMap.remove(name);
        }

        void checkLoader(Class<? extends BaseClassLoader> loader) {
            Assert.notNull(loader);
            int modifiers = loader.getModifiers();
            if (Modifier.isAbstract(modifiers))
                throw new IllegalArgumentException("Class loader can not be abstracted!");

            try {
                loader.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        /**
         * 校验类名
         *
         * @param className 全限定名
         */
        void checkClassName(String className) {
            Assert.notEmpty(className);
            try {
                int lastDot = className.lastIndexOf('.');
                if (lastDot != -1) {
                    String simpleName = className.substring(lastDot + 1);
                    StringUtils.checkJavaIdentifier(simpleName);

                    String packageName = className.substring(0, lastDot);
                    this.checkPackageName(packageName);
                } else {
                    // 以根为包的类
                    StringUtils.checkJavaIdentifier(className);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Class name [" + className + "] verification failed.", e);
            }
        }

        /**
         * 校验包名
         *
         * @param packageName 包名
         */
        void checkPackageName(String packageName) {
            Assert.notEmpty(packageName);
            try {
                int len = packageName.length();
                int prev = len;// 上一个经过的dot, 该值大于循环idx

                for (int i = len - 1; i >= 0; i--) {
                    char ch = packageName.charAt(i);
                    if (ch == '.') {
                        String curr = packageName.substring(i + 1, prev);
                        StringUtils.checkJavaIdentifier(curr);
                        prev = i;
                    }
                }

                if (prev == 0)// 包名首字符为.
                    throw new IllegalArgumentException("Java identifier can not start with '.'.");

                // 首个包名
                StringUtils.checkJavaIdentifier(packageName.substring(0, prev));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Package name [" + packageName + "] verification failed.", e);
            }
        }

    }

    class AssociatedManagementStrategy implements ManagementStrategy {

        /**
         * 判断classMap中是否存在对应加载器
         *
         * @param className 类的全限定名
         */
        @Override
        public boolean isManagedClass(String className) {
            Assert.notEmpty(className);
            if (RegistrableClassLoaderFactory.this.getClassLoader(className) != null)
                return true;

            int lastDot = className.lastIndexOf('.');
            if (lastDot == -1)
                return false;

            return RegistrableClassLoaderFactory.this.getPackageClassLoader(className.substring(0, lastDot)) != null;
        }

        /**
         * 判断packageMap中是否存在对应加载器
         * 允许以层级低的包的加载器作为对应
         *
         * @param packageName 包名
         */
        @Override
        public boolean isManagedPackage(String packageName) {
            Assert.notEmpty(packageName);
            return RegistrableClassLoaderFactory.this.getPackageClassLoader(packageName) != null;
        }

    }

}
