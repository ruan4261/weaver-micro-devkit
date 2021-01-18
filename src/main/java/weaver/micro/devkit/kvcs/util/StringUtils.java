package weaver.micro.devkit.kvcs.util;

import weaver.micro.devkit.Assert;

public class StringUtils {

    private StringUtils() {
    }

    /**
     * 校验一个java定义名字符串
     *
     * @param name 定义名
     */
    public static void checkJavaIdentifier(String name) {
        Assert.notEmpty(name);

        char start = name.charAt(0);
        if (!Character.isJavaIdentifierStart(start))
            throw new IllegalArgumentException("Illegal java identifier start: " + start);

        int len = name.length();
        for (int i = 1; i < len; i++) {
            char part = name.charAt(i);
            if (!Character.isJavaIdentifierPart(part))
                throw new IllegalArgumentException("Illegal java identifier part: " + part);
        }
    }

}
