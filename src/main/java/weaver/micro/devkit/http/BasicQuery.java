package weaver.micro.devkit.http;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 辅助Http接口
 *
 * @author ruan4261
 */
public class BasicQuery {

    final static String EMPTY = "";

    /**
     * 生成MethodGet的URI
     *
     * @param param 主体参数，可传空值
     * @param uri   可传空值
     * @return (? a = xxx & b = xxx) || (protocol://www.site.com/rest?a=xxx&b=xxx)
     */
    public static String buildUrl(Map<String, String> param, String uri) {
        if (param == null) return uri == null ? EMPTY : uri;
        StringBuilder builder;
        if (uri != null) {
            builder = new StringBuilder((param.size() << 4) + uri.length()).append(uri).append('?');
        } else
            builder = new StringBuilder((param.size() << 4)).append('?');

        for (Map.Entry<String, String> entry : param.entrySet()) {
            builder.append(entry.getKey()).append('=').append((entry.getValue())).append('&');
        }

        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * overload
     */
    public static String buildQuery(Map<String, String> param) {
        if (param == null) return EMPTY;
        return buildUrl(param, null);
    }

    /**
     * 将参数Mapper转换为List<NameValuePair>格式
     * 可传空值，返回值为空的list实例
     */
    public static List<NameValuePair> mapToNameValuePairList(Map<String, Object> param) {
        List<NameValuePair> result = new ArrayList<NameValuePair>();
        if (param == null) return result;

        for (Map.Entry<String, Object> entry : param.entrySet()) {
            result.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }
        return result;
    }
}
