package weaver.micro.devkit.print;

import weaver.micro.devkit.util.BeanUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@MinimumType
public enum ObjectType {

    Minimum(false),
    MinimumWithAnnotation(false),
    MinimumWithModel(false),
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

    public static ObjectType whichType(Object o, Field f) {
        return whichType(o, f, null);
    }

    public static ObjectType whichType(Object o, MinimumTypeModel modal) {
        return whichType(o, null, modal);
    }

    public static ObjectType whichType(Object o, Field f, MinimumTypeModel modal) {
        if (o == null)
            return NULL;
        if (isMinimumTypeWithAnnotation(f))
            return MinimumWithAnnotation;
        if (isMinimumTypeWithAnnotation(o.getClass()))
            return MinimumWithAnnotation;
        if (isMinimumTypeWithModel(o, modal))
            return MinimumWithModel;
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

    public static ObjectType whichType(Object o) {
        return whichType(o, null, null);
    }

    public static boolean isMinimumType(Object o) {
        if (o == null)
            return false;

        return isMinimumType(o.getClass());
    }

    public static boolean isMinimumType(Class<?> type) {
        return type != null
                && (BeanUtils.isPrimitive(type)
                || Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || Class.class.isAssignableFrom(type)
                || Enum.class.isAssignableFrom(type)
                || Annotation.class.isAssignableFrom(type));
    }

    public static boolean isMinimumTypeWithModel(Object o, MinimumTypeModel modal) {
        return o != null
                && modal != null
                && modal.isMinimumType(o.getClass());
    }

    public static boolean isMinimumTypeWithAnnotation(Field f) {
        if (f == null)
            return false;

        return f.isAnnotationPresent(MinimumType.class);
    }

    public static boolean isMinimumTypeWithAnnotation(Class<?> type) {
        return type.isAnnotationPresent(MinimumType.class);
    }

    public static boolean isNeedCheckRepetition(Object o) {
        return whichType(o).isNeedCheckRepetition();
    }

}
