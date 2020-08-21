package weaver.micro.devkit.exception.runtime;

/**
 * 随时起飞，异常特别开心。
 * 该异常表明某数据输入非法，或是程序运行过程中产生了非法数据。
 * 概念比较宽泛，本包仅使用其作为断言失败时抛出的异常。
 * 你可以认为该异常涵盖的意义包括（但不限于）:
 * {@link IllegalArgumentException}
 * {@link NumberFormatException}
 * {@link NullPointerException}
 *
 * @author ruan4261
 */
public class IllegalDataException extends RuntimeException {

    public IllegalDataException() {
        super();
    }

    public IllegalDataException(String message) {
        super(message);
    }

    public IllegalDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalDataException(Throwable cause) {
        super(cause);
    }
}
