package com.weaver.test.print;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.util.VisualPrintUtils;

@MinimumType(

)
public class MinimumSelfTest {

    public static void main(String[] args) {
        MinimumType m = MinimumSelfTest.class.getAnnotation(MinimumType.class);
        System.out.println(m);
        //VisualPrintUtils.print(m);// to long
    }

}
