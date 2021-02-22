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
        if (param == null || param.isEmpty())
            return uri == null ? EMPTY : uri;

        // build url
        StringBuilder builder;
        if (uri != null) {
            builder = new StringBuilder((param.size() << 4) + uri.length())
                    .append(uri);
        } else {
            builder = new StringBuilder((param.size() << 4));
        }
        builder.append('?');

        // build params
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            if (k == null || k.equals(""))
                continue;
            v = (v == null ? "null" : v);

            builder.append(k)
                    .append('=')
                    .append(v.toString())
                    .append('&');
        }

        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * overload
     * @return ?param1=xx&param2=xx
     */
    public static String buildQuery(Map<String, String> param) {
        if (param == null)
            return EMPTY;
        else
            return buildUrl(param, null);
    }

    /**
     * 将参数Mapper转换为List<NameValuePair>格式
     * 可传空值，返回值为空的list实例
     */
    public static List<NameValuePair> mapToNameValuePairList(Map<String, Object> param) {
        List<NameValuePair> result = new ArrayList<NameValuePair>();
        if (param == null)
            return result;

        for (Map.Entry<String, Object> entry : param.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();

            if (k == null || k.equals(""))
                continue;
            v = (v == null ? "null" : v);

            result.add(new BasicNameValuePair(k, v.toString()));
        }

        return result;
    }
}
