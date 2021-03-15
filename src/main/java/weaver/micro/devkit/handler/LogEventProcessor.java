package weaver.micro.devkit.handler;

import weaver.general.BaseBean;
import weaver.micro.devkit.util.StringUtils;

/**
 * @since 1.1.3
 */
public class LogEventProcessor implements Loggable {

    private final BaseBean baseBean;

    LogEventProcessor() {
        this.baseBean = new BaseBean();
    }

    private final static LogEventProcessor instance = new LogEventProcessor();

    public static LogEventProcessor getInstance() {
        return instance;
    }

    @Override
    public void log(String mes) {
        writeLog(mes);
    }

    @Override
    public void log(Throwable throwable) {
        writeLog("\n" + StringUtils.makeStackTraceInfo(throwable));
    }

    @Override
    public void log(String title, Throwable throwable) {
        writeLog(title + "\n" + StringUtils.makeStackTraceInfo(throwable));
    }

    /**
     * internal use
     */
    void writeLog(String mes) {
        baseBean.writeLog(getCallerClassName(4), mes);
    }

    void writeLog(Class<?> printer, String mes) {
        baseBean.writeLog(printer.getName(), mes);
    }

    /**
     * internal use
     */
    String getCallerClassName(int arrIdx) {
        return Thread.currentThread().getStackTrace()[arrIdx].getClassName();
    }

}
