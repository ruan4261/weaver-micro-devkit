package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class AnnotationUtils {

    /**
     * Construct by default values.
     */
    public static <T extends Annotation> T getAnnotationInstance(Class<T> type) {
        return getAnnotationInstance(type, getDefaultMemberValues(type));
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T getAnnotationInstance(Class<T> type, Map<String, Object> memberValues) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class[]{type},
                // checkReturnMemberValues
                new AnnotationInvocationHandler<T>(type, memberValues)
        );
    }

    public static Map<String, Object> getDefaultMemberValues(Class<? extends Annotation> type) {
        Assert.judge(type.isAnnotation());

        Method[] methods = type.getDeclaredMethods();
        int size = methods.length;
        Map<String, Object> defaultMemberValues = new HashMap<String, Object>(size + size >> 1);
        for (Method method : methods) {
            defaultMemberValues.put(method.getName(), method.getDefaultValue());
        }
        return defaultMemberValues;
    }

    /* Internal used API */

    static Map<String, Object> checkReturnMemberValues(Class<? extends Annotation> type, Map<String, Object> memberValues) {
        Assert.notNull(type, "Annotation type is null");
        Assert.judge(type.isAnnotation(), "Incorrect annotation type");
        Method[] members = type.getDeclaredMethods();
        if (members.length == 0) {
            return java.util.Collections.emptyMap();
        }

        Assert.notEmpty(memberValues, "Member values mapping is empty.");
        int size = memberValues.size();
        Map<String, Object> tmpMapping = new HashMap<String, Object>(size + size >> 1);

        for (Method member : members) {
            String name = member.getName();
            Class<?> returnType = member.getReturnType();

            Object returnValue = memberValues.get(name);
            if (!returnType.isInstance(returnValue) && !returnType.isPrimitive()) {
                throw new IllegalArgumentException("Incorrect member value [" + name + "], " +
                        "need: " + returnType.toString() + ", " +
                        "actual object: " + StringUtils.toStringNative(returnValue));
            }

            tmpMapping.put(name, returnValue);
        }

        return weaver.micro.devkit.util.Collections.immutableMap(tmpMapping);
    }

}
