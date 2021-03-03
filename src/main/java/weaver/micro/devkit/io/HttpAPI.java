package weaver.micro.devkit.io;

import org.apache.http.impl.client.CloseableHttpClient;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.http.CommonHttpAPI;
import weaver.micro.devkit.http.HttpResponseHolder;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * 仅允许http或https协议
 */
public class HttpAPI {

    public static void httpVerify(final String path) {
        Assert.judge(new Assert.Judgement<RuntimeException>() {
            @Override
            public boolean through() {
                return path.startsWith("https://") || path.startsWith("http://");
            }
        }, "Illegal protocol, only allow http or https.");
    }

    public static byte[] getRemoteBytes(String path) throws IOException {
        httpVerify(path);
        CloseableHttpClient client = null;
        try {
            client = CommonHttpAPI.BUILD_DEFAULT_CLIENT();

            HttpResponseHolder response = CommonHttpAPI.doGetWithHolder(client, path, null, null);
            if (!response.isResolveSuccess())
                throw response.getResolveException();

            int status = response.getHttpStatusCode();
            if (200 == status) {
                return response.getResponseEntity();
            } else {
                String mes = response.getReasonPhrase();
                throw new IOException(status + " :: " + mes);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        } catch (Throwable throwable) {
            if (throwable instanceof IOException)
                throw (IOException) throwable;
            else
                throw new IOException(throwable);
        } finally {
            CommonHttpAPI.close(client);
        }
    }

    public static String getRemoteChars(String path, String charset) throws IOException {
        return new String(getRemoteBytes(path), charset);
    }

    public static String getRemoteChars(String path) throws IOException {
        return new String(getRemoteBytes(path), CommonHttpAPI.getThreadEncoding());
    }

}
