package weaver.interfaces.micro.devkit.api;

import weaver.interfaces.micro.devkit.core.CacheBase;

import java.math.BigDecimal;

/**
 * 基本类型的格式化工具，尝试将入参转换为目标类型。
 * 在任何时候不会返回{@code Null}值或抛出{@code ThrowAble}实例。
 *
 * @author ruan4261
 */
public abstract class Formatter implements CacheBase {

    public static String toString(Object val) {
        String s;
        return val == null ? EMPTY : (s = val.toString()) == null ? EMPTY : s;
    }

    public static BigDecimal toBigDecimal(Object val) {
        try {
            if (val instanceof Number) {
                return new BigDecimal(val.toString());
            } else if (val instanceof String) {
                return new BigDecimal((String) val);
            } else return ZERO;
        } catch (ArithmeticException | NumberFormatException ignore) {
            return ZERO;
        }
    }

}
