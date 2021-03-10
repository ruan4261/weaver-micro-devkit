package com.weaver.test.storage;

public class InheritThreadLocalTest {

    public static final InheritableThreadLocal<String> threadLocal = new InheritableThreadLocal<String>();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("main-" + threadLocal.get());

        class A {

        }

    }

}
