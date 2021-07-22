package com.weaver.test.print;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.print.VisualPrintProcess;
import weaver.micro.devkit.util.VisualPrintUtils;

import java.io.IOException;
import java.util.Arrays;

public class MinimumTypePrintTest {

    @MinimumType(
            serializationClass = Arrays.class,
            parametersList = {int[].class},
            callIndex = 1
    )// the default setting by model
    int[] arr = new int[]{3, 2356, 1252646, 12};

    int[] arr2 = new int[]{3, 2356, 1252646, 12};

    String[] arr3 = new String[]{"fword", "furry", "fun"};

    public static void main(String[] args) throws IllegalAccessException, IOException {
        VisualPrintUtils.print(new MinimumTypePrintTest());
        System.out.println();
        System.out.println();
        VisualPrintProcess process = new VisualPrintProcess(System.out);
        process.print(new MinimumTypePrintTest());
    }

}
