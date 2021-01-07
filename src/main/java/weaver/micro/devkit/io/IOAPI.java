package weaver.micro.devkit.io;

import java.io.*;

/**
 * IO流无状态API，常用于向本地输出文件流.
 *
 * @author ruan4261
 */
public final class IOAPI {

    public static byte[] getByteStreamData(InputStream input) throws IOException {
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

    public static String getCharStreamData(InputStream input, String charset) throws IOException {
        InputStreamReader reader = null;
        StringBuilder result = new StringBuilder(8192);
        try {
            reader = new InputStreamReader(input, charset);

            int read;
            char[] data = new char[8192];
            while ((read = reader.read(data)) != -1) {
                result.append(data, 0, read);
            }

            return result.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

}
