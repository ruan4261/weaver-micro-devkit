package weaver.micro.devkit.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ruan4261
 */
public final class ReflectUtil {

    private ReflectUtil() {
    }

    /**
     * 获取类本类, 超类, 实现接口<br>
     * 数组顺序是：自身->自身接口->超类->超类接口->超类的超类->超类的超类的接口->...
     */
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
                    classes = ArrayUtil.arrayExtend(classes, needSize);
                else
                    classes = ArrayUtil.arrayExtend(classes, needSize + 4);
            }

            System.arraycopy(interfaces, 0, classes, count, interfaces.length);
            count += interfaces.length;
        }

        if (count == classes.length)
            return ArrayUtil.delRepeat(classes);
        else {
            return ArrayUtil.delRepeat(ArrayUtil.arrayExtend(classes, count));
        }
    }

    /**
     * 根据名称获取字段状态<br>
     * 包括继承下来的字段<br>
     * 如果自身的字段名称与继承的字段名称相同，将选择自身的字段
     */
    public static Object getProperty(Object object, String fieldName) throws NoSuchFieldException {
        Class<?> clazz = object.getClass();

        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if (!field.isAccessible())
                    field.setAccessible(true);

                return field.get(object);
            } catch (NoSuchFieldException ignore) {
            } catch (IllegalAccessException ignore) {
            }

            clazz = clazz.getSuperclass();
        }

        throw new NoSuchFieldException(fieldName);
    }

    /**
     * 通过getter方法获取实例属性<br>
     * 使用的getter方法必须可访问()
     * 方法优先级
     * <ol>
     *     <li>getXx</li>
     *     <li>isXx</li>
     * </ol>
     */
    public static Object getPropertyWithGetter(Object object, String fieldName) throws NoSuchMethodException {
        Class<?> clazz = object.getClass();

        String pn = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method m = null;
        try {
            m = clazz.getMethod("get" + pn);
        } catch (NoSuchMethodException ignored) {
            try {
                m = clazz.getMethod("is" + pn);
            } catch (NoSuchMethodException ignored2) {
            }
        }

        if (m != null) {
            try {
                return m.invoke(object);
            } catch (IllegalAccessException e) {// no access permission
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {// internal exception
                throw new RuntimeException(e);
            }
        }

        throw new NoSuchMethodException("get" + pn + "() | is" + pn + "()");
    }

    /**
     * 获取类的字段，参数为true时过滤对应关键字字段<br>
     * 修饰符 ---- 位值<br>
     * public ---- 1<br>
     * private ---- 2<br>
     * protected ---- 4<br>
     * static ---- 8<br>
     * final ---- 16<br>
     * synchronized ---- 32<br>
     * volatile ---- 64<br>
     * transient ---- 128<br>
     * native ---- 256<br>
     * interface ---- 512<br>
     * abstract ---- 1024<br>
     * strict ---- 2048<br>
     *
     * @param filter 该参数bit对应关键字将被过滤
     * @param parent 是否获取父类的字段
     */
    public static Field[] queryFields(Class<?> clazz, final int filter, boolean parent) {
        Field[] fields = clazz.getDeclaredFields();
        if (parent) {
            clazz = clazz.getSuperclass();
            while (clazz != null) {
                fields = ArrayUtil.concat(fields, clazz.getDeclaredFields());

                clazz = clazz.getSuperclass();
            }
        }

        fields = ArrayUtil.arrayFilter(fields, new ArrayUtil.ArrayFilter<Field>() {
            @Override
            public boolean filter(Field ele) {
                int modifier = ele.getModifiers();
                return (modifier & filter) == 0;
            }
        });
        return fields;
    }
}