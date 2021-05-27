package com.weaver.test.print;

import weaver.micro.devkit.util.VisualPrintUtils;

public class VisualPrintTest {

    public static void main(String[] args) throws IllegalAccessException {
        class Node {
            Node child;
        }

        Node root = new Node();
        Node prev = root;
        for (int i = 0; i < 3000; i++) {
            prev.child = new Node();
            prev = prev.child;
        }

        try {
            VisualPrintUtils.print(root);
        }catch (Throwable t){
        }

        System.out.println("continue");
    }

}
