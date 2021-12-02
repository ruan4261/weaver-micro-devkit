package weaver.micro.devkit.util;

import weaver.micro.devkit.Assert;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 所有操作不应该更改数组内顺序
 *
 * @author ruan4261
 */
@SuppressWarnings("all")
public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static Object concat(Object a1, Object a2) {
        Assert.notNull(a1);
        Assert.notNull(a2);
        if (!a1.getClass().isArray()
                || !a2.getClass().isArray())
            Assert.fail("argument must be an array");

        Class<?> type1 = a1.getClass().getComponentType();
        Class<?> type2 = a2.getClass().getComponentType();
        if (type1 != type2)
            Assert.fail("Inconsistent array member types!");

        int len1 = Array.getLength(a1);
        int len2 = Array.getLength(a2);

        Object dest = Array.newInstance(type1, len1 + len2);
        System.arraycopy(a1, 0, dest, 0, len1);
        System.arraycopy(a2, 0, dest, len1, len2);
        return dest;
    }

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
     * 输入数组数量必须大于0
     */
    public static <T> T[] concat(T[]... arrays) {
        Assert.notEmpty(arrays, "Illegal input");
        Class<?> arrayType = arrays[0].getClass();
        Class<?> type = arrayType.getComponentType();
        // calculate length
        int length = 0;
        for (T[] arr : arrays) {
            Assert.notNull(arr, "The array to be concat is null");
            Assert.judge(arrayType == arr.getClass(), "Incorrect array type.");
            length += arr.length;
        }

        T[] ret = (T[]) Array.newInstance(type, length);
        int idx = 0;
        for (T[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static int[] concat(int[]... arrays) {
        Assert.notNull(arrays);
        int len = 0;
        for (int[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        int[] ret = new int[len];
        int idx = 0;
        for (int[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static double[] concat(double[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (double[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        double[] ret = new double[len];
        int idx = 0;
        for (double[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static char[] concat(char[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (char[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        char[] ret = new char[len];
        int idx = 0;
        for (char[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static boolean[] concat(boolean[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (boolean[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        boolean[] ret = new boolean[len];
        int idx = 0;
        for (boolean[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static float[] concat(float[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (float[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        float[] ret = new float[len];
        int idx = 0;
        for (float[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static byte[] concat(byte[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (byte[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        byte[] ret = new byte[len];
        int idx = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }


    public static short[] concat(short[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (short[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        short[] ret = new short[len];
        int idx = 0;
        for (short[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    public static long[] concat(long[]... arrays) {
        Assert.notNull(arrays);

        int len = 0;
        for (long[] arr : arrays) {
            Assert.notNull(arr);
            len += arr.length;
        }

        long[] ret = new long[len];
        int idx = 0;
        for (long[] arr : arrays) {
            System.arraycopy(arr, 0, ret, idx, arr.length);
            idx += arr.length;
        }
        return ret;
    }

    /**
     * 仅能返回 ComponentType 为 Object 的数组
     */
    public static Object concat(Object... arrays) {
        Assert.notNull(arrays);
        int len = 0;
        for (Object arr : arrays) {
            Assert.checkArray(arr, "Component is not an instance of array type");
            len += Array.getLength(arr);
        }

        Object ret = new Object[len];

        int idx = 0;
        for (Object arr : arrays) {
            int l = Array.getLength(arr);
            System.arraycopy(arr, 0, ret, idx, l);
            idx += l;
        }
        return ret;
    }

    /**
     * 删除数组内重复元素
     */
    public static <T> T[] delRepeat(T[] a) {
        Assert.notNull(a);
        Set<T> set = new LinkedHashSet<T>(a.length);
        set.addAll(Arrays.asList(a));
        return set.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), 0));
    }

    public static Object delRepeat(Object a) {
        Assert.notNull(a);
        if (!a.getClass().isArray())
            Assert.fail("arg0 must be an array");

        int originLen = Array.getLength(a);
        Set<Object> set = new LinkedHashSet<Object>(originLen);
        for (int i = 0; i < originLen; i++)
            set.add(Array.get(a, i));

        int newLength = set.size();
        Object dest = Array.newInstance(a.getClass().getComponentType(), newLength);
        Iterator<?> it = set.iterator();
        int i = 0;
        while (it.hasNext() && i < newLength)
            Array.set(dest, i++, it.next());

        return dest;
    }

    /**
     * 改变数组大小
     * 支持扩大或缩小
     * 如果数组扩大，原元素将被全部保留于原下标位置；
     * 如果数组缩小，所有下标大于等于新数组长度的元素将被舍去。
     *
     * @return 请使用新的数组
     */
    public static <T> T[] arrayExtend(T[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);

        Class<?> type = a.getClass().getComponentType();
        T[] dest = (T[]) Array.newInstance(type, newLength);

        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static int[] arrayExtend(int[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        int[] dest = new int[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static double[] arrayExtend(double[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        double[] dest = new double[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static float[] arrayExtend(float[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        float[] dest = new float[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static char[] arrayExtend(char[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        char[] dest = new char[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static byte[] arrayExtend(byte[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        byte[] dest = new byte[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static boolean[] arrayExtend(boolean[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        boolean[] dest = new boolean[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static short[] arrayExtend(short[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        short[] dest = new short[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static long[] arrayExtend(long[] a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        long[] dest = new long[newLength];
        int size = Math.min(newLength, a.length);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static Object arrayExtend(Object a, int newLength) {
        Assert.notNull(a);
        Assert.notNeg(newLength);
        if (!a.getClass().isArray())
            Assert.fail("arg0 must be an array!");

        int originLen = Array.getLength(a);
        Class<?> componentType = a.getClass().getComponentType();
        Object dest = Array.newInstance(componentType, newLength);

        int size = Math.min(newLength, originLen);
        System.arraycopy(a, 0, dest, 0, size);
        return dest;
    }

    public static Object arrayFilter(Object a, ArrayFilter<Object> filter) {
        Assert.notNull(a);
        if (!a.getClass().isArray())
            Assert.fail("arg0 must be an array!");

        int len = Array.getLength(a);
        int alive = 0;
        for (int i = 0; i < len; i++) {
            Object ele = Array.get(a, i);
            if (filter.filter(ele)) {
                Array.set(a, alive++, ele);
            }
        }

        if (alive != len)
            return arrayExtend(a, alive);
        else
            return a;
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
        else
            return a;
    }

    public interface ArrayFilter<T> {

        /**
         * @return 返回false的值将被过滤, true将存活
         */
        boolean filter(T ele);

    }

}
