package weaver.micro.devkit.util;

import weaver.micro.devkit.print.MinimumTypeModel;
import weaver.micro.devkit.print.VisualPrintProcess;
import weaver.micro.devkit.print.model.OfficialPresetMinimumTypeModel;

import java.io.*;

/**
 * 打印格式为树状目录结构(形同于linux下的tree命令)
 *
 * @see VisualPrintProcess
 */
public class VisualPrintUtils {

    private static final MinimumTypeModel defaultSetting
            = OfficialPresetMinimumTypeModel.immutable();

    public static void print(Object o) {
        try {
            print(o, System.out);
        } catch (IOException ignored) {
        }
    }

    public static void print(Object o, PrintStream out) throws IOException {
        VisualPrintProcess process = new VisualPrintProcess(out);
        useDefaultSetting(process);
        process.print(o);
    }

    public static void print(Object o, OutputStream out) throws IOException {
        VisualPrintProcess process = new VisualPrintProcess(new PrintStream(out));
        useDefaultSetting(process);
        process.print(o);
    }

    public static void print(Object o, OutputStream out, String encoding) throws IOException {
        VisualPrintProcess process = new VisualPrintProcess(
                new PrintStream(out, false, encoding)
        );
        useDefaultSetting(process);
        process.print(o);
    }

    public static void print(Object o, Writer out) throws IOException {
        VisualPrintProcess process = new VisualPrintProcess(out);
        useDefaultSetting(process);
        process.print(o);
    }

    public static String getPrintInfo(Object o) {
        CharArrayWriter out = new CharArrayWriter();
        VisualPrintProcess process = new VisualPrintProcess(out);
        useDefaultSetting(process);
        try {
            process.print(o);
            return out.toString();
        } catch (IOException ignored) {
        } finally {
            out.close();
        }
        throw new RuntimeException("I'm an impossible exception!");
    }

    public static void useDefaultSetting(VisualPrintProcess process) {
        process.setMinimumTypeModel(defaultSetting);
    }

}
