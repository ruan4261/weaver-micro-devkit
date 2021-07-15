package com.weaver.test.print;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.util.VisualPrintUtils;

import java.util.Arrays;

public class MinimumTypePrintTest {

    @MinimumType(
            serializationClass = Arrays.class,
            parametersList = {int[].class},
            callIndex = 1
    )
    int[] arr = new int[]{3, 2356, 1252646, 12};

    int[] arr2 = new int[]{3, 2356, 1252646, 12};

    String[] arr3 = new String[]{"fword", "furry", "fun"};

    public static void main(String[] args) throws IllegalAccessException {
        VisualPrintUtils.print(new MinimumTypePrintTest());
        System.out.println();
        System.out.println();
        VisualPrintUtils.print(new MinimumTypePrintTest());
        System.out.println();
        System.out.println();
        VisualPrintUtils.print(new MinimumTypePrintTest());
        System.out.println();
        System.out.println();
    }

}
