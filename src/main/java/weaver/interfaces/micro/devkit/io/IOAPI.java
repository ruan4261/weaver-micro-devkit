package weaver.interfaces.micro.devkit.io;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * IO流无状态API，常用于向本地输出文件流.
 *
 * @author ruan4261
 */
public interface IOAPI {

    /**
     * 下载资源到本地，保存路径不存在可以自动创建。
     *
     * @param url      远程资源路径
     * @param savePath 本地保存路径
     * @param append   是否向已存在文件追加内容，为{@code false}时覆盖原文件
     * @throws IOException       IO流异常，可能原因（未知协议或流异常或...）
     * @throws SecurityException 被本地安全策略限制，无法保存文件
     */
    static void resourceDownload(String url, String savePath, boolean append) throws IOException, SecurityException {
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
     * @param stream   输入流，出栈时会被关闭！！！
     * @param append   是否向已存在文件追加内容，为{@code false}时覆盖原文件
     * @throws IOException IO流异常
     */
    static void inputStreamSaveLocally(String savePath, InputStream stream, boolean append) throws IOException {
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(savePath, append));
             InputStream input = stream) {
            int read;
            byte[] data = new byte[8192];
            while ((read = input.read(data)) != -1) {
                output.write(data, 0, read);
            }
            output.flush();
        }
    }

    /**
     * 从输入流中输出内容保存至本地。
     * 字符流
     *
     * @param savePath 本地保存路径
     * @param stream   输入流，出栈时会被关闭！！！
     * @param append   是否向已存在文件追加内容，为{@code false}时覆盖原文件
     * @throws IOException IO流异常
     */
    static void inputStreamSaveLocallyCharset(String savePath, InputStream stream, boolean append, String charset) throws IOException {
        try (OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(savePath, append), charset);
             InputStreamReader input = new InputStreamReader(stream)) {
            int read;
            char[] data = new char[8192];
            while ((read = input.read(data)) != -1) {
                output.write(data, 0, read);
            }
            output.flush();
        }
    }

    /**
     * 从输入流中读取二进制数据为字节数组
     *
     * @param stream 输入流，出栈时会被关闭！！！
     * @return 二进制数据
     * @throws IOException io流异常
     */
    static byte[] getInputStreamData(InputStream stream) throws IOException {
        try (InputStream input = stream;
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            int read;
            byte[] data = new byte[8192];
            while ((read = input.read(data)) != -1) {
                output.write(data, 0, read);
            }
            output.flush();
            return output.toByteArray();
        }
    }

}
