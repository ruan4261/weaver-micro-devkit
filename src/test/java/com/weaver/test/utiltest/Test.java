package com.weaver.test.utiltest;

import weaver.general.Util;

public class Test {

    public static void main(String[] args) {
        System.out.println(Util.getSeparator());
        System.out.println('\000');
        System.out.println('\u0002');
    }

}
