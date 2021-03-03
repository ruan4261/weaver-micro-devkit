package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author ruan4261
 */
public final class BeanUtil {

    public static boolean isConvertible(Class<?> ori, Class<?> dest) {
        return dest.isAssignableFrom(ori);
    }

    /**
     * 将所有实例字段作为键值<br>
     * 值为实例当前的字段状态<br>
     * 获取的字段包括父类
     *
     * @param filter 该参数bit对应关键字将被过滤
     * @see ReflectUtil#queryFields(Class, int, boolean)
     * @deprecated 如果子类有与父类同名字段, 值将取自父类字段
     */
    @Deprecated
    public static Map<String, Object> object2Map(Object object, int filter) {
        Assert.notNull(object);
        Class<?> clazz = object.getClass();

        Field[] fields = ReflectUtil.queryFields(clazz, filter, true);

        Map<String, Object> map = new HashMap<String, Object>(fields.length);
        for (Field field : fields) {
            try {
                if (!field.isAccessible())
                    field.setAccessible(true);

                map.put(field.getName(), field.get(object));
            } catch (IllegalAccessException ignore) {
            }
        }
        return map;
    }

    /**
     * 将所有实例字段作为键值<br>
     * 使用getter方法获取字段, 要求空参且访问修饰为public
     *
     * todo 当前暂不可用, 会在接下来的版本更新
     *
     * @param filter 该参数bit对应关键字将被过滤
     * @see ReflectUtil#queryFields(Class, int, boolean)
     */
    public static Map<String, Object> obj2Map(Object obj, int filter) {
        return object2Map(obj, filter);
    }

    /**
     * 填充实例对象字段
     * 类型不匹配且无法转换的字段将被跳过
     *
     * @param filter 被过滤的字段，将不会产生修改
     */
    public static <T> void fillObject(Map<String, Object> state, T object, int filter) {
        Class<?> clazz = object.getClass();
        // 遍历目标类型的字段, 从map取值
        Field[] fields = ReflectUtil.queryFields(clazz, filter, true);
        for (Field field : fields) {
            String key = field.getName();
            if (state.containsKey(key)) {
                Object value = state.get(key);
                try {
                    if (!field.isAccessible())
                        field.setAccessible(true);

                    if (value == null) {
                        field.set(object, value);
                        continue;
                    }

                    Class<?> destType = field.getType();
                    Class<?> oriType = value.getClass();
                    if (isConvertible(oriType, destType)) {
                        field.set(object, value);
                        continue;
                    }

                    if (isPrimitive(destType)) {
                        field.set(object, o2Primitive(destType, value));
                    }
                } catch (IllegalAccessException ignored) {
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }

    @SuppressWarnings("unchecked")
    public static <T> T o2Primitive(Class<T> clazz, Object val) {
        if (clazz == byte.class || clazz == Byte.class) {
            return (T) o2Byte(val);
        } else if (clazz == char.class || clazz == Character.class) {
            return (T) o2Character(val);
        } else if (clazz == int.class || clazz == Integer.class) {
            return (T) o2Integer(val);
        } else if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) o2Boolean(val);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) o2Long(val);
        } else if (clazz == double.class || clazz == Double.class) {
            return (T) o2Double(val);
        } else if (clazz == float.class || clazz == Float.class) {
            return (T) o2Float(val);
        } else if (clazz == short.class || clazz == Short.class) {
            return (T) o2Short(val);
        } else if (clazz == void.class || clazz == Void.class) {
            return null;
        }
        throw new RuntimeException(clazz.toString() + " is not a primitive class.");
    }

    /**
     * 解析失败后不会抛出异常，默认返回false
     * 如果目标类型重写了toString方法，则在LastCase下会判断toString
     */
    public static Boolean o2Boolean(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean)
            return (Boolean) val;
        if (val instanceof Number)
            return ((Number) val).intValue() > 0;
        if (val instanceof Character) {
            char c = (Character) val;
            return c == 'T'
                    || c == 't'
                    || c == 'Y'
                    || c == 'y'
                    || c == '1';
        }
        if (!(val instanceof String) && !hasOwnMethod(val.getClass(), "toString"))
            throw new RuntimeException(val.getClass().toString() + "cannot convert to Boolean.");

        String str = val.toString();
        final int len = str.length();

