package com.weaver.test;

import weaver.micro.devkit.util.ReflectUtils;

import java.util.Arrays;

public class ArrayInstanceOfObjectTest {

    public static void main(String[] args) {
        System.out.println(new Integer[0] instanceof Object);
        System.out.println(new Integer[0] instanceof Object[]);
        System.out.println(new int[0] instanceof Object);
        //System.out.println(new int[0] instanceof Object[]); compile error

        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(int[].class)));
        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(Integer[].class)));
        System.out.println(Arrays.toString(Integer[].class.getInterfaces()));
        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(Object[].class)));

        A[] tmp = new B[1];
        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(A[].class)));
        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(B[].class)));

        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(A[][].class)));
        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(B[][][].class)));
    }

    static class A {
    }

    static class B extends A {
    }

}
