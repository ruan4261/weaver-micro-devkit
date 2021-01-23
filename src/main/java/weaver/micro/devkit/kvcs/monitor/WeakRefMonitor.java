package weaver.micro.devkit.kvcs.monitor;

import weaver.micro.devkit.kvcs.loader.BaseClassLoader;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class WeakRefMonitor {

    WeakHashMap<BaseClassLoader, Void> tracker = new WeakHashMap<BaseClassLoader, Void>();

    public void joinTrack(BaseClassLoader loader){
        WeakReference<BaseClassLoader> reference = new WeakReference<BaseClassLoader>(loader);

    }


}
