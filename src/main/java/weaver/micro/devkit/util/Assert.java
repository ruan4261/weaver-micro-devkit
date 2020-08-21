package weaver.micro.devkit.util;

import weaver.micro.devkit.exception.runtime.IllegalDataException;

/**
 * 一套组合拳，打的调用者找不着bug。
 *
 * @author ruan4261
 */
public final class Assert {

    private Assert() {
        throw new AssertionError("No Assert instances for you!");
    }

    public static void notEmpty(String arg, String mes) {
        if (arg == null || arg.trim().length() == 0) fail(mes);
    }

    public static void notEmpty(String arg) {
        if (arg == null || arg.trim().length() == 0) fail();
    }

    public static void notNull(Object arg, String mes) {
        if (arg == null) fail(mes);
    }

    public static void notNull(Object arg) {
        if (arg == null) fail();
    }

    public static void notNegAndZero(Number num) {
        if (num.intValue() <= 0) fail();
    }

    public static void notNegAndZero(Number num, String mes) {
        if (num.intValue() <= 0) fail(mes);
    }

    public static void notNeg(Number num) {
        if (num.intValue() < 0) fail();
    }

    public static void notNeg(Number num, String mes) {
        if (num.intValue() < 0) fail(mes);
    }

    public static IllegalDataException fail() throws IllegalDataException {
        throw new IllegalDataException();
    }

    public static IllegalDataException fail(String mes) throws IllegalDataException {
        throw new IllegalDataException(mes);
    }

}