        char firstChar = str.charAt(0);
        if (firstChar == 't'
                || firstChar == 'T'
                || firstChar == 'y'
                || firstChar == 'Y'
                || firstChar == 's'
                || firstChar == 'S') {
            if (len == 1)
                return true;
            return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "success".equalsIgnoreCase(str);
        } else if (firstChar == '+' || (firstChar >= '0' && firstChar <= '9')) {
            try {
                return new BigDecimal(str).compareTo(BigDecimal.ZERO) > 0;
            } catch (NumberFormatException ignore) {
                return false;
            }
        } else return false;
    }

    public static Integer o2Integer(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof Boolean) return ((Boolean) val) ? 1 : 0;
        if (val instanceof Character) return val.hashCode();
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            try {
                return new BigDecimal(v).intValue();
            } catch (NumberFormatException ignored) {
                throw new RuntimeException(v + " cannot resolve to Integer.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Integer.");
    }

    public static Double o2Double(Object val) {
        if (val == null) return 0d;
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof Boolean) return ((Boolean) val) ? 1D : 0D;
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            try {
                return new BigDecimal(v).doubleValue();
            } catch (NumberFormatException ignored) {
                throw new RuntimeException(v + " cannot resolve to Double.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Double.");
    }

    public static Float o2Float(Object val) {
        if (val == null) return 0f;
        if (val instanceof Number) return ((Number) val).floatValue();
        if (val instanceof Boolean) return ((Boolean) val) ? 1F : 0F;
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            try {
                return new BigDecimal(v).floatValue();
            } catch (NumberFormatException ignored) {
                throw new RuntimeException(v + " cannot resolve to Float.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Float.");
    }

    public static Short o2Short(Object val) {
        if (val == null) return (short) 0;
        if (val instanceof Number) return ((Number) val).shortValue();
        if (val instanceof Boolean) return ((Boolean) val) ? (short) 1 : (short) 0;
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            try {
                return new BigDecimal(v).shortValue();
            } catch (NumberFormatException ignored) {
                throw new RuntimeException(v + " cannot resolve to Short.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Short.");
    }

    public static Long o2Long(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof Boolean) return ((Boolean) val) ? 1L : 0L;
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            try {
                return new BigDecimal(v).longValue();
            } catch (NumberFormatException ignored) {
                throw new RuntimeException(v + " cannot resolve to Long.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Long.");
    }

    public static Byte o2Byte(Object val) {
        if (val == null) return (byte) 0;
        if (val instanceof Number) return ((Number) val).byteValue();
        if (val instanceof Boolean) return ((Boolean) val) ? (byte) 1 : (byte) 0;
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            try {
                return new BigDecimal(v).byteValue();
            } catch (NumberFormatException ignored) {
                throw new RuntimeException(v + " cannot resolve to Byte.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Byte.");
    }

    public static Character o2Character(Object val) {
        if (val == null) return (char) 0;
        if (val instanceof Character) return (Character) val;
        if (val instanceof Number) return (char) ((Number) val).intValue();
        if (val instanceof CharSequence || hasOwnMethod(val.getClass(), "toString")) {
            String v = val.toString();
            int len = v.length();

            if (len == 1)
                return v.charAt(0);

            // unicode
            if (len == 6 && v.charAt(0) == '\\' && v.charAt(1) == 'u') {
                int unicode = 0;

                int i = 2;
                for (; i < 6; i++) {
                    int uni = v.charAt(i);
                    if (uni >= '0' && uni <= '9') {
                        unicode = (unicode << 4) + uni - '0';
                    } else if (uni >= 'a' && uni <= 'f') {
                        unicode = (unicode << 4) + uni - 'a' + 10;
                    } else if (uni >= 'A' && uni <= 'F') {
                        unicode = (unicode << 4) + uni - 'A' + 10;
                    } else break;
                }

                if (i == 6)
                    return (char) unicode;
                // go next
            }

            // num
            try {
                return (char) new BigDecimal(v).intValue();
            } catch (RuntimeException ignored) {
                throw new RuntimeException(v + " cannot resolve to Character.");
            }
        }
        throw new RuntimeException(val.getClass().toString() + " cannot convert to Byte.");
    }

    /**
     * 检查目标类型是否拥有或重写了目标方法
     * 继承的方法不算在内
     * 这个方法一般被用于检查子类是否重写了父类方法
     * <b>提醒: 方法签名仅与方法名称及参数类型有关</b>
     *
     * @param clazz      检查的目标类型
     * @param methodName 目标方法
     * @param paramTypes 方法参数类型
     * @see ReflectUtil#queryFields(Class, int, boolean) filter描述
     */
    public static <T> boolean hasOwnMethod(Class<T> clazz, String methodName, Class<?>... paramTypes) {
        Assert.notNull(clazz);
        Assert.notEmpty(methodName);

        try {
            clazz.getDeclaredMethod(methodName, paramTypes);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    /**
     * 复制对象属性
     *
     * @param origin 源
     * @param dest   目标
     * @param filter 过滤字段
     */
    public static void copyProperties(Object origin, Object dest, int filter) {
        Map<String, Object> dat = object2Map(origin, filter);
        fillObject(dat, dest, filter);
    }
}