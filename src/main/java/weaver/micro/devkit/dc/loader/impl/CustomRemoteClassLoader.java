package weaver.micro.devkit.dc.loader.impl;

import weaver.micro.devkit.ClassCannotResolvedException;
import weaver.micro.devkit.dc.loader.CustomClassLoader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomRemoteClassLoader extends CustomClassLoader{

    private URLClassLoader classLoader;

    @Override
    public void loadResource(URL... urls) throws IOException {
        this.classLoader = new URLClassLoader(urls);
    }

    @Override
    public Class<?> define(String clazz) throws ClassCannotResolvedException {
        return null;// todo remote
    }
}
