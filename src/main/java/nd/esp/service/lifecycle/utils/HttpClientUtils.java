package nd.esp.service.lifecycle.utils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;

import nd.esp.service.lifecycle.utils.httpclient.HttpClientSupport;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http客户端的工具类
 *
 * @author bifeng.liu
 */
public class HttpClientUtils {
	private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtils.class);


    /**
     * 私有化构造函数，不允许实例化该类
     */
    private HttpClientUtils() {
    }


    /**
     * 通过Http协议及Get方式取得数据
     *
     * @param url URL
     * @return
     * @throws java.io.IOException
     */
    public static String httpGet(String url) throws IOException {
        return httpGet(url, null);
    }

    /**
     * 通过Http协议及Get方式取得数据
     *
     * @param url        URL
     * @param parameters 参数
     * @return
     * @throws java.io.IOException
     */
    public static String httpGet(String url, Map<String, String> parameters) throws IOException {
        return httpGet(url, parameters, null, null);
    }

    /**
     * 通过Http协议及Get方式取得数据
     * <p/>
     * Header参数只支持半角字符
     *
     * @param url        URL
     * @param parameters 参数
     * @param headers    Header参数
     * @param charset    字符集
     * @return
     * @throws java.io.IOException
     */
    public static String httpGet(String url, Map<String, String> parameters, Map<String, String> headers, Charset charset) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String body = null;
        LOG.debug("create http get:" + url);
        String destUrl = HttpClientSupport.applyHttpGetParameters(url, parameters, charset);
        HttpGet httpGet = new HttpGet(destUrl);
        HttpClientSupport.applyHeaderParameters(httpGet, headers);
        try {
            body = invoke(httpClient, httpGet, charset);
        } finally {
            httpClient.close();
        }
        return body;
    }

    /**
     * 通过Http协议及Post方式取得数据
     *
     * @param url URL
     * @return
     * @throws java.io.IOException
     */
    public static String httpPost(String url) throws IOException {
        return httpPost(url, null);
    }

    /**
     * 通过Http协议及Post方式取得数据
     * <p/>
     * 参数支持字符串数组<key,String[]{}>，当参数值为null时，则直接使用空字符串处理
     *
     * @param url        URL
     * @param parameters 参数
     * @return
     * @throws java.io.IOException
     */
    public static String httpPost(String url, Map<String, ? extends Object> parameters) throws IOException {
        return httpPost(url, parameters, null, null);
    }

    /**
     * 通过Http协议及Post方式取得数据
     *
     * @param url        URL
     * @param parameters 参数
     * @param headers    Header参数
     * @param charset    字符集
     * @return
     * @throws java.io.IOException
     */
    public static String httpPost(String url, Map<String, ? extends Object> parameters, Map<String, String> headers, Charset charset) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String body = null;
        LOG.debug("create http post:" + url);
        HttpPost httpPost = new HttpPost(url);
        HttpClientSupport.applyHttpPostParameters(httpPost, parameters, charset);
        HttpClientSupport.applyHeaderParameters(httpPost, headers);
        try {
            body = invoke(httpClient, httpPost, charset);
        } finally {
            httpClient.close();
        }
        return body;
    }

    /**
     * 通过Http协议及Post方式提交取得数据，提交的数据在Body中
     *
     * @param url      URL
     * @param body     内容
     * @param headers  Header参数
     * @param mimeType 内容类型
     * @param charset  字符集
     * @return
     * @throws java.io.IOException
     */
    public static String httpPost(String url, String body, Map<String, String> headers, String mimeType, Charset charset) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = null;
        LOG.debug("create http post:" + url);
        HttpPost httpPost = new HttpPost(url);
        ContentType contentType = StringUtils.isNotEmpty(mimeType) ? ContentType.create(mimeType, HttpClientSupport.getCharset(charset)) : ContentType.create("text/plain", HttpClientSupport.getCharset(charset));
        httpPost.setEntity(new StringEntity(body, contentType));
        HttpClientSupport.applyHeaderParameters(httpPost, headers);
        try {
            result = invoke(httpClient, httpPost, charset);
        } finally {
            httpClient.close();
        }
        return result;
    }

    /**
     * 通过Http协议及Put方式取得数据
     *
     * @param url URL
     * @return
     * @throws java.io.IOException
     */
    public static String httpPut(String url) throws IOException {
        return httpPut(url, null);
    }

    /**
     * 通过Http协议及Put方式取得数据
     * <p/>
     * 参数支持字符串数组<key,String[]{}>，当参数值为null时，则直接使用空字符串处理
     *
     * @param url        URL
     * @param parameters 参数
     * @return
     * @throws java.io.IOException
     */
    public static String httpPut(String url, Map<String, ? extends Object> parameters) throws IOException {
        return httpPut(url, parameters, null, null);
    }

    /**
     * 通过Http协议及Put方式取得数据
     *
     * @param url        URL
     * @param parameters 参数
     * @param headers    Header参数
     * @param charset    字符集
     * @return
     * @throws java.io.IOException
     */
    public static String httpPut(String url, Map<String, ? extends Object> parameters, Map<String, String> headers, Charset charset) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String body = null;
        LOG.debug("create http put:" + url);
        HttpPut httpPut = new HttpPut(url);
        HttpClientSupport.applyHttpPostParameters(httpPut, parameters, charset);
        HttpClientSupport.applyHeaderParameters(httpPut, headers);
        try {
            body = invoke(httpClient, httpPut, charset);
        } finally {
            httpClient.close();
        }
        return body;
    }

    /**
     * 通过Http协议及Put方式提交取得数据，提交的数据在Body中
     *
     * @param url      URL
     * @param body     内容
     * @param headers  Header参数
     * @param mimeType 内容类型
     * @param charset  字符集
     * @return
     * @throws java.io.IOException
     */
    public static String httpPut(String url, String body, Map<String, String> headers, String mimeType, Charset charset) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String result = null;
        LOG.debug("create http put:" + url);
        HttpPut httpPut = new HttpPut(url);
        ContentType contentType = StringUtils.isNotEmpty(mimeType) ? ContentType.create(mimeType, HttpClientSupport.getCharset(charset)) : ContentType.create("text/plain", HttpClientSupport.getCharset(charset));
        httpPut.setEntity(new StringEntity(body, contentType));
        HttpClientSupport.applyHeaderParameters(httpPut, headers);
        try {
            result = invoke(httpClient, httpPut, charset);
        } finally {
            httpClient.close();
        }
        return result;
    }

    /**
     * 执行发送请求
     *
     * @param httpClient
     * @param httpRequestBase 请求
     * @return
     * @throws java.io.IOException
     */
    private static String invoke(HttpClient httpClient, HttpRequestBase httpRequestBase, Charset charset) throws IOException {
        long begin = System.currentTimeMillis();
        HttpResponse response = sendRequest(httpClient, httpRequestBase);
        long end = System.currentTimeMillis();
        LOG.debug("get response from http server..");
        HttpEntity entity = response.getEntity();
        LOG.debug("response status: " + response.getStatusLine());
        int status = response.getStatusLine().getStatusCode();

        // 重定向
        if (isRedirect(status)) {
            return redirect(response, charset, httpClient, httpRequestBase);
        }

        // 502 进行重试
        if (isBadGateWay(status)) {
            LOG.error("Http response status 502, request lasts " + (end - begin) + " ms.");
            return waitAndRetry(charset, httpClient, httpRequestBase);
        }

        // 正常返回
        if (isNormal(status)) {
            return entity != null ? EntityUtils.toString(entity, HttpClientSupport.getCharset(charset)) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }

    /**
     * 是否正常返回
     *
     * @param status
     * @return
     */
    private static boolean isNormal(int status) {
        return status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES;
    }

    /**
     * 判断是否重定向
     *
     * @param status HTTP STATUS
     * @return
     */
    private static boolean isRedirect(int status) {
        return status == HttpStatus.SC_MOVED_TEMPORARILY
                || status == HttpStatus.SC_MOVED_PERMANENTLY;

    }

    /**
     * 判断是否网关错误 502
     *
     * @param status HTTP STATUS
     * @return
     */
    private static boolean isBadGateWay(int status) {
        return status == HttpStatus.SC_BAD_GATEWAY;
    }


    /**
     * 处理重定向
     *
     * @param response
     * @param charset
     * @param httpclient
     * @param httpRequestBase
     * @return
     * @throws java.io.IOException
     */
    private static String redirect(HttpResponse response, Charset charset, HttpClient httpclient,
                                   HttpRequestBase httpRequestBase) throws IOException {
        Header locationHeader = response.getFirstHeader("location");
        if (locationHeader != null) {
            String location = locationHeader.getValue();
            LOG.error("redirect to " + location);
            if (StringUtils.isEmpty(location)) {
                // 重定向地址为空就只能重试了
                LOG.error("redirect location is empty");
                return waitAndRetry(charset, httpclient, httpRequestBase);
            } else {
                httpRequestBase.setURI(URI.create(location));
                return retry(charset, httpclient, httpRequestBase);
            }
        } else {
            return waitAndRetry(charset, httpclient, httpRequestBase);
        }
    }

    /**
     * 10ms后重试
     *
     * @param charset
     * @param httpclient
     * @param httpRequestBase
     * @return
     */
    private static String waitAndRetry(Charset charset, HttpClient httpclient, HttpRequestBase httpRequestBase) throws IOException{
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        	LOG.error("等待重试请求",e);
        }
        return retry(charset, httpclient, httpRequestBase);
    }

    /**
     * 重试
     *
     * @param charset
     * @param httpclient
     * @param httpRequestBase
     * @return
     */
    private static String retry(Charset charset, HttpClient httpclient, HttpRequestBase httpRequestBase) throws IOException{
        HttpResponse response = sendRequest(httpclient, httpRequestBase);
        int status = response.getStatusLine().getStatusCode();

        // 正常返回
        if (isNormal(status)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, HttpClientSupport.getCharset(charset)) : null;
        } else {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }



    /**
     * 发送请求
     *
     * @param httpClient
     * @param httpRequestBase
     * @return
     * @throws java.io.IOException
     */
    private static HttpResponse sendRequest(HttpClient httpClient, HttpRequestBase httpRequestBase) throws IOException {
        LOG.debug("execute post...");
        // Create a custom response handler
        long begin = System.currentTimeMillis();
        try {
            return httpClient.execute(httpRequestBase);
        } catch (ConnectTimeoutException e) {
            long used = System.currentTimeMillis() - begin;
            LOG.error("ConnectTimeoutException, connection used " + used + " ms.");
            throw e;
        } catch (NoHttpResponseException e1) {
            long used = System.currentTimeMillis() - begin;
            LOG.error("NoHttpResponseException, connection used " + used + " ms.");
            throw e1;
        }
    }


}
