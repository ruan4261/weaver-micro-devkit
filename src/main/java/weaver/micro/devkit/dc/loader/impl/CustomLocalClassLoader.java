package weaver.micro.devkit.dc.loader.impl;

import weaver.micro.devkit.Assert;
import weaver.micro.devkit.ClassCannotResolvedException;
import weaver.micro.devkit.dc.loader.CustomClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 仅支持本地类文件加载
 * 输入流一次性读完，
 *
 * @author ruan4261
 */
public class CustomLocalClassLoader extends CustomClassLoader {

    private URLClassLoader loader;

    @Override
    public void loadResource(URL... url) throws IOException {
        Assert.notEmpty(url);
        this.loader = new URLClassLoader(url);
    }

    @Override
    public Class<?> define(String clazz) throws ClassCannotResolvedException {
        URL url = this.loader.getResource(clazz);
        try {
            InputStream input = null;
            try {
                input = url.openStream();
                byte[] data = new byte[input.available()];
                input.read(data);

                return super.invoke(loader, clazz, data, 0, data.length);
            } finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException ignore) {
                    }
            }
        } catch (Throwable e) {
            throw ClassCannotResolvedException.ThrowAutoFill(clazz, e);
        }
    }

}
