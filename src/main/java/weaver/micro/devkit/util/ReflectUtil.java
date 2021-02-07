package weaver.micro.devkit.util;

import java.lang.reflect.Field;

/**
 * @author ruan4261
 */
public final class ReflectUtil {

    private ReflectUtil() {
    }

    /**
     * 获取类本类, 超类, 实现接口
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
            if ((needSize = count + interfaces.length) > classes.length) {
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
            Class<?>[] res = new Class[count];
            System.arraycopy(classes, 0, res, 0, count);
            return ArrayUtil.delRepeat(res);
        }
    }

    /**
     * 根据名称获取字段状态
     * 包括继承下来的字段
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
     * 获取类的字段，参数为true时过滤对应关键字字段
     * 进制从低位到高位的分部如下，step=1     位值
     * public                              1
     * private                             2
     * protected                           4
     * static                              8
     * final                               16
     * synchronized                        32
     * volatile                            64
     * transient                           128
     * native                              256
     * interface                           512
     * abstract                            1024
     * strict                              2048
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

        fields = (Field[]) ArrayUtil.arrayFilter(fields, new ArrayUtil.ArrayFilter<Field>() {
            @Override
            public boolean filter(Field ele) {
                int modifier = ele.getModifiers();
                return (modifier & filter) == 0;
            }
        });
        return fields;
    }
}