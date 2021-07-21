package com.weaver.test.enclosing;

public class Demo {

    public Demo() {
        class ConstructorLocalClass {
        }

        ConstructorLocalClass instance = new ConstructorLocalClass();
        System.out.println(instance.getClass().getEnclosingClass());
        System.out.println(instance.getClass().getEnclosingMethod());
        System.out.println(instance.getClass().getEnclosingConstructor());

        Object instance2 = new Object() {
        };
        System.out.println(instance2.getClass().getEnclosingClass());
        System.out.println(instance2.getClass().getEnclosingMethod());
        System.out.println(instance2.getClass().getEnclosingConstructor());
    }

    public static void main(String[] args) {
        class MethodLocalClass {
        }

        MethodLocalClass instance = new MethodLocalClass();
        System.out.println(instance.getClass().getEnclosingClass());
        System.out.println(instance.getClass().getEnclosingMethod());
        System.out.println(instance.getClass().getEnclosingConstructor());

        Object instance2 = new Object() {
        };
        System.out.println(instance2.getClass().getEnclosingClass());
        System.out.println(instance2.getClass().getEnclosingMethod());
        System.out.println(instance2.getClass().getEnclosingConstructor());

        System.out.println();

        Demo demo = new Demo();

        System.out.println();

        StaticInnerClass staticInnerClass = new StaticInnerClass();
        System.out.println(staticInnerClass.getClass().getEnclosingClass());
        System.out.println(staticInnerClass.getClass().getEnclosingMethod());
        System.out.println(staticInnerClass.getClass().getEnclosingConstructor());


        InnerClass innerClass = demo.new InnerClass();
        System.out.println(innerClass.getClass().getEnclosingClass());
        System.out.println(innerClass.getClass().getEnclosingMethod());
        System.out.println(innerClass.getClass().getEnclosingConstructor());
    }

    static class StaticInnerClass{

    }

    class InnerClass{

    }

}
