package weaver.micro.devkit.task;

import weaver.interfaces.schedule.BaseCronJob;
import weaver.micro.devkit.dc.DynamicClass;

public class LoaderCacheClearJob extends BaseCronJob {

    @Override
    public void execute() {
        DynamicClass.clear();
    }

}
