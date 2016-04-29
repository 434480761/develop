package com.nd.esp.task.worker.buss.packaging.utils;

import java.io.IOException;

import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;

/**
 * @title 将Object进行mapper操作
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @see com.nd.gaea.rest.o2o.JacksonCustomObjectMapper
 * @create 2015年3月18日 下午6:15:06
 */
public class BeanMapperUtils {

   

	@SuppressWarnings("unchecked")
	public static <T> T mapper(Object origin, Class<T>t) throws IOException {
		
		JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
		T mapperT = (T) mapper.readValue(mapper.writeValueAsString(origin),
				t);
		
		return mapperT;
	}
	@SuppressWarnings("unchecked")
	public static <T> T mapperOnString(String origin, Class<T>t) throws IOException {
		
		JacksonCustomObjectMapper mapper = new JacksonCustomObjectMapper();
		T mapperT = (T) mapper.readValue(origin,
				t);
		
		return mapperT;
	}
	
	
	

}
