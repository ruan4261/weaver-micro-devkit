package com.weaver.test.utils;

import weaver.micro.devkit.handler.ActionHandler;
import weaver.micro.devkit.util.StringUtils;
import weaver.soa.workflow.request.RequestInfo;

public class RecursionPrintDemo {

    public static void main(String[] args) {
        class a extends ActionHandler{

            /**
             * Unique construction method
             *
             * @param actionInfo
             */
            public a(String actionInfo) {
                super(actionInfo);
            }

            @Override
            public String handle(RequestInfo requestInfo) throws Throwable {
                return "anc";
            }

        }
        System.out.println(StringUtils.fullRecursionPrint(new a("test class 1")));
        System.out.println();
    }

}
