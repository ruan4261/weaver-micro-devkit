package weaver.micro.devkit.http;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

/**
 * 简单的无状态的HttpApi基于{@code org.apache.http}。
 *
 * @author ruan4261
 * @see org.apache.http
 */
public interface CommonHttpAPI {

    /**
     * 默认请求配置，暂时懒得提供修改
     */
    RequestConfig DEFAULT_CONFIG = RequestConfig.custom()
            // 等待连接池给出可用连接超时 ms
            .setConnectionRequestTimeout(5000)
            // 与目标服务器建立tcp连接超时 ms
            .setConnectTimeout(5000)
            // 服务器响应超时 ms
            .setSocketTimeout(30000)
            // 设置是否允许重定向(默认为true)
            .setRedirectsEnabled(true).build();

    Header[] DEFAULT_HEADERS = new Header[]{
            new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"),
            new BasicHeader("Content-Type", "application/x-www-form-urlencoded")
    };

    /**
     * 最普通的GET请求
     *
     * @param uri     资源定位
     * @param headers 自定义请求头
     * @param param   请求参数
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpResponse doGet(final String uri, Map<String, String> headers, Map<String, Object> param) throws IOException, URISyntaxException {
        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(DEFAULT_CONFIG).build();

        // body
        String query = BasicQuery.toQueryStringEncodeUTF8(param, uri);

        // method
        HttpGet method = new HttpGet();
        buildMethod(method, query, headers);

        return httpClient.execute(method);
    }

    static HttpResponse doPost(String uri, Map<String, String> header, Map<String, Object> param) throws IOException, URISyntaxException {
        HttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(DEFAULT_CONFIG).build();

        // method
        HttpPost method = new HttpPost(uri);
        buildMethod(method, uri, header);
        // todo 提供方法区分一下post的content-type
        /*
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setCharset(StandardCharsets.UTF_8);
        entityBuilder.seContentType(ContentType.APPLICATION_FORM_URLENCODED);*/
        HttpEntity entity = new UrlEncodedFormEntity(BasicQuery.mapToNameValuePairList(param));

        method.setEntity(entity);

        return httpClient.execute(method);
    }

    /**
     * 构建请求方法
     *
     * @param requestBase 构建对象
     * @param uri         请求地址
     * @param headers     自定义请求头
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static void buildMethod(HttpRequestBase requestBase, String uri, Map<String, String> headers) throws URISyntaxException {
        requestBase.setURI(new URI(uri));// url
        requestBase.setConfig(DEFAULT_CONFIG);// request config
        requestBase.setProtocolVersion(HttpVersion.HTTP_1_1);// http version
        requestBase.setHeaders(DEFAULT_HEADERS);// default headers
        headers.forEach(requestBase::setHeader);// custom headers
    }
}
