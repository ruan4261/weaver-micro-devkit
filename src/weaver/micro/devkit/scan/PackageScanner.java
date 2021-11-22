package weaver.micro.devkit.scan;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * https://www.cnblogs.com/youdiaodaxue16/p/9813087.html
 */
public abstract class PackageScanner {

    public PackageScanner() {
    }

    /**
     * 对扫描到的所有类进行自定义处理
     * 不限次数
     */
    public abstract void dealClass(Class<?> klass);

    /**
     * @param currentPackage 当前所在包
     * @param currentFile    当前所在物理位置
     */
    protected void scan0(String currentPackage, File currentFile) throws ClassNotFoundException {
        File[] files = currentFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathName) {
                if (pathName.isDirectory()) {
                    return true;
                }
                return pathName.getName().endsWith(".class");
            }
        });

        if (files != null)
            for (File file : files) {
                if (file.isDirectory()) {
                    scan0(currentPackage + "." + file.getName(), file);
                } else {
                    String fileName = file.getName().replace(".class", "");
                    String className = currentPackage + "." + fileName;

                    Class<?> klass = Class.forName(className);// define class
                    dealClass(klass);
                }
            }
    }

    /**
     * Scan Jar File.
     */
    protected void scanJar(URL url) throws IOException, ClassNotFoundException {
        JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
        JarFile jarfile = urlConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarfile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarName = jarEntry.getName();
            if (!jarName.endsWith(".class")) continue;

            String className = jarName.replace(".class", "").replaceAll("/", ".");

            Class<?> klass = Class.forName(className);
            dealClass(klass);
        }
    }

    /**
     * 扫描当前类所在包
     */
    public void packageScan(Class<?> klass) throws ClassNotFoundException, IOException, URISyntaxException {
        packageScan(klass.getPackage().getName());
    }

    /**
     * 使用包名扫描
     */
    public void packageScan(String packageName) throws IOException, URISyntaxException, ClassNotFoundException {
        String path = packageName.replace(".", "/");

        // get real path
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();

            if (url.getProtocol().equalsIgnoreCase("jar")) {
                scanJar(url);
            } else {
                File file = new File(url.toURI());
                if (!file.exists())
                    continue;

                scan0(packageName, file);
            }
        }
    }
}
