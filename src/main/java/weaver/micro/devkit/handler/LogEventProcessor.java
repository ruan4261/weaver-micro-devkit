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
        this.writeLog(mes);
    }

    @Override
    public void log(Throwable throwable) {
        String mes = "\n" + StringUtils.makeStackTraceInfo(throwable);
        writeLog(mes);
    }

    @Override
    public void log(String title, Throwable throwable) {
        String mes = title + "\n" + StringUtils.makeStackTraceInfo(throwable);
        writeLog(mes);
    }

    /**
     * internal use
     */
    void writeLog(String mes) {
        StackTraceElement caller = getCallerClassName(2);
        String callerName = caller.getClassName();
        baseBean.writeLog(callerName, mes);
    }

    /**
     * internal use
     */
    void writeLog(Class<?> caller, String mes) {
        String callerName = caller.getName();
        baseBean.writeLog(callerName, mes);
    }

    /**
     * internal use
     * 入参为0代表调用该方法的栈帧
     * 入参为-1代表当前这个栈帧
     */
    StackTraceElement getCallerClassName(int arrIdx) {
        arrIdx += 2;
        return Thread.currentThread().getStackTrace()[arrIdx];
    }

}
