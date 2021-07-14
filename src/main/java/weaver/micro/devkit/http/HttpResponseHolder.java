package weaver.micro.devkit.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import weaver.micro.devkit.Assert;
import weaver.micro.devkit.print.MinimumType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ruan4261
 * @since 1.1.0
 */
public class HttpResponseHolder {

    /**
     * 判断{@code CloseableHTTPResponse}是否解析成功<hr>
     * 在构造过程(返回值解析)过程中引发异常会被catch, 保存到{@link #resolveException}中<br>
     * 如果当前变量值为{@code true}, 则{@link #resolveException}为空, 否则代表其是构造过程中的异常
     */
    private boolean resolveSuccess;

    /**
     * 解析{@code CloseableHttpResponse}返回值过程中的异常<br>
     * 不存在则为空
     */
    private Throwable resolveException;

    /**
     * Http状态码
     */
    private StatusLine httpStatusLine;

    /**
     * Http协议版本信息
     */
    @MinimumType
    private ProtocolVersion protocolVersion;

    /**
     * 返回报文头, 需自行处理value
     */
    private Map<String, String> headerMap;

    /**
     * 返回http报文实体部分
     */
    @MinimumType(
            serializationClass = Arrays.class,
            parametersList = {byte[].class},
            callIndex = 1
    )
    private byte[] responseEntity;

    /**
     * httpClient封装返回对象的引用, 返回值解析时不会关闭连接
     * 需要通过该引用手动关闭连接
     */
    @MinimumType
    private final CloseableHttpResponse closeableHttpResponse;

    public HttpResponseHolder(CloseableHttpResponse response) {
        this.closeableHttpResponse = response;
        HttpEntity entity = null;
        try {
            // response header
            this.httpStatusLine = Assert.notNull(response.getStatusLine());
            this.protocolVersion = Assert.notNull(response.getProtocolVersion());

            // header resolve
            Header[] allHeaders = Assert.notNull(response.getAllHeaders());
            this.headerMap = new HashMap<String, String>(allHeaders.length + (allHeaders.length >> 1));
            for (Header header : allHeaders) {
                String name = header.getName();
                String value = header.getValue();
                if (name != null && !name.equals(""))
                    this.headerMap.put(name, value);
            }

            // response body from input stream
            entity = Assert.notNull(response.getEntity());
            this.responseEntity = EntityUtils.toByteArray(entity);
            this.resolveSuccess = true;
        } catch (Throwable e) {
            this.resolveException = e;
            this.resolveSuccess = false;
            EntityUtils.consumeQuietly(entity);
        }
    }

    /**
     * @see #resolveSuccess
     */
    public boolean isResolveSuccess() {
        return this.resolveSuccess;
    }

    /**
     * @see #resolveException
     */
    public Throwable getResolveException() {
        return this.resolveException;
    }

    /**
     * Http状态码
     */
    public int getHttpStatusCode() {
        return this.httpStatusLine.getStatusCode();
    }

    /**
     * Http状态描述
     */
    public String getReasonPhrase() {
        return this.httpStatusLine.getReasonPhrase();
    }

    /**
     * Example:
     * <ul>
     *     <li>Http/1.1</li>
     *     <li>Http/2.0</li>
     * </ul>
     */
    public String getProtocolVersion() {
        return this.protocolVersion.toString();
    }

    /**
     * @see #headerMap
     */
    public Map<String, String> getHeaders() {
        return this.headerMap;
    }

    public byte[] getResponseEntity() {
        return this.responseEntity;
    }

    /**
     * 通过{@link CommonHttpAPI#threadEncoding}中设置的线程编码集解读数据
     */
    public String getResponseText() {
        return this.getResponseText(Charset.forName(CommonHttpAPI.getThreadEncoding()));
    }

    /**
     * 通过制定编码集解读数据
     *
     * @throws UnsupportedEncodingException 本地平台不支持该编码集
     */
    public String getResponseText(String charset) throws UnsupportedEncodingException {
        return new String(this.responseEntity, charset);
    }

    /**
     * 通过制定编码集解读数据
     */
    public String getResponseText(Charset charset) {
        return new String(this.responseEntity, charset);
    }

    /**
     * 关闭该连接, 请注意是否为连接池情况
     */
    public void closeConnection() {
        if (this.closeableHttpResponse != null) {
            try {
                this.closeableHttpResponse.close();
            } catch (IOException ignored) {
            }
        }
    }

}
