package weaver.micro.devkit.dc;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.ClassCannotResolvedException;
import weaver.micro.devkit.dc.loader.CustomClassLoader;
import weaver.micro.devkit.dc.loader.impl.CustomLocalClassLoader;
import weaver.micro.devkit.dc.loader.impl.CustomRemoteClassLoader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一入口
 * 提供动态更新可缓存的类定义及实例
 */
public class DynamicClass {

    private final static Map<String, Class<?>> CACHE = new ConcurrentHashMap<String, Class<?>>();

    public static void remove(String clazz) {
        CACHE.remove(clazz);
    }

    public static void clear() {
        CACHE.clear();
    }

    /** instance scope */

    private final String clazz;
    private final CustomClassLoader classLoader;
    private final URL location;

    public DynamicClass(String clazz) {
        this(clazz, false, null);
    }

    public DynamicClass(String clazz, boolean remote, URL location) {
        this.clazz = clazz;
        if (remote) {
            Assert.notNull(location);
            this.location = location;
            this.classLoader = (CustomClassLoader) InconsistentSingleContainer.get(CustomRemoteClassLoader.class);
        } else {
            String relativePath = clazz.replace('.', '/') + ".class";
            this.location = DynamicClass.class.getClassLoader().getResource(relativePath);
            this.classLoader = (CustomClassLoader) InconsistentSingleContainer.get(CustomLocalClassLoader.class);
        }
    }

    public Object newInstance(Class<?>[] argsClass, Object[] args) throws ClassCannotResolvedException {
        try {
            Class<?> defineClass = CACHE.get(clazz);

            if (defineClass == null) {
                Definer definer = new Definer();

                defineClass = definer.define();
                CACHE.put(clazz, defineClass);
            }

            // construct instance
            Constructor<?> constructor = defineClass.getDeclaredConstructor(argsClass);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ClassCannotResolvedException e) {
            throw e;
        } catch (Throwable e) {
            throw ClassCannotResolvedException.ThrowAutoFill(clazz, e);
        }
    }

    private class Definer {
        String clazz;
        boolean remote;

        Class<?> define() throws ClassCannotResolvedException {
            if (remote) return byRemote();
            else return byLocal();
        }

        private Class<?> byRemote() {
            return null;
        }

        private Class<?> byLocal() throws ClassCannotResolvedException {
            // localed
            URL location =

            InputStream input = null;
            try {
                input = location.openStream();
                // define
                return new CustomLocalClassLoader().define(clazz, input);
            } catch (ClassCannotResolvedException e) {
                throw e;
            } catch (Throwable e) {
                throw ClassCannotResolvedException.ThrowAutoFill(clazz, e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
    }

}
