package weaver.micro.devkit;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @author ruan4261
 */
public final class Assert {

    private Assert() {
        throw new AssertionError("No Assert instance for you!");
    }

    /**
     * Verify that the target is not a null pointer.
     *
     * @param mes exception message, be used by npe
     * @throws NullPointerException if the target is a null pointer
     */
    public static <T> T notNull(T arg, String mes) {
        if (arg == null)
            npe(mes);

        return arg;
    }

    /**
     * Verify that the target is not a null pointer,
     * the exception message generated by this method will be 'null'.
     *
     * @throws NullPointerException if the target is a null pointer
     */
    public static <T> T notNull(T arg) {
        return notNull(arg, null);
    }

    /**
     * Verify that the content of target is not empty,
     * the exception message generated by this method will be 'null'.
     *
     * @throws NullPointerException     if the target is a null pointer
     * @throws IllegalArgumentException if the content of target is empty
     */
    public static <T> T notEmpty(T arg) {
        return notEmpty(arg, null);
    }

    /**
     * Verify that the content of target is not empty,
     * null pointer check will be performed first.
     * Support to judge the following types:
     * <ol>
     *     <li>CharSequence</li>
     *     <li>Map</li>
     *     <li>Collection</li>
     *     <li>Native Array</li>
     * </ol>
     * Other types will not do verify.
     *
     * @param mes exception message, be used by npe or iae
     * @throws NullPointerException     if the target is a null pointer
     * @throws IllegalArgumentException if the content of target is empty
     */
    public static <T> T notEmpty(T arg, String mes) {
        consume(notNull(arg, mes));
        if (arg instanceof CharSequence) {
            if (((CharSequence) arg).length() == 0)
                fail(mes);
        } else if (arg instanceof Map) {
            if (((Map<?, ?>) arg).isEmpty())
                fail(mes);
        } else if (arg instanceof Collection) {
            if (((Collection<?>) arg).isEmpty())
                fail(mes);
        } else if (arg.getClass().isArray()) {
            int len = Array.getLength(arg);
            if (len == 0)
                fail(mes);
        }

        return arg;
    }

    /**
     * Verify that the offset of the content of target is legal.
     * Support to judge the following types:
     * <ol>
     *     <li>CharSequence</li>
     *     <li>Map</li>
     *     <li>Collection</li>
     *     <li>Native Array</li>
     * </ol>
     * Other types will not do verify.
     *
     * @throws NullPointerException     if the target is a null pointer,
     *                                  and the exception message will be 'null'
     * @throws IllegalArgumentException if the offset is not in the size of
     *                                  the content of the target
     */
    public static <T> T legalOffset(T arg, int offset) {
        consume(notNull(arg));
        int len = -1;
        if (arg instanceof CharSequence) {
            len = ((CharSequence) arg).length();
        } else if (arg instanceof Map) {
            len = ((Map<?, ?>) arg).size();
        } else if (arg instanceof Collection) {
            len = ((Collection<?>) arg).size();
        } else if (arg.getClass().isArray()) {
            len = Array.getLength(arg);
        }

        if (len != -1 && (offset < 0 || offset >= len))
            fail("The length of sequence is " + len + ", but offset is " + offset);

        return arg;
    }

    /**
     * Verify that the target is a positive number,
     * the exception message generated by this method will be 'null'.
     *
     * @throws NullPointerException     if the target is a null pointer
     * @throws IllegalArgumentException if the target is not a positive number
     */
    public static Number notNegAndZero(Number num) {
        notNegAndZero(num, null);
        return num;
    }

    /**
     * Verify that the target is a positive number.
     *
     * @param mes exception message, be used by npe or iae
     * @throws NullPointerException     if the target is a null pointer
     * @throws IllegalArgumentException if the target is not a positive number
     */
    public static Number notNegAndZero(Number num, String mes) {
        consume(notNull(num, mes));
        if (num.intValue() <= 0)
            fail(mes);

        return num;
    }

    /**
     * Verify that the target is not a negative number,
     * the exception message generated by this method will be 'null'.
     *
     * @throws NullPointerException     if the target is a null pointer
     * @throws IllegalArgumentException if the target is a negative number
     */
    public static Number notNeg(Number num) {
        notNeg(num, null);

        return num;
    }

    /**
     * Verify that the target is not a negative number.
     *
     * @param mes exception message, be used by npe or iae
     * @throws NullPointerException     if the target is a null pointer
     * @throws IllegalArgumentException if the target is a negative number
     */
    public static Number notNeg(Number num, String mes) {
        consume(notNull(num, mes));
        if (num.intValue() < 0)
            fail(mes);

        return num;
    }

    public static Object checkArray(Object arr) {
        notNull(arr);
        if (!arr.getClass().isArray())
            fail(null);

        return arr;
    }

    public static Object checkArray(Object arr, String mes) {
        notNull(arr, mes);
        if (!arr.getClass().isArray())
            fail(mes);

        return arr;
    }

    /**
     * If an exception occurs internally, it will be thrown directly,
     * otherwise the return value will be judged.
     * If the judgement returns false that the message of thrown
     * exception will be 'null'.
     *
     * @param judgement custom verification
     * @param <T>       types of exceptions allowed to be thrown internally
     * @throws T                        thrown by the judgement
     * @throws IllegalArgumentException if the judgement returns false
     */
    public static <T extends Throwable> void judge(Judgement<T> judgement) throws T {
        if (!judgement.through())
            fail(null);
    }

    /**
     * If an exception occurs internally, it will be thrown directly,
     * otherwise the return value will be judged.
     *
     * @param judgement custom verification
     * @param mes       exception message, it is used by iae when the return value is false
     * @param <T>       types of exceptions allowed to be thrown internally
     * @throws T                        thrown by the judgement
     * @throws IllegalArgumentException if the judgement returns false
     */
    public static <T extends Throwable> void judge(Judgement<T> judgement, String mes) throws T {
        if (!judgement.through())
            fail(mes);
    }

    /**
     * Custom functional interface for judgment.
     *
     * @param <T> allowed internal exception types
     */
    public interface Judgement<T extends Throwable> {

        /**
         * Implement this method for local verification.
         * If the return value is false, a runtime exception will be thrown,
         * the return value is true means that it has passed the verification.
         *
         * @throws T internal exception
         */
        boolean through() throws T;

    }

    /**
     * @return make the method directly followed after the throw keyword
     * @throws IllegalArgumentException iae
     */
    public static IllegalArgumentException fail(String mes) throws IllegalArgumentException {
        throw new IllegalArgumentException(mes);
    }

    /**
     * @return make the method directly followed after the throw keyword
     * @throws NullPointerException npe
     */
    public static NullPointerException npe(String mes) throws NullPointerException {
        throw new NullPointerException(mes);
    }

    /**
     * @return make the method directly followed after the throw keyword
     * @throws NullPointerException rune
     */
    public static RuntimeException rune(String mes) throws RuntimeException {
        throw new RuntimeException(mes);
    }

    public static void consume(Object... ignored) {
        // do nothing
    }

}