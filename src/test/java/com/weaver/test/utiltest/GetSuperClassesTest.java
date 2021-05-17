package com.weaver.test.utiltest;

import com.weaver.test.model.MySuper1;
import weaver.general.Util;
import weaver.micro.devkit.util.ReflectUtil;

import java.security.MessageDigest;
import java.util.Arrays;

public class GetSuperClassesTest {

    public void test1(){
        System.out.println(Arrays.toString(ReflectUtil.getAllSuper(MySuper1.class)));
    }

    public static void main(String[] args) {
        System.out.println(Util.getEncrypt("name123456"));
        System.out.println(getMD5("name123456"));
    }

    //md5加密 2018-04-13 liukang
    public static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] md5Array = md5.digest();
            StringBuilder result = new StringBuilder();
            for (byte b : md5Array) {
                result.append(String.format("%02x", b));
            }
            return result.toString().toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
