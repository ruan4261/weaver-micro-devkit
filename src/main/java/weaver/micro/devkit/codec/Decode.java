package weaver.micro.devkit.codec;

import java.util.Base64;

/**
 * 字符串解码接口
 *
 * @author ruan4261
 */
public interface Decode {

    static byte[] base64ToByteArray(byte[] base64) {
        return Base64.getDecoder().decode(base64);
    }

    static String base64ToString(byte[] base64) {
        return new String(base64ToByteArray(base64));
    }

    static String base64ToString(String base64) {
        return base64ToString(base64.getBytes());
    }
}
