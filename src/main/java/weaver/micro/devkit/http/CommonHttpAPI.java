package weaver.micro.devkit.http;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import weaver.micro.devkit.Assert;

import javax.net.ssl.SSLContext;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * 简单的无状态的HttpApi基于{@link org.apache.http}。
 * 版本weaver.Ecology8lib
 *
 * @author ruan4261
 * @see org.apache.http
 */
public class CommonHttpAPI {

    static ThreadLocal<String> threadEncoding = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return "UTF-8";
        }
    };

    public static String getThreadEncoding() {
        return threadEncoding.get();
    }

    public static void setThreadEncoding(String encoding) {
        threadEncoding.set(encoding);
    }

    public static void clearThreadLocal() {
        threadEncoding.remove();
    }

    /**
     * 默认请求配置，懒得提供修改，请自行构造
     */
    public final static RequestConfig DEFAULT_CONFIG = RequestConfig.custom()
            // ms, 请求本地连接超时(请求未发出)
            .setConnectionRequestTimeout(5000)
            // ms, 请求目标服务器超时(未响应请求)
            .setConnectTimeout(15000)
            // ms, 与目标服务器连接超时(非正常结束)
            .setSocketTimeout(15000)
            .build();

    final static Header[] DEFAULT_USER_AGENT = new Header[]{
            new BasicHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" +
                            " (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36"),
            new BasicHeader("Accept-Encoding", "Identity")
    };

    public static LayeredConnectionSocketFactory PASS_SSL_SOCKET_FACTORY() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build();
            return new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpRoutePlanner PROXY_ROUTER(String protocol, String ip, int port) {
        return new DefaultProxyRoutePlanner(PROXY_HOST(protocol, ip, port));
    }

    public static HttpHost PROXY_HOST(String protocol, String ip, int port) {
        return new HttpHost(ip, port, protocol);
    }

    /**
     * 基于默认请求配置构建一个新的客户端
     */
    public static CloseableHttpClient BUILD_DEFAULT_CLIENT() {
        return buildClient(DEFAULT_CONFIG, null, null);
    }

    public static CloseableHttpClient buildClient(HttpRoutePlanner routePlanner, LayeredConnectionSocketFactory socketFactory, RequestConfig requestConfig) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (requestConfig != null)
            builder.setDefaultRequestConfig(requestConfig);
        if (socketFactory != null)
            builder.setSSLSocketFactory(socketFactory);
        if (routePlanner != null)
            builder.setRoutePlanner(routePlanner);

        return builder.build();
    }

    public static CloseableHttpClient buildClient(RequestConfig requestConfig, HttpHost proxy, LayeredConnectionSocketFactory socketFactory) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if (requestConfig != null)
            builder.setDefaultRequestConfig(requestConfig);
        if (socketFactory != null)
            builder.setSSLSocketFactory(socketFactory);
        if (proxy != null)
            builder.setProxy(proxy);

        return builder.build();
    }

    /**
     * 最普通的GET请求
     * param参数会拼接到url上
     * 一般不推荐设置请求头, 很多应用服务器不会读取其以及请求的实体部分
     *
     * @param client  发信端
     * @param uri     资源定位
     * @param headers 自定义请求头，允许为空
     * @param param   请求参数
     * @throws IOException        IO流异常，检查网络环境
     * @throws URISyntaxException 资源定位不符合RFC2396规范
     */
    public static CloseableHttpResponse doGet(CloseableHttpClient client,
                                              final String uri,
                                              Map<String, String> headers,
                                              Map<String, String> param)
            throws IOException, URISyntaxException {
        Assert.notNull(client);
        Assert.notNull(uri);

        String url = BasicQuery.buildUrl(param, uri);

        HttpGet get = buildMethodHeader(new HttpGet(), url, headers);

        return client.execute(get);
    }

    /**
     * 包装重载
     *
     * @see #doGet(CloseableHttpClient, String, Map, Map)
     * @since 1.1.0
     */
    public static HttpResponseHolder doGetWithHolder(CloseableHttpClient client,
                                                     final String uri,
                                                     Map<String, String> headers,
                                                     Map<String, String> param)
            throws IOException, URISyntaxException {
        return new HttpResponseHolder(doGet(client, uri, headers, param));
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
    public static CloseableHttpResponse doPostURLEncode(CloseableHttpClient client,
                                                        String uri,
                                                        Map<String, String> headers,
                                                        Map<String, Object> param)
            throws IOException, URISyntaxException {
        Assert.notNull(client);
        Assert.notNull(uri);

        HttpPost post = buildMethodHeader(new HttpPost(), uri, headers);
        // request body
        HttpEntity entity =
                new UrlEncodedFormEntity(BasicQuery.mapToNameValuePairList(param),
                        threadEncoding.get());
        post.setEntity(entity);

        return client.execute(post);
    }

    /**
     * 包装重载
     *
     * @see #doPostURLEncode(CloseableHttpClient, String, Map, Map)
     * @since 1.1.0
     */
    public static HttpResponseHolder doPostURLEncodeWithHolder(CloseableHttpClient client,
                                                               String uri,
                                                               Map<String, String> headers,
                                                               Map<String, Object> param)
            throws IOException, URISyntaxException {
        return new HttpResponseHolder(doPostURLEncode(client, uri, headers, param));
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
    public static CloseableHttpResponse doPostJson(CloseableHttpClient client,
                                                   String uri,
                                                   Map<String, String> headers,
                                                   String json)
            throws IOException, URISyntaxException {
        Assert.notNull(client);
        Assert.notNull(uri);

        HttpPost post = buildMethodHeader(new HttpPost(), uri, headers);

        // request body
        json = (json == null ? "{}" : json);
        post.setEntity(new StringEntity(json,
                ContentType.create("application/json", threadEncoding.get())));

        return client.execute(post);
    }

    /**
     * 包装重载
     *
     * @see #doPostJson(CloseableHttpClient, String, Map, String)
     * @since 1.1.0
     */
    public static HttpResponseHolder doPostJsonWithHolder(CloseableHttpClient client,
                                                          String uri,
                                                          Map<String, String> headers,
                                                          String json)
            throws IOException, URISyntaxException {
        return new HttpResponseHolder(doPostJson(client, uri, headers, json));
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
    public static CloseableHttpResponse doPostMultipart(CloseableHttpClient client,
                                                        String uri,
                                                        Map<String, String> headers,
                                                        Map<String, Object> param)
            throws IOException, URISyntaxException {
        Assert.notNull(client);
        Assert.notNull(uri);

        HttpPost post = buildMethodHeader(new HttpPost(), uri, headers);

        // request body
        MultipartEntity entity =
                new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,
                        null,
                        Charset.forName(threadEncoding.get()));

        if (param != null && !param.isEmpty())
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                String k = entry.getKey();
                Object v = entry.getValue();

                if (k == null || k.equals(""))
                    continue;
                v = (v == null ? "null" : v);

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
        post.setEntity(entity);

        return client.execute(post);
    }


    /**
     * 包装重载
     *
     * @see #doPostMultipart(CloseableHttpClient, String, Map, Map)
     * @since 1.1.0
     */
    public static HttpResponseHolder doPostMultipartWithHolder(CloseableHttpClient client,
                                                               String uri,
                                                               Map<String, String> headers,
                                                               Map<String, Object> param)
            throws IOException, URISyntaxException {
        return new HttpResponseHolder(doPostMultipart(client, uri, headers, param));
    }

    /**
     * http状态码
     */
    public static int getStatusCode(HttpResponse response) {
        Assert.notNull(response);
        return response.getStatusLine().getStatusCode();
    }

    /**
     * http状态解释
     */
    public static String getReasonPhrase(HttpResponse response) {
        Assert.notNull(response);
        return response.getStatusLine().getReasonPhrase();
    }

    /**
     * http报文主体内容, 通过线程本地设置的字符集读取, 默认utf8
     */
    public static String getText(HttpResponse response) throws IOException {
        Assert.notNull(response);
        return EntityUtils.toString(response.getEntity(), threadEncoding.get());
    }

    /**
     * http报文主体内容，以指定字符编码获取
     */
    public static String getText(HttpResponse response, Charset charset) throws IOException {
        Assert.notNull(response);
        return EntityUtils.toString(response.getEntity(), charset);
    }

    /**
     * http报文主体内容
     */
    public static byte[] getBytes(HttpResponse response) throws IOException {
        Assert.notNull(response);
        return EntityUtils.toByteArray(response.getEntity());
    }

    /**
     * 返回http头中的Content-Length值
     * 如果返回头中没有该键，则此方法返回-1
     * 一般来说，如果请求对象是个页面，该大概率无此键值，如果请求对象为某文件，则大概率存在此键值
     * 通过这种方式获取请求体长度不一定正确，因为该信息存在http头中，可被服务端手动修改
     */
    public static long getContentLength(HttpResponse response) {
        Assert.notNull(response);
        return response.getEntity().getContentLength();
    }

    /**
     * 获取全部返回头
     */
    public static Header[] getAllHeaders(HttpResponse response) {
        Assert.notNull(response);
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
    public static <T extends HttpRequestBase> T buildMethodHeader(T requestBase,
                                                                  String uri,
                                                                  Map<String, String> headers)
            throws URISyntaxException {
        Assert.notNull(requestBase);

        requestBase.setURI(new URI(uri));// url
        requestBase.setProtocolVersion(HttpVersion.HTTP_1_1);// http version
        requestBase.setHeaders(DEFAULT_USER_AGENT);// user agent
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String k = header.getKey();
                String v = header.getValue();

                if (k == null || k.equals(""))
                    continue;
                v = (v == null ? "null" : v);

                requestBase.setHeader(k, v);
            }
        }
        return requestBase;
    }

    /**
     * @deprecated please use {@link #buildMethodHeader(HttpRequestBase, String, Map)}
     */
    @Deprecated
    public static HttpRequestBase buildMethod(HttpRequestBase requestBase,
                                              String uri,
                                              Map<String, String> headers)
            throws URISyntaxException {
        Assert.notNull(requestBase);
        return buildMethodHeader(requestBase, uri, headers);
    }

    /**
     * @see #close(Closeable)
     */
    @Deprecated
    public static void closeClient(CloseableHttpClient client) {
        if (client != null)
            try {
                client.close();
            } catch (IOException ignored) {
            }
    }

    public static void close(Closeable closeable) {
        if (closeable != null)
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
    }

}
