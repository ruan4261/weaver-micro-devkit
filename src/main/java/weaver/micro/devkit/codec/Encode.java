package weaver.micro.devkit.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * 字符串编码接口
 *
 * @author ruan4261
 */
public interface Encode {

    /**
     * 输入流转base64
     *
     * @param inputStream 输入流
     * @return Base64编码内容，以平台默认编码集输出string内容
     * @throws IOException 异常必定从输入流抛出，本方法抛出异常时输入流必定被关闭
     */
    static String toBase64String(InputStream inputStream) throws IOException {
        try (InputStream in = inputStream;
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buff = new byte[1024];
            int read;
            while ((read = in.read(buff)) > 0) out.write(buff, 0, read);
            return toBase64String(out.toByteArray());
        }
    }

    static byte[] toBase64(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    static String toBase64String(byte[] bytes) {
        return new String(toBase64(bytes));
    }

    static String toBase64String(String origin) {
        return toBase64String(origin.getBytes());
    }

}
