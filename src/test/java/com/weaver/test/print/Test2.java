package com.weaver.test.print;

import org.apache.log4j.Level;
import weaver.micro.devkit.util.VisualPrintUtils;

import java.io.IOException;

public class Test2 {

    /**
     * global dedup test
     */
    public static void main(String[] args) throws IllegalAccessException, IOException {
//        VisualPrintUtils.print(Level.toLevel(500)/*,
//                new PrintStream(
//                        new FileOutputStream("/Users/a4261/Downloads/a.txt"),
//                        false)*/);
        VisualPrintUtils.print(A.instance);
    }

    static class A extends Prototype {
        final static A instance = new A();
        final static A instance2 = new A();
        final static A instance3 = new A();
        final static A instance4 = new A();
        final static A instance5 = new A();
    }

    static class Prototype {
        final static Prototype proto = new A();
        final static Prototype proto2 = new A();
        final static Prototype proto3 = new A();
        final static Prototype proto4 = new A();
        final static Prototype proto5 = new A();
    }

}
