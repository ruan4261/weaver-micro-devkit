package weaver.micro.devkit.dc.loader;

import weaver.micro.devkit.ClassCannotResolvedException;

import java.io.IOException;
import java.net.URL;

public interface ClassDefiner {

    void loadResource(URL... url) throws IOException;

    Class<?> define(String clazz) throws ClassCannotResolvedException;

}
