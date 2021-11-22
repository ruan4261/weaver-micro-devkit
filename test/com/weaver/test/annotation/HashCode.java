package com.weaver.test.annotation;

import weaver.micro.devkit.util.AnnotationUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;

public class HashCode {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.FIELD})
    @interface DemoAnnotation {

        int a() default 0;

    }

    @DemoAnnotation
    static int a;

    @DemoAnnotation(a = 1)
    static int b;

    @DemoAnnotation(a = 20000)
    static int c;

    public static void main(String[] args) throws NoSuchFieldException {
        DemoAnnotation demo1 = HashCode.class.getDeclaredField("a").getAnnotation(DemoAnnotation.class);
        DemoAnnotation demo2 = HashCode.class.getDeclaredField("b").getAnnotation(DemoAnnotation.class);
        DemoAnnotation demo3 = HashCode.class.getDeclaredField("c").getAnnotation(DemoAnnotation.class);
        DemoAnnotation demo11 = AnnotationUtils.getAnnotationInstance(DemoAnnotation.class);
        DemoAnnotation demo22 = AnnotationUtils.getAnnotationInstance(DemoAnnotation.class, new HashMap<String, Object>() {
            {
                this.put("a", 1);
            }
        });
        DemoAnnotation demo33 = AnnotationUtils.getAnnotationInstance(DemoAnnotation.class, new HashMap<String, Object>() {
            {
                this.put("a", 20000);
            }
        });
        System.out.println(demo1.hashCode());
        System.out.println(demo2.hashCode());
        System.out.println(demo3.hashCode());
        System.out.println(demo11.hashCode());
        System.out.println(demo22.hashCode());
        System.out.println(demo33.hashCode());
    }

}
