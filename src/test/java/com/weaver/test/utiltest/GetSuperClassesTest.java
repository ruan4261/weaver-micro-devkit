package com.weaver.test.utiltest;

import com.weaver.test.model.MySuper1;
import weaver.micro.devkit.util.ReflectUtil;

import java.util.Arrays;

public class GetSuperClassesTest {

    public void test1(){
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(MySuper1.class)));
    }
}
