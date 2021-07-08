package weaver.micro.devkit.print;

import weaver.micro.devkit.util.BeanUtil;

import java.lang.reflect.Field;

@MinimumType
public enum ObjectType {

    Minimum(false),
    Obj(true),
    Array(true),
    Map(true),
    Collection(true),
    NULL(false);

    private final boolean needCheckRepetition;

    ObjectType(boolean needCheckRepetition) {
        this.needCheckRepetition = needCheckRepetition;
    }

    public boolean isNeedCheckRepetition() {
        return this.needCheckRepetition;
    }

    public static ObjectType whichType(Object o) {
        if (o == null)
            return NULL;
        if (isMinimumType(o))
            return Minimum;

        Class<?> clazz = o.getClass();
        if (clazz.isArray())
            return Array;
        if (o instanceof java.util.Map)
            return Map;
        if (o instanceof java.util.Collection)
            return Collection;

        return Obj;
    }

    /**
     * It may be convert to string.
     */
    public static boolean isMinimumType(Field f) {
        if (f == null)
            return false;

        return f.isAnnotationPresent(MinimumType.class);
    }

    public static boolean isMinimumType(Object o) {
        if (o == null)
            return false;

        Class<?> clazz = o.getClass();
        if (BeanUtil.isPrimitive(clazz) ||
                o instanceof Number ||
                o instanceof CharSequence)
            return true;

        return clazz.isAnnotationPresent(MinimumType.class);
    }

    public static boolean isNeedCheckRepetition(Object o) {
        return whichType(o).isNeedCheckRepetition();
    }

}
