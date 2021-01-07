package weaver.micro.devkit.codec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

    /**
     * 输出32位16进制MD5散列码
     */
    public static String hash(String mes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] binaryCode = md.digest(mes.getBytes());
            return CodecUtils.binary2Hex(binaryCode);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
