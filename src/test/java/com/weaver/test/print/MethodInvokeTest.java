package com.weaver.test.print;

import weaver.micro.devkit.util.ReflectUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvokeTest {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        Child1 a = new Child1();
        Method m = ReflectUtil.getMethodQuietly(a.getClass(), "a");
        m.setAccessible(true);
        System.out.println(m.invoke(a));
    }

}
