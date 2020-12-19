package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * 所有操作不应该更改数组内顺序
 *
 * @author ruan4261
 */
public final class ArrayUtil {

    private ArrayUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] a1, T[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        Class<?> type = a1.getClass().getComponentType();
        T[] arr = (T[]) Array.newInstance(type, a1.length + a2.length);
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static int[] concat(int[] a1, int[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        int[] arr = new int[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static char[] concat(char[] a1, char[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        char[] arr = new char[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static byte[] concat(byte[] a1, byte[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        byte[] arr = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static boolean[] concat(boolean[] a1, boolean[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        boolean[] arr = new boolean[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static float[] concat(float[] a1, float[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        float[] arr = new float[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static double[] concat(double[] a1, double[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        double[] arr = new double[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static short[] concat(short[] a1, short[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        short[] arr = new short[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    public static long[] concat(long[] a1, long[] a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        long[] arr = new long[a1.length + a2.length];
        System.arraycopy(a1, 0, arr, 0, a1.length);
        System.arraycopy(a2, 0, arr, a1.length, a2.length);
        return arr;
    }

    /**
     * 删除数组内重复元素
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] delRepeat(T[] a) {
        Assert.notNull(a);
        LinkedHashSet<T> set = new LinkedHashSet<T>(a.length, 1f);
        set.addAll(Arrays.asList(a));
        return set.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), 0));
    }

    /**
     * 改变数组大小
     * 支持扩大或缩小
     * 如果数组扩大，原元素将被全部保留于原下标位置；
     * 如果数组缩小，所有下标大于登录新数组长度的元素将被舍去。
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayExtend(T[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);

        Class<?> type = a.getClass().getComponentType();
        T[] dest = (T[]) Array.newInstance(type, newLength);

        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static <T> T[] arrayFilter(T[] a, ArrayFilter<T> filter) {
        int alive = 0;
        for (T ele : a) {
            if (filter.filter(ele)) {
                a[alive++] = ele;
            }
        }

        if (alive != a.length)
            return arrayExtend(a, alive);
        return a;
    }

    public interface ArrayFilter<T> {

        /**
         * @return 返回false的值将被过滤, true将存活
         */
        boolean filter(T ele);

    }

}
