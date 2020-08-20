package weaver.micro.devkit.http;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 辅助Http接口
 *
 * @author ruan4261
 */
public interface BasicQuery {

    /**
     * 生成MethodGet的URI
     *
     * @param param 主体参数，不可传null值，可传空内容映射
     * @param uri   可传空值
     * @return (? a = xxx & b = xxx) || (protocol://www.site.com/rest?a=xxx&b=xxx)
     */
    static String toQueryStringEncodeUTF8(Map<String, Object> param, String uri) {
        Objects.requireNonNull(param);
        StringBuilder builder;
        if (uri != null) {
            try {
                uri = URLEncoder.encode(uri, "UTF-8");
            } catch (UnsupportedEncodingException ignore) {
            }
            builder = new StringBuilder((param.size() << 4) + uri.length()).append(uri).append('?');
        } else
            builder = new StringBuilder((param.size() << 4)).append('?');
        param.forEach((k, v) -> {
            try {
                builder.append(URLEncoder.encode(k, "UTF-8")).append('=').append(URLEncoder.encode(v.toString(), "UTF-8")).append('&');
            } catch (UnsupportedEncodingException ignore) {
            }
        });
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * overload
     */
    static String toQueryStringEncodeUTF8(Map<String, Object> param) {
        Objects.requireNonNull(param);
        return toQueryStringEncodeUTF8(param, null);
    }

    /**
     * 将参数Mapper转换为List<NameValuePair>格式
     */
    static List<NameValuePair> mapToNameValuePairList(Map<String, Object> param) {
        Objects.requireNonNull(param);
        List<NameValuePair> result = new ArrayList<>();
        param.forEach((k, v) -> {
            result.add(new BasicNameValuePair(k, v.toString()));
        });
        return result;
    }
}
