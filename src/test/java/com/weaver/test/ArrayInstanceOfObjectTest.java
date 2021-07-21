package com.weaver.test;

import weaver.micro.devkit.util.ReflectUtil;

import java.util.Arrays;

public class ArrayInstanceOfObjectTest {

    public static void main(String[] args) {
        System.out.println(new Integer[0] instanceof Object);
        System.out.println(new Integer[0] instanceof Object[]);
        System.out.println(new int[0] instanceof Object);
        //System.out.println(new int[0] instanceof Object[]); compile error

        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(int[].class)));
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(Integer[].class)));
        System.out.println(Arrays.toString(Integer[].class.getInterfaces()));
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(Object[].class)));

        A[] tmp = new B[1];
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(A[].class)));
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(B[].class)));

        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(A[][].class)));
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(B[][][].class)));
    }

    static class A {
    }

    static class B extends A {
    }

}
