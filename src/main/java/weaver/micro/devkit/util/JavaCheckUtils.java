package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

/**
 * @author ruan4261
 * @since 1.1.9
 */
public class JavaCheckUtils {

    private JavaCheckUtils() {
    }

    /**
     * Check whether the string conforms to the java naming convention.
     */
    public static void checkJavaIdentifier(String name) {
        Assert.notEmpty(name, "Illegal java identifier: (null or empty string)");
        checkJavaIdentifier(name.toCharArray(), 0, name.length());
    }

    /**
     * Check whether the string in the specified range conforms to the java naming convention.
     */
    public static void checkJavaIdentifier(final char[] value, final int offset, final int length) {
        Assert.judge(new Assert.Judgement<RuntimeException>() {
            @Override
            public boolean through() {
                return value != null
                        && value.length != 0
                        && offset >= 0
                        && length > 0
                        && offset + length <= value.length;
            }
        });

        char start = value[0];
        if (!Character.isJavaIdentifierStart(start))
            throw new IllegalArgumentException("Illegal java identifier start: " + start);

        int limit = offset + length;
        for (int i = offset + 1; i < limit; i++) {
            char part = value[i];
            if (!Character.isJavaIdentifierPart(part))
                throw new IllegalArgumentException("Illegal java identifier part: " + part);
        }
    }

    /**
     * Check whether the string conforms to the java package naming convention,
     * it should start with a letter and end with a letter, dot can only appear internally.
     */
    public static void checkPackageName(String name) {
        Assert.notEmpty(name, "Illegal package name: (null or empty string)");
        char[] value = name.toCharArray();

        try {
            int len = value.length;
            int prev = len;// the previous dot char passed in the loop

            for (int i = len - 1; i >= 0; i--) {
                char ch = value[i];
                if (ch == '.') {
                    checkJavaIdentifier(value, i + 1, prev - i - 1);
                    prev = i;
                }
            }

            if (prev == 0)// first char is dot
                throw new IllegalArgumentException("Package name can not start with '.'");

            // check the package name of the root
            checkJavaIdentifier(value, 0, prev);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal package name: " + name, e);
        }
    }

    /**
     * Check whether the string conforms to the java class naming convention,
     * it can be a full class name or a simple class name, array type does not support.
     */
    public static void checkClassName(String name) {
        Assert.notEmpty(name, "Illegal class name: (null or empty string)");
        char[] value = name.toCharArray();

        try {
            int prev = value.length;// the previous dot char passed in the loop

            do {
                int doti = name.lastIndexOf('.', prev - 1);

                if (doti == -1) {
                    checkJavaIdentifier(value, 0, prev);
                } else {
                    checkJavaIdentifier(value, doti + 1, prev - doti - 1);
                }

                prev = doti;
            } while (prev >= 0);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Illegal class name: " + name, e);
        }
    }

}
