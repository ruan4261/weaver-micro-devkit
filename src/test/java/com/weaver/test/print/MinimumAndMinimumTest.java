package com.weaver.test.print;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.util.VisualPrintUtils;

public class MinimumAndMinimumTest {

    public static String a(int a) {
        return "Chrismas hohoho " + a;
    }

    /* instance */

    private int abc = 13;

    @MinimumType(
            serializationClass = MinimumAndMinimumTest.class,
            serializationMethod = "a",
            parametersList = {int.class},
            callIndex = 1
    )
    private int abc2 = 13;

    public static void main(String[] args) {
        VisualPrintUtils.print(new MinimumAndMinimumTest());
    }

}
