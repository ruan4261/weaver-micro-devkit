package weaver.micro.devkit.util;

import weaver.micro.devkit.exception.runtime.IllegalDataException;

import java.math.BigDecimal;
import java.math.BigInteger;

import static weaver.micro.devkit.core.CacheBase.*;

/**
 * 强制转换数据类型，你可以通过选定默认值来避免抛出异常。
 *
 * @author ruan4261
 */
public final class Cast {

    private Cast() {
        throw new AssertionError("No Cast instances for you!");
    }

    /**
     * 喜闻乐见空字符串，参数为NULL时返回空字符串
     */
    public static String toString(Object val) {
        String s;
        return val == null ? EMPTY : (s = val.toString()) == null ? EMPTY : s;
    }

    /**
     * 参数为NULL时返回'NULL'字符串
     */
    public static String toStringNullable(Object val) {
        return val == null ? NULL : toString(val);
    }

    /**
     * @throws IllegalDataException 转换不了就抛这玩意
     */
    public static BigDecimal toBigDecimal(Object val) throws IllegalDataException {
        Assert.notNull(val);
        try {
            if (val instanceof Integer) {
                return new BigDecimal((Integer) val);
            } else if (val instanceof Long) {
                return new BigDecimal((Long) val);
            } else if (val instanceof BigInteger) {
                return new BigDecimal((BigInteger) val);
            } else if (val instanceof Number) {
                return new BigDecimal(val.toString());
            } else if (val instanceof String) {
                return new BigDecimal((String) val);
            }
        } catch (ArithmeticException | NumberFormatException e) {
            throw Assert.fail(val.toString() + " cannot cast to java.math.BigDecimal.");
        }
        throw Assert.fail(val.getClass().getTypeName() + " cannot cast to java.math.BigDecimal.");
    }

    public static BigDecimal toBigDecimal(Object val, BigDecimal decimal) {
        if (val == null) return decimal;
        try {
            return toBigDecimal(val);
        } catch (IllegalDataException ignore) {
            return decimal;
        }
    }

    public static Integer toInteger(Object val) throws IllegalDataException {
        Assert.notNull(val);
        try {
            if (val instanceof Number) {
                return ((Number) val).intValue();
            } else if (val instanceof String) {
                return Integer.parseInt((String) val);
            }
        } catch (NumberFormatException e) {
            throw Assert.fail(val.toString() + " cannot cast to java.lang.Integer.");
        }
        throw Assert.fail(val.getClass().getTypeName() + " cannot cast to java.lang.Integer.");
    }

    public static Integer toInteger(Object val, Integer integer) {
        if (val == null) return integer;
        try {
            return toInteger(val);
        } catch (IllegalDataException ignore) {
            return integer;
        }
    }
}
