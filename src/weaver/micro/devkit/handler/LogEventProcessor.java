package weaver.micro.devkit.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import weaver.micro.devkit.print.ObjectDepthOverflowException;
import weaver.micro.devkit.util.VisualPrintUtils;

/**
 * 该类行为等同于BaseBean, 默认使用Error模式输出日志,
 * 当模式不可用时自动切换(低重要性优先)
 * <br>
 * 实例在创建时就会确定日志的类签名
 *
 * @since 1.1.3
 */
public class LogEventProcessor implements Loggable {

    /**
     * NO_PRINT = -1;<br>
     * ALL = 0;<br>
     * TRACE = 1;<br>
     * DEBUG = 2;<br>
     * INFO = 3;<br>
     * WARN = 4;<br>
     * ERROR = 5;<br>
     * FATAL = 6;<br>
     * OFF = 7;
     */
    private int usedLevel = 5;

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

    public int getUsedLevel() {
        return usedLevel;
    }

    public LogEventProcessor setUsedLevel(int usedLevel) {
        this.usedLevel = usedLevel;
        return this;
    }

    @Override
    public void log(String mes) {
        this.internalLog(mes, null);
    }

    @Override
    public void log(Throwable throwable) {
        this.internalLog("", throwable);
    }

    @Override
    public void log(String title, Throwable throwable) {
        this.internalLog(title, throwable);
    }

    /**
     * Print with tree structure.
     */
    @Override
    public void log(Object o) {
        try {
            String mes = VisualPrintUtils.getPrintInfo(o);
            this.internalLog(mes, null);
        } catch (ObjectDepthOverflowException e) {
            this.internalLog("Visual print exception", e);
        }
    }

    void internalLog(String msg, Throwable throwable) {
        switch (this.usedLevel) {
            case 1:
                this.trace(msg, throwable);
                break;
            case 2:
                this.debug(msg, throwable);
                break;
            case 3:
                this.info(msg, throwable);
                break;
            case 4:
                this.warn(msg, throwable);
                break;
            case 5:
                this.error(msg, throwable);
                break;
            case 6:
                this.fatal(msg, throwable);
                break;
            default:
                this.retry(msg, throwable);
                break;
        }
    }

    void retry(String msg, Throwable t) {
        if (this.reset()) {
            this.internalLog(msg, t);
        }
    }

    boolean reset() {
        if (this.usedLevel == -1)
            return false;

        if (this.log.isTraceEnabled())
            this.usedLevel = 1;
        else if (this.log.isDebugEnabled())
            this.usedLevel = 2;
        else if (this.log.isInfoEnabled())
            this.usedLevel = 3;
        else if (this.log.isWarnEnabled())
            this.usedLevel = 4;
        else if (this.log.isErrorEnabled())
            this.usedLevel = 5;
        else if (this.log.isFatalEnabled())
            this.usedLevel = 6;
        else {
            this.usedLevel = -1;
            return false;
        }
        return true;
    }

    void trace(String msg, Throwable throwable) {
        if (this.log.isTraceEnabled()) {
            if (throwable == null) {
                this.log.trace(msg);
            } else {
                this.log.trace(msg, throwable);
            }
        } else this.retry(msg, throwable);
    }

    void debug(String msg, Throwable throwable) {
        if (this.log.isDebugEnabled()) {
            if (throwable == null) {
                this.log.debug(msg);
            } else {
                this.log.debug(msg, throwable);
            }
        } else this.retry(msg, throwable);
    }

    void info(String msg, Throwable throwable) {
        if (this.log.isInfoEnabled()) {
            if (throwable == null) {
                this.log.info(msg);
            } else {
                this.log.info(msg, throwable);
            }
        } else this.retry(msg, throwable);
    }

    void warn(String msg, Throwable throwable) {
        if (this.log.isWarnEnabled()) {
            if (throwable == null) {
                this.log.warn(msg);
            } else {
                this.log.warn(msg, throwable);
            }
        } else this.retry(msg, throwable);
    }

    void error(String msg, Throwable throwable) {
        if (this.log.isErrorEnabled()) {
            if (throwable == null) {
                this.log.error(msg);
            } else {
                this.log.error(msg, throwable);
            }
        } else this.retry(msg, throwable);
    }

    void fatal(String msg, Throwable throwable) {
        if (this.log.isFatalEnabled()) {
            if (throwable == null) {
                this.log.fatal(msg);
            } else {
                this.log.fatal(msg, throwable);
            }
        } else this.retry(msg, throwable);
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
