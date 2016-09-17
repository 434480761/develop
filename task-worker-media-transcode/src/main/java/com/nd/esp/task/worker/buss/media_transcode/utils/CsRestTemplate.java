package com.nd.esp.task.worker.buss.media_transcode.utils;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @title 实现cs的patch请求
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月12日 下午2:09:30
 */
public class CsRestTemplate extends RestTemplate{
    
     public <T> T patchForObject(String url, Class<T> responseType, Object... urlVariables) throws RestClientException {
        RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
        HttpMessageConverterExtractor<T> responseExtractor =
                new HttpMessageConverterExtractor<T>(responseType, getMessageConverters());
        return execute(url, HttpMethod.PATCH, requestCallback, responseExtractor, urlVariables);
    }

}
