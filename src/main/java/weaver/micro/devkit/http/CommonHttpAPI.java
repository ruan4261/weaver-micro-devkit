package weaver.micro.devkit.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的无状态的HttpApi基于{@link org.apache.http}。
 * 版本weaver.Ecology8lib
 *
 * @author ruan4261
 * @see org.apache.http
 */
public class CommonHttpAPI {

    /**
     * 默认请求配置，懒得提供修改，请自行构造
     */
    final static RequestConfig DEFAULT_CONFIG = RequestConfig.custom()
            // 等待连接池给出可用连接超时 ms
            .setConnectionRequestTimeout(5000)
            // 与目标服务器建立tcp连接超时 ms
            .setConnectTimeout(5000)
            // 服务器响应超时 ms
            .setSocketTimeout(30000)
            // 设置是否允许重定向(默认为true)
            .setRedirectsEnabled(true).build();

    final static Header[] DEFAULT_USER_AGENT = new Header[]{
            new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"),
            new BasicHeader("Accept-Encoding", "Identity")
    };

    /**
     * 基于默认请求配置构建一个新的客户端
     */
    static CloseableHttpClient BUILD_DEFAULT_CLIENT() {
        return HttpClientBuilder.create().setDefaultRequestConfig(DEFAULT_CONFIG).build();
    }

    /**
     * 最普通的GET请求
     *
     * @param client  发信端
     * @param uri     资源定位
     * @param headers 自定义请求头，允许为空
     * @param param   请求参数
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpResponse doGet(HttpClient client, final String uri, Map<String, String> headers, Map<String, String> param) throws IOException, URISyntaxException {
        // entity
        String url = BasicQuery.buildUrl(param, uri);

        // method
        HttpGet method = (HttpGet) buildMethod(new HttpGet(), url, headers);

        return client.execute(method);
    }

    /**
     * 使用application/x-www-form-urlencoded格式发送post请求
     *
     * @param client  发信端
     * @param uri     资源定位
     * @param headers 自定义请求头，允许为空
     * @param param   请求参数
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpResponse doPostURLEncode(HttpClient client, String uri, Map<String, String> headers, Map<String, Object> param) throws IOException, URISyntaxException {
        // method
        if (headers == null) headers = new HashMap<String, String>(1, 1f);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        HttpPost method = (HttpPost) buildMethod(new HttpPost(), uri, headers);

        // body
        HttpEntity entity = new UrlEncodedFormEntity(BasicQuery.mapToNameValuePairList(param));
        method.setEntity(entity);

        return client.execute(method);
    }

    /**
     * 使用application/json格式发送post请求
     *
     * @param client  发信端
     * @param uri     资源定位
     * @param headers 自定义请求头，允许为空
     * @param json    请求参数（json格式字符串）
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpResponse doPostJson(HttpClient client, String uri, Map<String, String> headers, String json) throws IOException, URISyntaxException {
        // method
        if (headers == null) headers = new HashMap<String, String>(1, 1f);
        headers.put("Content-Type", "application/json");
        HttpPost method = (HttpPost) buildMethod(new HttpPost(), uri, headers);

        // body
        method.setEntity(new StringEntity(json, Charset.forName("UTF-8")));

        return client.execute(method);
    }

    /**
     * 使用multipart/form-data格式发送post请求
     *
     * @param client  发信端
     * @param uri     资源定位
     * @param headers 自定义请求头，允许为空
     * @param param   请求参数
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpResponse doPostMultipart(HttpClient client, String uri, Map<String, String> headers, Map<String, Object> param) throws IOException, URISyntaxException {
        // method
        if (headers == null) headers = new HashMap<String, String>(1, 1f);
        headers.put("Content-Type", "multipart/form-data");
        HttpPost method = (HttpPost) buildMethod(new HttpPost(), uri, headers);

        // body
        final MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            if (v instanceof byte[]) {
                entity.addPart(k, new ByteArrayBody((byte[]) v, k));
            } else if (v instanceof File) {
                entity.addPart(k, new FileBody((File) v));
            } else if (v instanceof InputStream) {
                entity.addPart(k, new InputStreamBody((InputStream) v, k));
            } else {
                entity.addPart(k, new StringBody(v.toString()));
            }
        }

        method.setEntity(entity);
        return client.execute(method);
    }

    /**
     * http状态码
     */
    static int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * 它应该是对http状态的解释
     */
    static String getReasonPhrase(HttpResponse response) {
        return response.getStatusLine().getReasonPhrase();
    }

    /**
     * 返回的主题内容，以指定字符编码获取，不包括http头
     */
    static String getText(HttpResponse response, Charset charset) throws IOException {
        return EntityUtils.toString(response.getEntity(), charset);
    }

    /**
     * 返回http头中的Content-Length值
     * 如果返回头中没有该键，则此方法返回-1
     * 一般来说，如果请求对象是个页面，该大概率无此键值，如果请求对象为某文件，则大概率存在此键值
     * 通过这种方式获取请求体长度不一定正确，因为该信息存在http头中，可被服务端手动修改
     */
    static long getContentLength(HttpResponse response) {
        return response.getEntity().getContentLength();
    }

    /**
     * 获取全部返回头
     */
    static Header[] getAllHeaders(HttpResponse response) {
        return response.getAllHeaders();
    }

    /**
     * 构建请求方法
     *
     * @param requestBase 构建对象
     * @param uri         请求地址
     * @param headers     自定义请求头，允许为null
     * @return 可返回构建对象
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpRequestBase buildMethod(HttpRequestBase requestBase, String uri, Map<String, String> headers) throws URISyntaxException {
        requestBase.setURI(new URI(uri));// url
        requestBase.setConfig(DEFAULT_CONFIG);// request config
        requestBase.setProtocolVersion(HttpVersion.HTTP_1_1);// http version
        requestBase.setHeaders(DEFAULT_USER_AGENT);// user agent
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBase.setHeader(header.getKey(), header.getValue());
            }
        }
        return requestBase;
    }

    static void closeClient(CloseableHttpClient client) {
        if (client != null)
            try {
                client.close();
            } catch (IOException ignore) {
            }
    }
}
