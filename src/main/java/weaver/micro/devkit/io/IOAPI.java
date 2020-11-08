package weaver.micro.devkit.io;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * IO流无状态API，常用于向本地输出文件流.
 *
 * @author ruan4261
 */
public final class IOAPI {

    /**
     * 下载资源到本地，保存路径不存在可以自动创建。
     *
     * @param url      远程资源路径
     * @param savePath 本地保存路径
     * @param append   是否向已存在文件追加内容，为{@code false}时覆盖原文件
     * @throws IOException       IO流异常，可能原因（未知协议或流异常或...）
     * @throws SecurityException 被本地安全策略限制，无法保存文件
     */
    public static void resourceDownload(String url, String savePath, boolean append) throws IOException, SecurityException {
        URL locator;
        URLConnection conn;
        File save;
        File parentFolder;
        BufferedInputStream in;
        {
            locator = new URL(url);
            conn = locator.openConnection();
            save = new File(savePath);
            parentFolder = save.getParentFile();
            if (!parentFolder.exists()) parentFolder.mkdirs();
            in = new BufferedInputStream(conn.getInputStream());
            inputStreamSaveLocally(savePath, in, append);
        }
    }

    /**
     * 从输入流中输出内容保存至本地。
     * 字节流
     *
     * @param savePath 本地保存路径
     * @param input    输入流
     * @param append   是否向已存在文件追加内容，为{@code false}时覆盖原文件
     * @throws IOException IO流异常
     */
    public static void inputStreamSaveLocally(String savePath, InputStream input, boolean append) throws IOException {
        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream(new FileOutputStream(savePath, append));
            int read;
            byte[] data = new byte[8192];
            while ((read = input.read(data)) != -1) {
                output.write(data, 0, read);
            }
            output.flush();
        } finally {
            if (output != null)
                output.close();
        }
    }

    /**
     * 从输入流中输出内容保存至本地。
     * 字符流
     *
     * @param savePath 本地保存路径
     * @param stream   输入流
     * @param append   是否向已存在文件追加内容，为{@code false}时覆盖原文件
     * @throws IOException IO流异常
     */
    public static void inputStreamSaveLocallyCharset(String savePath, InputStream stream, boolean append, String charset) throws IOException {
        OutputStreamWriter output = null;
        InputStreamReader input = new InputStreamReader(stream);
        try {
            output = new OutputStreamWriter(new FileOutputStream(savePath, append), charset);
            int read;
            char[] data = new char[8192];
            while ((read = input.read(data)) != -1) {
                output.write(data, 0, read);
            }
            output.flush();
        } finally {
            if (output != null)
                output.close();
        }
    }

    /**
     * 从输入流中读取二进制数据为字节数组
     *
     * @param input 输入流
     * @return 二进制数据
     * @throws IOException io流异常
     */
    public static byte[] getInputStreamData(InputStream input) throws IOException {
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            int read;
            byte[] data = new byte[8192];
            while ((read = input.read(data)) != -1) {
                output.write(data, 0, read);
            }
            output.flush();
            return output.toByteArray();
        } finally {
            if (output != null)
                output.close();
        }
    }

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
        Reader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(new FileInputStream(path), charset));
            StringBuilder builder = new StringBuilder(1024);
            int read;
            char[] data = new char[1024];
            while ((read = input.read(data)) != -1) {
                builder.append(data, 0, read);
            }
            return builder.toString();
        } finally {
            if (input != null)
                input.close();
        }
    }

    public static void writeLocalFileText(String path, String data) throws IOException {
        writeLocalFileText(path, data, null, false);
    }

    public static void writeLocalFileText(String path, String data, Charset charset) throws IOException {
        writeLocalFileText(path, data, charset, false);
    }

    public static void writeLocalFileText(String path, String data, boolean append) throws IOException {
        writeLocalFileText(path, data, null, append);
    }

    /**
     * 通过字符流将数据写入本地文本文件
     *
     * @param path    路径
     * @param data    写入内容
     * @param charset 字符集，可为null，默认为平台字符集
     * @param append  是否追加（是：追加；否：覆盖）
     */
    public static void writeLocalFileText(String path, String data, Charset charset, boolean append) throws IOException {
        if (charset == null) charset = Charset.defaultCharset();
        Writer output = null;
        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, append), charset));
            output.write(data);
            output.flush();
        } finally {
            if (output != null)
                output.close();
        }
    }
}
