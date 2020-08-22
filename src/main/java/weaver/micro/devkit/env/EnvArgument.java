package weaver.micro.devkit.env;

import weaver.micro.devkit.core.CacheBase;
import weaver.micro.devkit.util.Cast;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当前应用环境变量
 * 请注意控制值的类型
 * TODO 暂时无法确定持久性方案，此类已被搁置
 *
 * @author ruan4261
 */
public final class EnvArgument implements CacheBase {

    private static final Map<String, Object> env;

    static {
        env = new ConcurrentHashMap<>();
    }

    private EnvArgument() {
    }

    public static void put(String key, Object value) {
        env.put(key, value);
    }

    public static Object get(String key) {
        return env.get(key);
    }

    public static String getString(String key) {
        return Cast.toString(env.get(key));
    }

    public static void clear() {
        env.clear();
    }

    public static int size() {
        return env.size();
    }

}
