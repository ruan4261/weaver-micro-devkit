package weaver.micro.devkit.task;

import weaver.interfaces.schedule.BaseCronJob;
import weaver.micro.devkit.dc.DynamicClass;

public class LoaderCacheRefreshJob extends BaseCronJob {

    public String targetClass;

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public void execute() {
        DynamicClass.remove(targetClass);
    }
}
