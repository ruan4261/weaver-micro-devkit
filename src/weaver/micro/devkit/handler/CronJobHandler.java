package weaver.micro.devkit.handler;

import weaver.interfaces.schedule.BaseCronJob;

/**
 * @since 1.1.7
 */
public abstract class CronJobHandler extends BaseCronJob implements Loggable {

    private final Loggable loggable = LogEventProcessor.getInstance(this.getClass());

    /**
     * 用于注入实例属性
     */
    protected void init() {
    }

    @Override
    public void execute() {
        log("Start >>");
        init();
        log("Initialization completed");
        try {
            handle();
        } catch (Throwable t) {
            log("CronJobHandler auto catch exception/error.", t);
            ifException(t);
        } finally {
            log("End >>");
        }
    }

    public abstract void handle() throws Throwable;

    /**
     * 在此处重写handle中抛出异常的处理逻辑
     * 默认抛出运行时异常
     */
    protected void ifException(Throwable t) throws RuntimeException {
        throw new RuntimeException(t);
    }

    @Override
    public void log(String mes) {
        this.loggable.log(mes);
    }

    @Override
    public void log(Throwable throwable) {
        this.loggable.log(throwable);
    }

    @Override
    public void log(String title, Throwable throwable) {
        this.loggable.log(title, throwable);
    }

    @Override
    public void log(Object o) {
        this.loggable.log(o);
    }

}
