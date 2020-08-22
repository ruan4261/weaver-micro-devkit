package weaver.micro.devkit.http;

import static weaver.micro.devkit.core.CacheBase.EMPTY;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 辅助Http接口
 *
 * @author ruan4261
 */
public interface BasicQuery {

    /**
     * 生成MethodGet的URI
     *
     * @param param 主体参数，可传空值
     * @param uri   可传空值
     * @return (? a = xxx & b = xxx) || (protocol://www.site.com/rest?a=xxx&b=xxx)
     */
    static String toQueryStringEncodeUTF8(Map<String, Object> param, String uri) {
        if (param == null) return uri;
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
        if (param == null) return EMPTY;
        return toQueryStringEncodeUTF8(param, null);
    }

    /**
     * 将参数Mapper转换为List<NameValuePair>格式
     * 可传空值，返回值为空的list实例
     */
    static List<NameValuePair> mapToNameValuePairList(Map<String, Object> param) {
        List<NameValuePair> result = new ArrayList<>();
        if (param == null) return result;
        param.forEach((k, v) -> {
            result.add(new BasicNameValuePair(k, v.toString()));
        });
        return result;
    }
}
