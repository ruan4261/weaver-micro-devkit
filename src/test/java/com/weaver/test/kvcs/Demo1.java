package com.weaver.test.kvcs;

import weaver.micro.devkit.kvcs.ClassLoaderFactory;
import weaver.micro.devkit.kvcs.ClassLoaderFactoryRegister;
import weaver.micro.devkit.kvcs.loader.factory.RegistrableClassLoaderFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Demo1 {

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassLoaderFactory factory = new RegistrableClassLoaderFactory();
        ClassLoaderFactoryRegister register = factory.getRegister();
        Class<? extends ClassLoaderFactoryRegister> klass = register.getClass();
        Method method = klass.getDeclaredMethod("checkPackageName", String.class);
        method.setAccessible(true);

        try {
            method.invoke(register, "E.a.w");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
