package weaver.micro.devkit.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author ruan4261
 */
public class ArrayIterator<T> implements Iterator<T> {

    private final T[] arr;
    protected int idx = -1;
    protected int lastRemove = -1;

    /**
     * For child class.
     */
    private ArrayIterator() {
        this.arr = null;
    }

    private ArrayIterator(T[] arr) {
        this.arr = arr;
    }

    public static <T> ArrayIterator<T> of(T[] arr) {
        return new ArrayIterator<T>(arr);
    }

    public static ArrayIterator<Integer> of(int[] arr) {
        return new IntArrayIterator(arr);
    }

    public static ArrayIterator<Byte> of(byte[] arr) {
        return new ByteArrayIterator(arr);
    }

    public static ArrayIterator<Float> of(float[] arr) {
        return new FloatArrayIterator(arr);
    }

    public static ArrayIterator<Double> of(double[] arr) {
        return new DoubleArrayIterator(arr);
    }

    public static ArrayIterator<Short> of(short[] arr) {
        return new ShortArrayIterator(arr);
    }

    public static ArrayIterator<Long> of(long[] arr) {
        return new LongArrayIterator(arr);
    }

    public static ArrayIterator<Boolean> of(boolean[] arr) {
        return new BoolArrayIterator(arr);
    }

    public static ArrayIterator<Character> of(char[] arr) {
        return new CharArrayIterator(arr);
    }

    @SuppressWarnings("all")
    protected int length() {
        return this.arr.length;
    }

    @SuppressWarnings("all")
    protected void set(T val) {
        this.arr[this.idx] = val;
    }

    @SuppressWarnings("all")
    protected T get() {
        return this.arr[this.idx];
    }

    @Override
    public boolean hasNext() {
        return this.idx + 1 < this.length();
    }

    @Override
    public T next() {
        if (!hasNext())
            throw new NoSuchElementException();

        ++this.idx;
        return this.get();
    }

    @Override
    public void remove() {
        if (this.lastRemove == this.idx)
            throw new IllegalStateException();

        this.set(null);
        this.lastRemove = this.idx;
    }

    private static class IntArrayIterator extends ArrayIterator<Integer> {

        final int[] arr;

        public IntArrayIterator(int[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Integer val) {
            this.arr[this.idx] = val == null ? 0 : val;
        }

        @Override
        protected Integer get() {
            return this.arr[this.idx];
        }

    }

    private static class ByteArrayIterator extends ArrayIterator<Byte> {

        final byte[] arr;

        public ByteArrayIterator(byte[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Byte val) {
            this.arr[this.idx] = val == null ? 0 : val;
        }

        @Override
        protected Byte get() {
            return this.arr[this.idx];
        }

    }

    private static class FloatArrayIterator extends ArrayIterator<Float> {

        final float[] arr;

        public FloatArrayIterator(float[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Float val) {
            this.arr[this.idx] = val == null ? 0.0f : val;
        }

        @Override
        protected Float get() {
            return this.arr[this.idx];
        }

    }

    private static class DoubleArrayIterator extends ArrayIterator<Double> {

        final double[] arr;

        public DoubleArrayIterator(double[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Double val) {
            this.arr[this.idx] = val == null ? 0.0d : val;
        }

        @Override
        protected Double get() {
            return this.arr[this.idx];
        }

    }

    private static class ShortArrayIterator extends ArrayIterator<Short> {

        final short[] arr;

        public ShortArrayIterator(short[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Short val) {
            this.arr[this.idx] = val == null ? 0 : val;
        }

        @Override
        protected Short get() {
            return this.arr[this.idx];
        }

    }

    private static class LongArrayIterator extends ArrayIterator<Long> {

        final long[] arr;

        public LongArrayIterator(long[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Long val) {
            this.arr[this.idx] = val == null ? 0L : val;
        }

        @Override
        protected Long get() {
            return this.arr[this.idx];
        }

    }

    private static class CharArrayIterator extends ArrayIterator<Character> {

        final char[] arr;

        public CharArrayIterator(char[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Character val) {
            this.arr[this.idx] = val == null ? '\u0000' : val;
        }

        @Override
        protected Character get() {
            return this.arr[this.idx];
        }

    }

    private static class BoolArrayIterator extends ArrayIterator<Boolean> {

        final boolean[] arr;

        public BoolArrayIterator(boolean[] arr) {
            super();
            this.arr = arr;
        }

        @Override
        protected int length() {
            return this.arr.length;
        }

        @Override
        protected void set(Boolean val) {
            this.arr[this.idx] = val != null && val;
        }

        @Override
        protected Boolean get() {
            return this.arr[this.idx];
        }

    }

}
