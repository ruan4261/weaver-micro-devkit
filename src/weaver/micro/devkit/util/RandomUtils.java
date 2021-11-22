package weaver.micro.devkit.util;

import java.util.Random;
import java.util.UUID;

public class RandomUtils {

    /**
     * 随机数是否允许为0, 默认禁止, 则为false情况下最小为1
     */
    final static ThreadLocal<Boolean> allowZero = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static void allowZero() {
        allowZero.set(true);
    }

    public static void notAllowZero() {
        allowZero.set(false);
    }

    public static void clear() {
        allowZero.remove();
    }

    /**
     * @param digits 期望随机数长度
     * @return 10 ^ (digits - 1) <= return value <= (10 ^ digits) - 1
     */
    public static int randomIntOfDigits(int digits) {
        if (digits < 1)
            throw new IllegalArgumentException("The digits of integer must be positive.");
        else if (digits == 1) return randomIntOfLimit(9);

        int low = (int) Math.pow(10, digits - 1);
        int up = ((int) Math.pow(10, digits)) - 1;
        int total = up - low + 1;
        return new Random(System.nanoTime()).nextInt(total) + low;
    }

    /**
     * @param limit 期望最大随机数
     * @return return value <= limit
     */
    public static int randomIntOfLimit(int limit) {
        int low = allowZero.get() ? 0 : 1;
        int p = 1 - low;
        return new Random(System.nanoTime()).nextInt(limit + p) + low;
    }

    /**
     * @since 1.1.5
     */
    public static String UUID() {
        return UUID.randomUUID().toString();
    }

    public static String UUID32() {
        return UUID().replaceAll("-", "");
    }

}
