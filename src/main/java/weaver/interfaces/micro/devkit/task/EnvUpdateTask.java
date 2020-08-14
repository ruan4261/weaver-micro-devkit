package weaver.interfaces.micro.devkit.task;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import weaver.general.BaseBean;
import weaver.interfaces.micro.devkit.env.EnvArgument;
import weaver.interfaces.schedule.BaseCronJob;

import java.util.Map;

/**
 * 环境更新任务
 * 适用于weaverOA
 * 配置本任务时，请设置param参数，参数的值为Json对象，Json对象将被更新到应用环境中。
 *
 * @author ruan4261
 */
public class EnvUpdateTask extends BaseCronJob {

    public String param;

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    private BaseBean baseBean;

    public EnvUpdateTask() {
        this.baseBean = new BaseBean();
    }

    @Override
    public void execute() {
        log("Start");
        if (param == null) {
            log("Task was stopped, cause the param must be a json object, but it is null.");
        } else {
            try {
                Map<String, Object> mapper = JSONObject.parseObject(param).getInnerMap();
                log("Mapper Data -> " + mapper.toString());
                mapper.forEach(EnvArgument::put);
            } catch (JSONException e) {
                log("Param make exception, it is '" + param + "'");
            }
        }
        log("End");
    }

    private void log(String log) {
        baseBean.writeLog("EnvUpdateTask::" + log);
    }
}
