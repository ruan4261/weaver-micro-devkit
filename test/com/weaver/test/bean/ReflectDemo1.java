package com.weaver.test.bean;

import weaver.micro.devkit.util.BeanUtils;
import weaver.micro.devkit.util.ReflectUtils;

public class ReflectDemo1 {

    public static void main(String[] args) throws NoSuchFieldException {
        People people = new Man();
        people.gender = "man";
        people.name = "john";
        people.age = 5;

        System.out.println(ReflectUtils.getProperty(people, "name"));
        System.out.println(BeanUtils.hasOwnMethod(ReflectDemo1.class, "main", String[].class));
    }

}
