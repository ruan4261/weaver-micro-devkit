package weaver.micro.devkit.exception.abs;

import static weaver.micro.devkit.core.CacheBase.LINE_SEPARATOR;

import static weaver.micro.devkit.core.SystemAPI.currentTimestamp;

/**
 * 抽象运行时异常类，继承自{@code RuntimeException}，新增dateTime属性用于记录异常发生时间。
 *
 * @author ruan4261
 */
public abstract class AbstractRuntimeException extends RuntimeException {

    private final long occurrenceTimestamp;

    {
        occurrenceTimestamp = currentTimestamp();
    }

    public AbstractRuntimeException() {
        super();
    }

    public AbstractRuntimeException(String message) {
        super(message);
    }

    public AbstractRuntimeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Print:
     * #Exception Message:cause xxx.
     * #Occurrence Timestamp:[timestamp]
     */
    @Override
    public String getMessage() {
        return "#Exception Message:" + super.getMessage() + LINE_SEPARATOR +
                "#Occurrence Timestamp:" + occurrenceTimestamp + LINE_SEPARATOR;
    }

}
