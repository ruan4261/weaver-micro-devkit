package com.weaver.test.kvcs;

import weaver.micro.devkit.kvcs.ClassLoaderFactory;
import weaver.micro.devkit.kvcs.VersionController;
import weaver.micro.devkit.kvcs.controller.AppVersionController;
import weaver.micro.devkit.kvcs.loader.ClassPathClassLoader;
import weaver.micro.devkit.kvcs.loader.factory.RegistrableClassLoaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Demo2 {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, IOException {
        ClassLoaderFactory factory = new RegistrableClassLoaderFactory();
        VersionController controller = AppVersionController.getInstance(factory);
        factory.getRegister().registerPackageLoader("com.weaver.test.kvcs", ClassPathClassLoader.class);

        loop(controller);
        controller.clearAll();

        InputStream stream = System.in;
        byte[] data = new byte[256];
        stream.read(data);
        System.out.println(Arrays.toString(data));

        loop(controller);
    }

    static void loop(VersionController controller) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Class<?> entity = controller.load("com.weaver.test.kvcs.Entity");
        Field field = entity.getDeclaredField("num");
        field.setAccessible(true);
        System.out.println(field.getInt(field));
    }

}
