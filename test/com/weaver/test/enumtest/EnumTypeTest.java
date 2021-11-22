package com.weaver.test.enumtest;

import weaver.micro.devkit.util.ReflectUtils;
import weaver.micro.devkit.util.VisualPrintUtils;

import java.util.Arrays;

public class EnumTypeTest {

    enum Father{
        A,
        B;
    }

    enum Child {

    }

    public static void main(String[] args) {
        Father e = Father.A;
        System.out.println(Arrays.toString(ReflectUtils.getAllSuper(e.getClass())));
        System.out.println(e);
        VisualPrintUtils.print(e);
    }

}
