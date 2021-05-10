package weaver.micro.devkit.handler;

import weaver.interfaces.schedule.BaseCronJob;

/**
 * @since 1.1.7
 */
public abstract class CronJobHandler extends BaseCronJob implements Loggable {

    private final Loggable loggable = LogEventProcessor.getInstance();

    @Override
    public void execute() {
        log("Start");
        try {
            handle();
        } catch (Throwable t) {
            log("End: Failed", t);
            throw new RuntimeException(t);
        }
        log("End: Success");
    }

    public abstract void handle() throws Throwable;

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
