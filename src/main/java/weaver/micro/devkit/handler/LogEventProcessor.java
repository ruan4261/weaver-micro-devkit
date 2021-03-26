package weaver.micro.devkit.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import weaver.micro.devkit.util.StringUtils;

/**
 * 实例在创建时就会确定日志的类签名
 *
 * @since 1.1.3
 */
public class LogEventProcessor implements Loggable {

    LogEventProcessor(Log log) {
        this.log = log;
    }

    private final Log log;

    /**
     * @since 1.1.3
     */
    public static LogEventProcessor getInstance() {
        StackTraceElement caller = getCallerClassName(1);
        String callerName = caller.getClassName();
        return getInstance(callerName);
    }

    /**
     * @since 1.1.4
     */
    public static LogEventProcessor getInstance(String name) {
        return new LogEventProcessor(LogFactory.getLog(name));
    }

    /**
     * @since 1.1.4
     */
    public static LogEventProcessor getInstance(Class<?> clazz) {
        return new LogEventProcessor(LogFactory.getLog(clazz));
    }

    @Override
    public void log(String mes) {
        this.internalLog(mes);
    }

    @Override
    public void log(Throwable throwable) {
        String mes = "\n" + StringUtils.toString(throwable);
        this.internalLog(mes);
    }

    @Override
    public void log(String title, Throwable throwable) {
        String mes = title + "\n" + StringUtils.toString(throwable);
        this.internalLog(mes);
    }

    /**
     * Full recursion.
     */
    @Override
    public void log(Object o) {
        String mes = StringUtils.fullRecursionPrint(o);
        this.internalLog(mes);
    }

    void internalLog(String msg) {
        this.log.error(msg);
    }

    /**
     * internal use
     * 入参为0代表调用该方法的栈帧
     * 入参为-1代表当前这个栈帧
     */
    static StackTraceElement getCallerClassName(int arrIdx) {
        arrIdx += 2;
        return Thread.currentThread().getStackTrace()[arrIdx];
    }

}
