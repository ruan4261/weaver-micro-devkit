package weaver.micro.devkit.exception.abs;

import weaver.micro.devkit.core.CacheBase;

import java.time.LocalDateTime;

/**
 * 抽象运行时异常类，继承自{@code RuntimeException}，新增dateTime属性用于记录异常发生时间。
 *
 * @author ruan4261
 */
public abstract class AbstractRuntimeException extends RuntimeException implements CacheBase {

    private final LocalDateTime occurrenceTime;

    {
        occurrenceTime = LocalDateTime.now();
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
     * #Occurrence Time:2020-01-01 12:00:00
     */
    @Override
    public String getMessage() {
        return "#Exception Message:" + super.getMessage() + LINE_SEPARATOR +
                "#Occurrence Time:" + occurrenceTime.format(DEFAULT_DATETIME_FORMATTER) + LINE_SEPARATOR;
    }

}
