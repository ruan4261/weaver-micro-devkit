package weaver.micro.devkit.io;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.http.CommonHttpAPI;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * 仅允许http或https协议
 */
public class HttpAPI {

    public static void httpVerify(final String path) {
        Assert.judge(new Assert.Judgement() {
            @Override
            public boolean through() {
                return path.startsWith("https://") || path.startsWith("http://");
            }
        }, "Illegal protocol, only allow http or https.");
    }

    public static byte[] getRemoteBytes(String path) throws IOException {
        httpVerify(path);
        HttpURLConnection conn = null;

        try {
            URL url = new URL(path);
            // cast to HttpUrlConnection
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // settings
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(60000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");
            // open
            conn.connect();
            //通过conn取得输入流，并使用Reader读取
            if (200 == conn.getResponseCode()) {
                return IOAPI.getByteStreamData(conn.getInputStream());
            } else {
                throw new IOException(conn.getResponseCode() + " :: " + conn.getResponseMessage());
            }
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    public static String getRemoteChars(String path, String charset) throws IOException {
        httpVerify(path);
        CloseableHttpClient client = null;
        try {
            client = CommonHttpAPI.BUILD_DEFAULT_CLIENT();

            HttpResponse response = CommonHttpAPI.doGet(client, path, null, null);

            int status = CommonHttpAPI.getStatusCode(response);
            if (200 == status) {
                return CommonHttpAPI.getText(response, Charset.forName(charset));
            } else {
                String mes = CommonHttpAPI.getReasonPhrase(response);
                throw new IOException(status + " :: " + mes);
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        } finally {
            CommonHttpAPI.closeClient(client);
        }
    }
}
