package com.weaver.test.print;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.util.VisualPrintUtils;

@MinimumType(
        serializationMethod = "p",
        parametersList = {PrintMinimumTypeClassTest.class},
        callIndex = 1
)
public class PrintMinimumTypeClassTest {

    int a = 1;
    int b = 2;

    public static void main(String[] args) {
        VisualPrintUtils.print(new PrintMinimumTypeClassTest());
    }

    private static Object p(PrintMinimumTypeClassTest o) {
        return "PrintMinimumTypeClassTest{" +
                "a=" + o.a +
                ", b=" + o.b +
                '}';
    }

    private String p2() {
        return "PrintMinimumTypeClassTest{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }

}
