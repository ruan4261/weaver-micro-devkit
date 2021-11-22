package com.weaver.test.annotation;

import weaver.micro.devkit.util.AnnotationUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;

public class ToString {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD})
    @interface DemoAnnotation {

        int a() default 1;

    }

    @DemoAnnotation
    static int a;

    @DemoAnnotation(a = 2)
    static int b;

    public static void main(String[] args) throws NoSuchFieldException {
        DemoAnnotation demo1 = ToString.class.getDeclaredField("a").getAnnotation(DemoAnnotation.class);
        DemoAnnotation demo2 = ToString.class.getDeclaredField("b").getAnnotation(DemoAnnotation.class);
        DemoAnnotation demo11 = AnnotationUtils.getAnnotationInstance(DemoAnnotation.class);
        DemoAnnotation demo22 = AnnotationUtils.getAnnotationInstance(DemoAnnotation.class, new HashMap<String, Object>() {
            {
                this.put("a", 2);
            }
        });
        System.out.println(demo1.toString());
        System.out.println(demo2.toString());
        System.out.println(demo11.toString());
        System.out.println(demo22.toString());

        System.out.println(demo1.annotationType());
        System.out.println(demo2.annotationType());
        System.out.println(demo11.annotationType());
        System.out.println(demo22.annotationType());

        System.out.println(demo1.equals(demo2));
        System.out.println(demo11.equals(demo1));
        System.out.println(demo22.equals(demo2));
        System.out.println(demo22.equals(demo1));
    }

}
