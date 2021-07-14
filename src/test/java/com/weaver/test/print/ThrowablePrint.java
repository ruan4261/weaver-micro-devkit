package com.weaver.test.print;

import weaver.micro.devkit.util.VisualPrintUtils;

public class ThrowablePrint {

    public static void main(String[] args) {
        Throwable t = f();
        VisualPrintUtils.print(t);
        t.printStackTrace();
        VisualPrintUtils.print(t);
    }

    static int n;

    static Throwable f() {
        if (n++ == 10)
            return new Throwable();
        return f();
    }

}
