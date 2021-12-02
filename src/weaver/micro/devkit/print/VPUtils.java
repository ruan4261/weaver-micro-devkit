package weaver.micro.devkit.print;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.util.ArrayIterator;
import weaver.micro.devkit.util.ReflectUtils;
import weaver.micro.devkit.util.StringUtils;

import java.lang.reflect.Method;

class VPUtils {

    public static Method getMethod(MinimumType type, Object o) throws NoSuchMethodException {
        // prop
        Class<?> calledClass = type.serializationClass();
        calledClass = calledClass == void.class ? o.getClass() : calledClass;
        String calledMethod = type.serializationMethod();
        Class<?>[] paramsList = type.parametersList();
        int paramsLen = paramsList.length;
        int callIndex = type.callIndex();
        Assert.checkOffset(callIndex, paramsLen + 1,
                "MinimumType prop[callIndex] cannot greater than the length of prop[parametersList].");

        Method m = ReflectUtils.getMethodQuietly(calledClass, calledMethod, paramsList);
        if (m == null) throw new NoSuchMethodException(calledClass.toString()
                + '#' + calledMethod
                + '(' + StringUtils.toString(ArrayIterator.of(paramsList)) + ')'
        );

        return m;
    }

}
