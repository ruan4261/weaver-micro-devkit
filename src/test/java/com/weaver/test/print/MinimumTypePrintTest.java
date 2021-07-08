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

    public static void main(String[] args) throws IllegalAccessException {
        VisualPrintUtils.print(new MinimumTypePrintTest());
        VisualPrintUtils.print(new MinimumTypePrintTest());
        VisualPrintUtils.print(new MinimumTypePrintTest());
        VisualPrintUtils.print(new MinimumTypePrintTest());
        VisualPrintUtils.print(new MinimumTypePrintTest());
        VisualPrintUtils.print(new MinimumTypePrintTest());
        VisualPrintUtils.print(new MinimumTypePrintTest());
    }

}
