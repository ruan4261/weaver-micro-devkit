package weaver.micro.devkit.util;

public class MathUtils {

    /**
     * 快速幂<hr>
     * 自行控制溢出
     */
    public static long binaryPow(long base, long exponent) {
        int res = 1;
        while (exponent > 0) {
            if ((exponent & 1) == 1)
                res *= base;

            base *= base;
            exponent >>= 1;
        }
        return res;
    }

    /**
     * 欧几里得算法<hr>
     * 取最大公约数, 无需控制溢出
     */
    public static long gcd(long large, long small) {
        long tmp;
        while (true) {
            if (small > large) {
                tmp = large;
                large = small;
                small = tmp;
            }
            if (small == 0)
                return large;

            tmp = large % small;
            large = small;
            small = tmp;
        }
    }

}
