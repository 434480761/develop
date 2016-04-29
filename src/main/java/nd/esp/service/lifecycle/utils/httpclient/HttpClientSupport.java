package nd.esp.service.lifecycle.utils.httpclient;

import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HttpClient的支持类，包括异常处理
 *
 * @author bifeng.liu
 */
public class HttpClientSupport {
    /**
     * 默认的字符集名称
     */
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    /**
     * 默认的字符集
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName(DEFAULT_CHARSET_NAME);
    /**
     * 设置Get参数
     *
     * @param url
     * @param parameters
     */
    public static String applyHttpGetParameters(String url, Map<String, String> parameters, Charset charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (!CollectionUtils.isEmpty(parameters)) {
            for (Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> data = iterator.next();
                String key = data.getKey();
                String value = data.getValue();
                if (StringUtils.hasText(key)) {
                    sb.append("&");
                    sb.append(key);
                    sb.append("=");
                    sb.append(URLEncoder.encode(value, getCharset(charset).toString()));
                }
            }
            if (url.indexOf("?") == -1) {
                sb.replace(0, 1, "?");
            }
        }
        return url + sb.toString();
    }


    /**
     * 设置Post参数
     *
     * @param httpPost
     * @param parameters
     * @param charset
     */
    public static void applyHttpPostParameters(HttpEntityEnclosingRequestBase httpPost, Map<String, ? extends Object> parameters, Charset charset) throws UnsupportedEncodingException {
        if (!CollectionUtils.isEmpty(parameters)) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Map.Entry<String, ? extends Object> data : parameters.entrySet()) {
                String key = data.getKey();
                Object value = data.getValue();
                if (StringUtils.hasText(key)) {
                    if (value != null && value.getClass().equals(String[].class)) {
                        String[] values = (String[]) value;
                        for (int i = 0; i < values.length; i++) {
                            nvps.add(new BasicNameValuePair(key, values[i]));
                        }
                    } else {
                        if(value!=null){
                            nvps.add(new BasicNameValuePair(key,  value.toString()));
                        }
                    }
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, getCharset(charset)));
        }
    }

    /**
     * 设置Header参数
     *
     * @param httpRequestBase
     * @param headers
     */
    public static void applyHeaderParameters(HttpRequestBase httpRequestBase, Map<String, String> headers) {
        if (!CollectionUtils.isEmpty(headers)) {
            for (Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> data = iterator.next();
                String key = data.getKey();
                String value = data.getValue();
                if (StringUtils.hasText(key)) {
                    httpRequestBase.setHeader(key, value);
                }
            }
        }
    }

    /**
     * 取得字符集
     * <p/>
     * 如果为NULL时，则返回系统默认的字符集
     *
     * @param charset
     * @return
     */
    public static Charset getCharset(Charset charset) {
        return charset == null ? DEFAULT_CHARSET : charset;
    }
}
