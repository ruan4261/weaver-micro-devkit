package com.weaver.test.print;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * java -XX:MaxJavaStackTraceDepth=100000 -classpath ./ com.weaver.test.print.StackOverflowTest
 */
public class StackOverflowTest {

    static int i = 0;

    static void a() {
        int stackSize = Thread.currentThread().getStackTrace().length;
        System.out.println(++i);
        System.out.println(stackSize);
        a();
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            a();
        } catch (StackOverflowError e) {
            Class<?>[] c = getAllSuper(e.getClass());
            Method m = c[c.length - 3].getDeclaredMethod("getStackTraceDepth");
            m.setAccessible(true);
            System.err.println(m.invoke(e));
        }
    }
    public static Class<?>[] getAllSuper(Class<?> clazz) {
        Class<?>[] classes = new Class[4];

        int count = 0;
        while (clazz != null) {
            classes[count++] = clazz;
            Class<?>[] interfaces = clazz.getInterfaces();

            clazz = clazz.getSuperclass();

            int needSize;
            if ((needSize = count + interfaces.length) >= classes.length) {
                if (clazz == null)
                    classes = arrayExtend(classes, needSize);
                else
                    classes = arrayExtend(classes, needSize + 4);
            }

            System.arraycopy(interfaces, 0, classes, count, interfaces.length);
            count += interfaces.length;
        }

        if (count == classes.length)
            return delRepeat(classes);
        else {
            return delRepeat(arrayExtend(classes, count));
        }
    }

    public static <T> T[] arrayExtend(T[] a, int newLength) {
        Class<?> type = a.getClass().getComponentType();
        T[] dest = (T[]) Array.newInstance(type, newLength);

        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static <T> T[] delRepeat(T[] a) {
        Set<T> set = new LinkedHashSet<T>(a.length);
        set.addAll(Arrays.asList(a));
        return set.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), 0));
    }
}
