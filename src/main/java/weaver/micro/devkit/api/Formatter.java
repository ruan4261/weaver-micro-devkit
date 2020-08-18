package weaver.micro.devkit.api;

import static weaver.micro.devkit.core.CacheBase.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 基本类型的格式化工具，尝试将入参转换为目标类型。
 * 在任何时候不会返回{@code Null}值或抛出{@code ThrowAble}实例。
 *
 * @author ruan4261
 */
public interface Formatter {

    static String toString(Object val) {
        String s;
        return val == null ? EMPTY : (s = val.toString()) == null ? EMPTY : s;
    }

    /**
     * 如果输入一个{@code BigDecimal}实例，此接口将返回一个新的{@code BigDecimal}对象。
     */
    static BigDecimal toBigDecimal(Object val) {
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
            } else return ZERO;
        } catch (ArithmeticException | NumberFormatException ignore) {
            return ZERO;
        }
    }

}
