package weaver.micro.devkit.http;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    Header DEFAULT_USER_AGENT = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");

    /**
     * 基于默认请求配置构建一个新的客户端
     * 可转换为CloseableHttpClient
     */
    static HttpClient BUILD_DEFAULT_CLIENT() {
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
    static HttpResponse doGet(HttpClient client, final String uri, Map<String, String> headers, Map<String, Object> param) throws IOException, URISyntaxException {
        // entity
        String query = BasicQuery.toQueryStringEncodeUTF8(param, uri);

        // method
        HttpGet method = (HttpGet) buildMethod(new HttpGet(), query, headers);

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
     * @param param   请求参数
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    static HttpResponse doPostJson(HttpClient client, String uri, Map<String, String> headers, Map<String, Object> param) throws IOException, URISyntaxException {
        // method
        headers.put("Content-Type", "application/json");
        HttpPost method = (HttpPost) buildMethod(new HttpPost(), uri, headers);

        // body
        JSONObject json = new JSONObject();
        json.putAll(param);
        method.setEntity(new StringEntity(json.toJSONString(), StandardCharsets.UTF_8));

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
        headers.put("Content-Type", "multipart/form-data");
        HttpPost method = (HttpPost) buildMethod(new HttpPost(), uri, headers);

        // body
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        entityBuilder.seContentType(ContentType.MULTIPART_FORM_DATA);
        param.forEach((k, v) -> {
            if (v instanceof File) {
                entityBuilder.addBinaryBody(k, (File) v);
            } else if (v instanceof InputStream) {
                entityBuilder.addBinaryBody(k, (InputStream) v);
            } else {
                entityBuilder.addTextBody(k, v.toString());
            }
        });
        method.setEntity(entityBuilder.build());

        return client.execute(method);
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
        requestBase.setHeader(DEFAULT_USER_AGENT);// user agent
        if (headers != null)
            headers.forEach(requestBase::setHeader);// custom headers
        return requestBase;
    }
}
