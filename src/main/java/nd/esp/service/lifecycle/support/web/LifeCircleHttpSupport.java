package nd.esp.service.lifecycle.support.web;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.HttpClientUtils;
import nd.esp.service.lifecycle.utils.MapUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.utils.openApi.OpenApiProtocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;
/**
 * @title 资源生命周期Http请求辅助类
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年1月27日 上午10:39:10
 */
@Component
public class LifeCircleHttpSupport {

	private static final Logger LOG = LoggerFactory.getLogger(LifeCircleHttpSupport.class);


    @Autowired
	private Environment env;
    /**
     * 具体模块名称
     * */
    public String getModule(){
    	return null;
    };

    /**
     * 获取请求url
     *
     * @param url
     * @return
     */
    private String getRequestUrl(String url) {
        StringBuilder sb = new StringBuilder(Constant.SOURCE_DATA_API_URL);
        sb.append("/v0.1/").append(getModule()).append("/").append(url);
        return sb.toString();
    }



    public <T> OpenApiProtocol<T> httpPost(String url, Map<String, Object> params, TypeToken<OpenApiProtocol<T>> typeToken) throws IOException{

            return httpPost(url, params, null, typeToken);

    }
    public <T> OpenApiProtocol<T> httpPost(String url, Map<String, Object> params,java.util.Map<java.lang.String,java.lang.String> headers, TypeToken<OpenApiProtocol<T>> typeToken) throws IOException{
        Map<String, String> paramsNew = null;

            if (null == params) {
                params = new HashMap<String, Object>();
            }
            paramsNew = MapUtil.toStringMap(params);
            url=getRequestUrl(url);
            String json = HttpClientUtils.httpPost(url, paramsNew,headers,null);
            if (StringUtils.isEmpty(json)) {

                LOG.warn("请求地址{" + url + "},请求参数" + paramsNew.toString());
                
            }
            OpenApiProtocol<T> openApiProtocol = ObjectUtils.fromJson(json, typeToken);
            return openApiProtocol;

    }

    public <T> OpenApiProtocol<T> httpGet(String url, Map<String, Object> params, TypeToken<OpenApiProtocol<T>> typeToken)throws IOException {

            return  httpGet(url,params,null,typeToken);

    }
    public String httpGet(String url, Map<String, Object> params)throws IOException {

        Map<String, String> paramsNew = null;
        if (null == params) {
            params = new HashMap<String, Object>();
        }
        paramsNew = MapUtil.toStringMap(params);
        url=getRequestUrl(url);
        String result = HttpClientUtils.httpGet(url,paramsNew );
        if (StringUtils.isEmpty(result)) {
        	 LOG.warn("向资源数据库存储请求数据失败");
             LOG.warn("请求地址{" + url + "},请求参数" + paramsNew.toString());
             throw new LifeCircleException("", "向资源数据库存储请求数据失败");
        }
        return  result;

    }
    public String httpGet(String url)throws IOException {
            return  httpGet(url,null);

    }
    public <T> OpenApiProtocol<T> httpGet(String url, Map<String, Object> params,java.util.Map<java.lang.String,java.lang.String> headers, TypeToken<OpenApiProtocol<T>> typeToken)throws IOException {
        Map<String, String> paramsNew = null;
            if (null == params) {
                params = new HashMap<String, Object>();
            }
            url=getRequestUrl(url);
            paramsNew = MapUtil.toStringMap(params);
            String json = HttpClientUtils.httpGet(url, paramsNew,headers,null);
            if (StringUtils.isEmpty(json)) {
                LOG.warn("向资源数据库存储请求数据失败");

                 LOG.warn("请求地址{" + url + "},请求参数" + paramsNew.toString());

                 throw new LifeCircleException("", "向资源数据库存储请求数据失败");
            }
            return  ObjectUtils.fromJson(json, typeToken);

    }

    /**
     * 将参数统统转为Json格式
     *
     * @param paramMap 参数
     * @return
     */
    private Map<String, String> toStringMap(Map<String, Object> paramMap) {
        Map<String, String> params = new IdentityHashMap<String, String>(paramMap.size());
        Iterator<Map.Entry<String, Object>> iterator = paramMap.entrySet().iterator();
        Map.Entry<String, Object> entry;
        String key;
        Object value;
        while (iterator.hasNext()) {
            entry = iterator.next();
            key = entry.getKey();
            value = entry.getValue();
            // String 不转json，否则会加上双引号
            if (null == value) {
                params.put(key, null);
            } else if (value instanceof String) {
                params.put(key, value.toString());
            } else if (value instanceof Collection) {
                for (Object o : (Collection) value) {
                    if (o instanceof String) {
                        // String 不转json，否则会加上双引号
                        params.put(new String(key), o.toString());
                    } else {
                        params.put(new String(key), ObjectUtils.toJson(o));
                    }
                }
            } else if (value.getClass().isArray()) {
                Object o;
                for (int i = 0; i < Array.getLength(value); i++) {
                    o = Array.get(value, i);
                    if (o instanceof String) {
                        // String 不转json，否则会加上双引号
                        params.put(new String(entry.getKey()), o.toString());
                    } else {
                        params.put(new String(entry.getKey()), ObjectUtils.toJson(o));
                    }
                }
            } else {
                params.put(entry.getKey(), ObjectUtils.toJson(entry.getValue()));
            }
        }
        return params;
    }

    public String httpGet4Resource(String url, Map<String, Object> params)throws IOException {

        Map<String, String> paramsNew = null;
        if (null == params) {
            params = new HashMap<String, Object>();
        }
        paramsNew = MapUtil.toStringMap(params);
        
        String result = HttpClientUtils.httpGet(Constant.SOURCE_DATA_API_URL+url,paramsNew );
        if (StringUtils.isEmpty(result)) {

            LOG.warn("向资源数据库存储请求数据失败");

             LOG.warn("请求地址{"+url+"},请求参数"+paramsNew.toString());

             throw new LifeCircleException("", "向资源数据库存储请求数据失败");
        }
        return  result;

    }

}
