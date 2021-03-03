package com.weaver.test.bean;

import weaver.micro.devkit.util.BeanUtil;
import weaver.micro.devkit.util.ReflectUtil;

public class ReflectDemo1 {

    public static void main(String[] args) throws NoSuchFieldException {
        People people = new Man();
        people.gender = "man";
        people.name = "john";
        people.age = 5;

        System.out.println(ReflectUtil.getProperty(people, "name"));
        System.out.println(BeanUtil.hasOwnMethod(ReflectDemo1.class, "main", String[].class));
    }

}
