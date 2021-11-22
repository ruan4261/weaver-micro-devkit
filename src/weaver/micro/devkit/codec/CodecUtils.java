package weaver.micro.devkit.codec;

public class CodecUtils {

    /**
     * 二进制字节转十六进制字符串表示
     * 计算不了超过2G大小的数组
     */
    public static String binary2Hex(byte[] bytes) {
        StringBuilder result = new StringBuilder(bytes.length << 1);

        // 此处不能用byte类型接收, 除非是unsigned, 但显然不可能, 所以用short代替
        for (short aByte : bytes) {
            if (aByte < 0) aByte += 256;

            if (aByte < 16) result.append("0");
            result.append(Integer.toHexString(aByte));
        }

        return result.toString();
    }

}
