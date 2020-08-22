package weaver.micro.devkit.test;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import weaver.micro.devkit.http.CommonHttpAPI;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpPostDemo {

    public static void main(String[] args) throws IOException, URISyntaxException {
        sendComment();
    }

    /**
     * post json
     */
    private static void sendComment() throws IOException, URISyntaxException {
        PrintStream out = System.out;
        String uri = "";
        Map<String, Object> params = new HashMap<>();
        params.put("param", "");
        // 使用try-with-resource节省代码调用，不使用这种方式则可以选择CommonHttpAPI.closeClient
        try (CloseableHttpClient client = CommonHttpAPI.BUILD_DEFAULT_CLIENT()) {
            HttpResponse response = CommonHttpAPI.doPostJson(client, uri, null, params);
            int code = CommonHttpAPI.getStatusCode(response);
            String mes = CommonHttpAPI.getReasonPhrase(response);
            long len = CommonHttpAPI.getContentLength(response);
            String html = CommonHttpAPI.getText(response, StandardCharsets.UTF_8);
            Header[] headers = CommonHttpAPI.getAllHeaders(response);

            out.println(code);
            out.println(mes);
            out.println(html);
            out.println(len);
            out.println(Arrays.toString(headers));
        }
    }

}
