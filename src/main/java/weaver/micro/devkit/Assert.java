package weaver.micro.devkit;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * 一套组合拳，打的调用者找不着bug。
 *
 * @author ruan4261
 */
public final class Assert {

    private Assert() {
        throw new AssertionError("No Assert instance for you!");
    }

    public static void notEmpty(Object arg) {
        notEmpty(arg, null);
    }

    /**
     * 支持判断如下类型
     * 1.CharSequence
     * 2.Map
     * 3.Collection
     * 4.Native Array
     * 不属于以上类型且不为Null时不做判断
     */
    public static void notEmpty(Object arg, String mes) {
        notNull(arg, mes);
        if (arg instanceof CharSequence) {
            if (((CharSequence) arg).length() == 0) fail(mes);
        } else if (arg instanceof Map) {
            if (((Map) arg).isEmpty()) fail(mes);
        } else if (arg instanceof Collection) {
            if (((Collection) arg).isEmpty()) fail(mes);
        } else if (arg.getClass().isArray()) {
            int len = Array.getLength(arg);
            if (len == 0) fail(mes);
        }
    }

    /**
     * 判断偏移量是否合法
     * 支持判断如下类型
     * 1.CharSequence
     * 2.Map
     * 3.Collection
     * 4.Native Array
     * 不属于以上类型且不为Null时不做判断
     */
    public static void legalOffset(Object arg, int offset) {
        notNull(arg, "Arg cannot be NULL!");
        int len = -1;
        if (arg instanceof CharSequence) {
            len = ((CharSequence) arg).length();
        } else if (arg instanceof Map) {
            len = ((Map) arg).size();
        } else if (arg instanceof Collection) {
            len = ((Collection) arg).size();
        } else if (arg.getClass().isArray()) {
            len = Array.getLength(arg);
        }

        if (len == -1) return;
        if (offset < 0 || offset >= len)
            fail("Series length is " + len + ", but offset is " + offset);
    }

    public static void notNull(Object arg, String mes) {
        if (arg == null) npe(mes);
    }

    public static void notNull(Object arg) {
        notNull(arg, null);
    }

    public static void notNegAndZero(Number num) {
        notNegAndZero(num, null);
    }

    public static void notNegAndZero(Number num, String mes) {
        notNull(num, mes);
        if (num.intValue() <= 0) fail(mes);
    }

    public static void notNeg(Number num) {
        notNeg(num, null);
    }

    public static void notNeg(Number num, String mes) {
        notNull(num, mes);
        if (num.intValue() < 0) fail(mes);
    }

    public static void judge(Judgement judgement) {
        if (judgement.judge())
            fail(null);
    }

    /**
     * 判定为true时触发IllegalDataException异常
     */
    public static void judge(Judgement judgement, String mes) {
        if (judgement.judge())
            fail(mes);
    }

    public interface Judgement {

        boolean judge();

    }

    /**
     * RuntimeException
     */
    public static RuntimeException fail(String mes) throws RuntimeException {
        throw new RuntimeException(mes);
    }

    public static NullPointerException npe(String mes) throws NullPointerException {
        throw new NullPointerException(mes);
    }
}