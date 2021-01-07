package weaver.micro.devkit.codec;

public class CodecUtils {

    /**
     * 二进制字节转十六进制字符串表示
     * 计算不了超过2G大小的数组
     */
    public static String binary2Hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length << 1);

        for (byte aByte : bytes) {
            if (aByte < 0) aByte += 256;

            if (aByte < 16) result.append("0");
            result.append(Integer.toHexString(aByte));
        }

        return result.toString();
    }

}
