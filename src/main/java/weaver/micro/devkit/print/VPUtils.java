package weaver.micro.devkit.print;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.util.ArrayIterator;
import weaver.micro.devkit.util.ReflectUtil;
import weaver.micro.devkit.util.StringUtils;

import java.lang.reflect.Method;

class VPUtils {

    public static Method getMethod(MinimumType type, Object o) throws NoSuchMethodException {
        // prop
        Class<?> calledClass = type.serializationClass();
        calledClass = calledClass == void.class ? o.getClass() : calledClass;
        String calledMethod = Assert.notEmpty(type.serializationMethod());
        Class<?>[] paramsList = type.parametersList();
        int paramsLen = paramsList.length;
        int callIndex = Assert.notNeg(type.callIndex()).intValue();
        if (callIndex > paramsLen)
            throw new IllegalArgumentException(
                    "MinimumType prop[callIndex] cannot greater than the length of prop[parametersList]."
            );

        Method m = ReflectUtil.getMethodQuietly(calledClass, calledMethod, paramsList);
        if (m == null)
            throw new NoSuchMethodException(calledClass.toString()
                    + '#' + calledMethod
                    + '(' + StringUtils.toString(ArrayIterator.of(paramsList)) + ')'
            );

        return m;
    }

}
