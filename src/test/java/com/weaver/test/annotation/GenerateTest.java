package com.weaver.test.annotation;

import weaver.micro.devkit.util.AnnotationUtils;
import weaver.micro.devkit.util.ReflectUtil;

import java.lang.annotation.*;
import java.util.Arrays;

public class GenerateTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD})
    @interface DemoAnnotation {
    }

    @DemoAnnotation
    int a = 1;

    public static void main(String[] args) throws NoSuchFieldException {
        DemoAnnotation original = GenerateTest.class.getDeclaredField("a").getAnnotation(DemoAnnotation.class);
        DemoAnnotation annotation = AnnotationUtils.getAnnotationInstance(DemoAnnotation.class);
        System.out.println(original.annotationType());
        System.out.println(annotation.annotationType());
        System.out.println(original.getClass());
        System.out.println(annotation.getClass());
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(original.getClass())));
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(annotation.getClass())));
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(DemoAnnotation.class)));
    }

}
