package weaver.micro.devkit.kvcs.loader;

import weaver.micro.devkit.io.IOAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 从classpath寻找字节码文件进行加载的加载器
 */
public class ResourceClassLoader extends BaseClassLoader {

    @Override
    public Class<?> findClass(String name) {
        // 将类全限定名转为资源形式
        name = name.replace(".", "/") + ".class";

        try {
            Enumeration<URL> enumeration = ClassLoader.getSystemResources(name);
            URL url = getOnlyURL(enumeration, name);
            File file = new File(url.toURI());

            if (file.exists() && file.isFile()) {
                InputStream input = null;
                try {
                    input = new FileInputStream(file);

                    // byte code
                    byte[] code = IOAPI.getByteStreamData(input);

                    // final definition
                    return defineClass(name, code, 0, code.length);
                } finally {
                    if (input != null)
                        try {
                            input.close();
                        } catch (IOException ignored) {
                        }
                }
            }
            throw new RuntimeException("Cannot resolve file: " + file.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 确保目标来源唯一
     */
    public URL getOnlyURL(Enumeration<URL> enumeration, String resourceName) {
        List<URL> url = new ArrayList<URL>(1);
        int num = 0;
        while (enumeration.hasMoreElements()) {
            url.add(enumeration.nextElement());
            num++;
        }

        if (num != 1)
            throw new RuntimeException(resourceName + " has " + num + " path! They are as follows: " + url.toString());
        return url.get(0);
    }
}
