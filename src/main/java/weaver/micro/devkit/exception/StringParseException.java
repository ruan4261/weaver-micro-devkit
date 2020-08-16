package weaver.micro.devkit.exception;

import weaver.micro.devkit.exception.abs.AbstractRuntimeException;

/**
 * 字符串解析异常。
 * 本实例只保存异常字符串信息。
 * 通过{@code ThrowAble}构造本异常时，才存在{@code detailMessage}信息，其由超类提供。
 *
 * @author ruan4261
 */
public class StringParseException extends AbstractRuntimeException {

    private String exceptionString;

    public StringParseException(String string) {
        super();
        this.exceptionString = string;
    }

    public StringParseException(Throwable ex, String string) {
        super(ex);
        this.exceptionString = string;
    }

    public String getExceptionString() {
        return exceptionString;
    }

    public void setExceptionString(String exceptionString) {
        this.exceptionString = exceptionString;
    }

    /**
     * Print:
     * #Exception Message:cause xxx.
     * #Occurrence Time:2020-01-01 12:00:00
     * #Exception String:abcdef
     */
    @Override
    public String getMessage() {
        return super.getMessage() + "#Exception String:" + exceptionString + LINE_SEPARATOR;
    }

}
