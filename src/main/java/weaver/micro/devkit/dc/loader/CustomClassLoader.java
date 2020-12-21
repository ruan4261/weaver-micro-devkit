package weaver.micro.devkit.dc.loader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class CustomClassLoader implements ClassDefiner {

    private final static Method DEFINE_METHOD;

    static {
        // URLClassLoader -> SecureClassLoader -> ClassLoader(Abstract)
        try {
            DEFINE_METHOD = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_METHOD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public final Class<?> invoke(ClassLoader loader, String clazz, byte[] data, int offset, int length) throws InvocationTargetException, IllegalAccessException {
        synchronized (DEFINE_METHOD) {
            return (Class<?>) DEFINE_METHOD.invoke(loader, clazz, data, offset, length);
        }
    }

}
