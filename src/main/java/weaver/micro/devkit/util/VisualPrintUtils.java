package weaver.micro.devkit.util;

import weaver.micro.devkit.print.VisualPrintProcess;

import java.io.*;

/**
 * 打印的最原子级为primitive类型以及CharSequence类型
 * 使用格式为树状目录结构(形同于linux下的tree命令)
 */
public class VisualPrintUtils {

    public static void print(Object o) throws IllegalAccessException {
        try {
            print(o, System.out);
        } catch (IOException ignored) {
        }
    }

    public static void print(Object o, PrintStream out) throws IOException, IllegalAccessException {
        VisualPrintProcess process = new VisualPrintProcess(out);
        process.print(o);
    }

    public static void print(Object o, OutputStream out) throws IOException, IllegalAccessException {
        VisualPrintProcess process = new VisualPrintProcess(new PrintStream(out));
        process.print(o);
    }

    public static void print(Object o, OutputStream out, String encoding) throws IOException, IllegalAccessException {
        VisualPrintProcess process = new VisualPrintProcess(new PrintStream(out, false, encoding));
        process.print(o);
    }

    public static void print(Object o, Writer out) throws IOException, IllegalAccessException {
        VisualPrintProcess process = new VisualPrintProcess(out);
        process.print(o);
    }

    public static String getPrintInfo(Object o) throws IllegalAccessException {
        CharArrayWriter out = new CharArrayWriter();
        VisualPrintProcess process = new VisualPrintProcess(out);
        try {
            process.print(o);
            return out.toString();
        } catch (IOException ignored) {
        } finally {
            out.close();
        }
        throw new RuntimeException("I'm an impossible exception!");
    }

}
