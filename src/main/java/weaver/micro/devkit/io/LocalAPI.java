package weaver.micro.devkit.io;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @deprecated 不完善的工具, 毫无意义
 */
@Deprecated
public class LocalAPI {

    /**
     * 从输入流中输出内容保存至本地。
     * 字节流
     *
     * @param savePath 本地保存路径
     * @param input    输入流
     * @throws IOException IO流异常
     */
    public static void saveByteStream(InputStream input, String savePath) throws IOException {
        PrintStream out = null;
        try {
            out = new PrintStream(savePath);

            int read;
            byte[] data = new byte[8192];
            while ((read = input.read(data)) != -1) {
                out.write(data, 0, read);
            }
            out.flush();
        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * 从输入流中输出内容保存至本地。
     * 字符流
     *
     * @param savePath 本地保存路径
     * @param stream   输入流
     * @throws IOException IO流异常
     */
    public static void saveCharStream(InputStream stream, String savePath, String charset) throws IOException {
        PrintWriter out = null;
        InputStreamReader input = new InputStreamReader(stream);
        try {
            out = new PrintWriter(savePath, charset);

            int read;
            char[] data = new char[8192];
            while ((read = input.read(data)) != -1) {
                out.write(data, 0, read);
            }
            out.flush();
        } finally {
            if (out != null)
                out.close();
        }
    }

    /**
     * 通过字符流读取本地文件, 平台默认字符集
     *
     * @param path 路径
     */
    public static String readLocalFileText(String path) throws IOException {
        return readLocalFileText(path, null);
    }

    /**
     * 通过字符流读取本地文件
     *
     * @param path    路径
     * @param charset 字符集，可为null，默认为平台字符集
     */
    public static String readLocalFileText(String path, Charset charset) throws IOException {
        if (charset == null) charset = Charset.defaultCharset();

        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(path), charset));

            StringBuilder result = new StringBuilder(8192);
            int read;
            char[] data = new char[8192];
            while ((read = input.read(data)) != -1) {
                result.append(data, 0, read);
            }

            return result.toString();
        } finally {
            if (input != null)
                input.close();
        }
    }

    /**
     * 通过字符流将数据写入本地文本文件, 平台默认字符集
     *
     * @param path 路径
     * @param data 写入内容
     */
    public static void writeLocalFileText(String path, String data) throws IOException {
        writeLocalFileText(path, data, Charset.defaultCharset().name());
    }

    /**
     * 通过字符流将数据写入本地文本文件
     *
     * @param path    路径
     * @param data    写入内容
     * @param charset 字符集，可为null，默认为平台字符集
     */
    public static void writeLocalFileText(String path, String data, String charset) throws IOException {
        Writer output = null;
        try {
            output = new PrintWriter(path, charset);
            output.write(data);
            output.flush();
        } finally {
            if (output != null)
                output.close();
        }
    }
}
