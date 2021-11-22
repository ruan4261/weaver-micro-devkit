package com.weaver.test.print;

import weaver.micro.devkit.print.MinimumType;
import weaver.micro.devkit.util.AnnotationUtils;
import weaver.micro.devkit.util.VisualPrintUtils;

public class PrintMinimumTypeInstanceTest {

    public static void main(String[] args) {
        VisualPrintUtils.print(AnnotationUtils.getAnnotationInstance(MinimumType.class));
    }

}
