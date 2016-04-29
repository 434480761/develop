package nd.esp.service.lifecycle.repository.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * 项目名字:ddknow<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2014年11月6日<br>
 * 修改人:<br>
 * 修改时间:2014年11月6日<br>
 * 修改备注:<br>
 * 
 * @version 0.1<br>
 */
public class MyUtils {
	public static final String ENCODE = "utf-8";

	// http传输文件，指定文件类型
	private static Map<String, String> httpContentTypes = new HashMap<String, String>();

	static {
		httpContentTypes.put("bmp", "application/x-bmp");
		httpContentTypes.put("doc", "application/msword");
		httpContentTypes.put("ppt", "application/vnd.ms-powerpoint");
		httpContentTypes.put("pptx", "application/vnd.ms-powerpoint");
		httpContentTypes.put("gif", "image/gif");
		httpContentTypes.put("html", "text/html");
		httpContentTypes.put("dwg", "application/x-dwg");
		httpContentTypes.put("img", "application/x-img");
		httpContentTypes.put("isp", "application/x-internet-signup");
		httpContentTypes.put("jpeg", "image/jpeg");
		httpContentTypes.put("jpg", "image/jpeg");
		httpContentTypes.put("mid", "udio/mid");
		httpContentTypes.put("mp4", "video/mpeg4");
		httpContentTypes.put("mpeg", "video/mpg");
		httpContentTypes.put("wmv", "video/x-ms-wmv");
		httpContentTypes.put("tif", "image/tiff");
		httpContentTypes.put("ico", "image/x-iconf");
		httpContentTypes.put("text", "text/html");
		httpContentTypes.put("txt", "text/html");
		httpContentTypes.put("xml", "text/xml");
		httpContentTypes.put("xsd", "text/xml");
	}

	private static final Logger logger = LoggerFactory.getLogger(MyUtils.class);

	public static String getClassesPath() {
		return MyUtils.class.getResource("/").getPath();
	}

	public static String uuid() {
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		return uuid;
	}

	/**
	 * 获取http文件对应ContentType
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getContentType(String fileName) {
		if (StringUtils.isEmpty(fileName))
			return null;
		String extName = FilenameUtils.getExtension(fileName);
		if (StringUtils.isEmpty(extName))
			return null;
		return httpContentTypes.get(extName.toLowerCase());
	}

	/**
	 * http请求
	 * 
	 * @param url
	 *            请求目标地址
	 * @param mapHeader
	 *            头信息
	 * @param params
	 *            被传输参数
	 * @param retDataType
	 *            返回数据类型
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Object httpRequestPost(final String url,
			final Map<String, String> mapHeader,
			final List<HttpRequestParamBean> params) throws Exception {
		return httpRequestPost(url, mapHeader, params,
				HttpRequestParamBean.RetDataType.STR);
	}

	public static Object httpRequestPost(final String url,
			final Map<String, String> mapHeader,
			final List<HttpRequestParamBean> params,
			HttpRequestParamBean.RetDataType retDataType) throws Exception {
		Object rt = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = httpClientBuilder.build();
		try {
		    
		    if (logger.isDebugEnabled()) {
                
		        logger.debug("request url:{}", url);
		        
            }
			        
			HttpPost httpPost = new HttpPost(url);
			if (mapHeader != null) {
				// 设置头消息
				Set<String> headerKey = mapHeader.keySet();
				for (String k : headerKey) {
					httpPost.addHeader(k, mapHeader.get(k));
				}
			}
			MultipartEntityBuilder partBuilder = MultipartEntityBuilder
					.create();
			if (params != null) {
				for (HttpRequestParamBean param : params) {
					switch (param.getType()) {
					case STR:
					    
					    logger.trace(param.getName(), param.getData());
						        
						if (param.getData() != null) {
							String v = null;

							v = String.valueOf(param.getData());

							StringBody sbody = new StringBody(v,
									ContentType.create("text/plain",
											Constant.DEF_CHARTSET));
							partBuilder.addPart(param.getName(), sbody);
						}
						break;
					case STRBODY:

						break;
					case STREAM:
						InputStreamBody inBody = new InputStreamBody(
								(InputStream) param.getData(),
								param.getContentType(), param.getFileName());
						partBuilder.addPart(param.getName(), inBody);
						break;
					case FILE:

						break;
					case BYTES:
						ByteArrayBody bytesBody = new ByteArrayBody(
								(byte[]) param.getData(),
								ContentType.APPLICATION_OCTET_STREAM,
								param.getFileName());
						partBuilder.addPart(param.getName(), bytesBody);
						break;
					default:
						break;
					}
				}
			}

			HttpEntity reqEntity = partBuilder.build();

			httpPost.setEntity(reqEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
			    
			    if (logger.isDebugEnabled()) {
                    
			        logger.debug("status line:{}", response.getStatusLine());
			        
                }
				        
				// 获取返回数据
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					switch (retDataType) {
					case STR:
						InputStream in = resEntity.getContent();
						try {

							rt = IOUtils.toString(in, Constant.DEF_CHARTSET);
						} finally {
							if (in != null)
								in.close();
						}
						break;
					default:
						break;
					}
					
					if (logger.isDebugEnabled()) {
                        
					    logger.debug("Response content length:{}", resEntity.getContentLength());
					    
                    }
					        
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return rt;
	}

	public static Object httpRequestPut(final String url,
			final Map<String, String> mapHeader,
			final List<HttpRequestParamBean> params) throws Exception {
		return httpRequestPost(url, mapHeader, params,
				HttpRequestParamBean.RetDataType.STR);
	}

	public static Object httpRequestPut(final String url,
			final Map<String, String> mapHeader,
			final List<HttpRequestParamBean> params,
			HttpRequestParamBean.RetDataType retDataType) throws Exception {
		Object rt = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = httpClientBuilder.build();
		try {
		    
		    if (logger.isDebugEnabled()) {
                
		        logger.debug("request url:{}", url);
		        
            }
			        
			HttpPut httpPost = new HttpPut(url);
			if (mapHeader != null) {
				// 设置头消息
				Set<String> headerKey = mapHeader.keySet();
				for (String k : headerKey) {
					httpPost.addHeader(k, mapHeader.get(k));
				}
			}
			MultipartEntityBuilder partBuilder = MultipartEntityBuilder
					.create();
			if (params != null) {
				for (HttpRequestParamBean param : params) {
					switch (param.getType()) {
					case STR:
					    
					    logger.trace(param.getName(), param.getData());
					        
						if (param.getData() != null) {
							String v = null;

							v = String.valueOf(param.getData());

							StringBody sbody = new StringBody(v,
									ContentType.create("text/plain",
											Constant.DEF_CHARTSET));
							partBuilder.addPart(param.getName(), sbody);
						}
						break;
					case STRBODY:

						break;
					case STREAM:
						InputStreamBody inBody = new InputStreamBody(
								(InputStream) param.getData(),
								param.getContentType(), param.getFileName());
						partBuilder.addPart(param.getName(), inBody);
						break;
					case FILE:

						break;
					case BYTES:
						ByteArrayBody bytesBody = new ByteArrayBody(
								(byte[]) param.getData(),
								ContentType.APPLICATION_OCTET_STREAM,
								param.getFileName());
						partBuilder.addPart(param.getName(), bytesBody);
						break;
					default:
						break;
					}
				}
			}

			HttpEntity reqEntity = partBuilder.build();

			httpPost.setEntity(reqEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
			    
			    if (logger.isDebugEnabled()) {
                    
			        logger.debug("status line:{}", response.getStatusLine());
			        
                }
				
			    // 获取返回数据
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					switch (retDataType) {
					case STR:
						InputStream in = resEntity.getContent();
						try {
							rt = IOUtils.toString(in, Constant.DEF_CHARTSET);
						} finally {
							if (in != null)
								in.close();
						}
						break;
					default:
						break;
					}
					
					if (logger.isDebugEnabled()) {
                        
					    logger.debug("Response content length{}", resEntity.getContentLength());
					    
                    }
					        
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return rt;
	}

	/**
	 * 发送只有一个实体
	 * 
	 * @param url
	 * @param mapHeader
	 * @param body
	 * @param retDataType
	 * @return
	 * @throws Exception
	 */

	public static Object httpRequestPost(final String url,
			final Map<String, String> mapHeader, Object body) throws Exception {
		return httpRequestPost(url, mapHeader, body,
				HttpRequestParamBean.RetDataType.STR);
	}

	public static Object httpRequestPost(final String url,
			final Map<String, String> mapHeader, Object body,
			HttpRequestParamBean.RetDataType retDataType) throws Exception {
		Object rt = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = httpClientBuilder.build();
		try {
		    
		    if (logger.isDebugEnabled()) {
                
		        logger.debug("request url:{}", url);
		        
            }
			        
			HttpPost httpPost = new HttpPost(url);
			if (mapHeader != null) {
				// 设置头消息
				Set<String> headerKey = mapHeader.keySet();
				for (String k : headerKey) {
					httpPost.addHeader(k, mapHeader.get(k));
				}
			}

			HttpEntity reqEntity = null;

			if (body instanceof String)
				reqEntity = new StringEntity((String) body,
						Constant.DEF_CHARTSET);

			httpPost.setEntity(reqEntity);
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
			    
			    if (logger.isDebugEnabled()) {
                    
			        logger.debug("status line:{}", response.getStatusLine());
			        
                }
        			    
				// 获取返回数据
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					switch (retDataType) {
					case STR:
						InputStream in = resEntity.getContent();
						try {
							rt = IOUtils.toString(in, Constant.DEF_CHARTSET);
						} finally {
							if (in != null)
								in.close();
						}
						break;
					default:
						break;
					}
					
					if (logger.isDebugEnabled()) {
                        
					    logger.debug("Response content length:{}", resEntity.getContentLength());
					    
                    }
        					
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return rt;
	}

	/**
	 * 获取get请求
	 * 
	 * @param url
	 * @param mapHeader
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpRequestGet(final String url,
			final Map<String, String> mapHeader,
			final Map<String, String> params) throws Exception {
		List<HttpRequestParamBean> requestParamBeans = null;

		if (params != null && params.size() > 0) {
			requestParamBeans = new ArrayList<HttpRequestParamBean>();

			Set<String> keys = params.keySet();
			for (String key : keys) {
				String value = params.get(key);
				HttpRequestParamBean paramBean = new HttpRequestParamBean();
				paramBean.setName(key);
				paramBean.setData(value);
				requestParamBeans.add(paramBean);
			}
		}

		return (String) httpRequestGet(url, mapHeader, requestParamBeans,
				HttpRequestParamBean.RetDataType.STR, null);
	}

	/**
	 * get请求
	 * 
	 * @param url
	 *            请求地址
	 * @param mapHeader
	 *            请求头消息
	 * @param params
	 *            被
	 * @param retDataType
	 * @param outputStream
	 *            输出流
	 * @return
	 * @throws Exception
	 */

	public static Object httpRequestGet(final String url,
			final Map<String, String> mapHeader,
			final List<HttpRequestParamBean> params,
			HttpRequestParamBean.RetDataType retDataType,
			OutputStream outputStream) throws Exception {
		Object rt = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = httpClientBuilder.build();
		try {
		    
		    if (logger.isDebugEnabled()) {
                
		        logger.debug("request url:{}", url);
		        
            }
			        
			// 设置参数
			List<NameValuePair> paramsValue = new ArrayList<NameValuePair>();
			if (params != null)
				for (HttpRequestParamBean pbean : params) {
					if (pbean.getData() != null) {
						NameValuePair valuePair = new BasicNameValuePair(
								pbean.getName(),
								String.valueOf(pbean.getData()));
						paramsValue.add(valuePair);
					}
				}
			String urlReal = url;
			if (paramsValue.size() > 0) {
				String paramstr = EntityUtils
						.toString(new UrlEncodedFormEntity(paramsValue,
								Constant.DEF_CHARTSET));
				urlReal += "?" + paramstr;
			}
			HttpGet httpGet = new HttpGet(urlReal);
			if (mapHeader != null) {
				// 设置头消息
				Set<String> headerKey = mapHeader.keySet();
				for (String k : headerKey) {
					httpGet.addHeader(k, mapHeader.get(k));
				}
			}
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
			    
			    if (logger.isDebugEnabled()) {
                    
			        logger.debug("status line:{}", response.getStatusLine());
			        
                }
        			    
				// 获取返回数据
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					if (retDataType == null)
						retDataType = HttpRequestParamBean.RetDataType.OTHER;
					switch (retDataType) {
					case STR:
						InputStream in = resEntity.getContent();
						try {
							rt = IOUtils.toString(in, Constant.DEF_CHARTSET);
						} finally {
							if (in != null)
								in.close();
						}
						break;
					default:
						if (outputStream != null) {
							InputStream in1 = resEntity.getContent();
							try {
								byte[] buff = new byte[1024];
								int rdlen = -1;
								while (-1 != (rdlen = in1.read(buff, 0,
										buff.length))) {
									outputStream.write(buff, 0, rdlen);
									outputStream.flush();
								}
							} finally {
								if (in1 != null)
									in1.close();
							}
						}
						break;
					}
					
					if (logger.isDebugEnabled()) {
                        
					    logger.debug("Response content length:{}", resEntity.getContentLength());
					    
                    }
					        
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return rt;
	}

	public static Object httpRequestDel(final String url) throws Exception {
		return httpRequestDel(url, null, null,
				HttpRequestParamBean.RetDataType.STR, null);
	}

	public static Object httpRequestDel(final String url,
			final Map<String, String> mapHeader,
			final List<HttpRequestParamBean> params,
			HttpRequestParamBean.RetDataType retDataType,
			OutputStream outputStream) throws Exception {
		Object rt = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient httpclient = httpClientBuilder.build();
		try {
		   
		    if (logger.isDebugEnabled()) {
                
		        logger.debug("request url:{}", url);
		        
            }
			        
			// 设置参数
			List<NameValuePair> paramsValue = new ArrayList<NameValuePair>();
			if (params != null)
				for (HttpRequestParamBean pbean : params) {
					if (pbean.getData() != null) {
						NameValuePair valuePair = new BasicNameValuePair(
								pbean.getName(),
								String.valueOf(pbean.getData()));
						paramsValue.add(valuePair);
					}
				}
			String urlReal = url;
			if (paramsValue.size() > 0) {
				String paramstr = EntityUtils
						.toString(new UrlEncodedFormEntity(paramsValue,
								Constant.DEF_CHARTSET));
				urlReal += "?" + paramstr;
			}
			HttpDelete httpDelete = new HttpDelete(urlReal);
			// HttpGet httpGet = new HttpGet(urlReal);
			if (mapHeader != null) {
				// 设置头消息
				Set<String> headerKey = mapHeader.keySet();
				for (String k : headerKey) {
					httpDelete.addHeader(k, mapHeader.get(k));
				}
			}
			CloseableHttpResponse response = httpclient.execute(httpDelete);
			try {
			    
			    if (logger.isDebugEnabled()) {
                    
			        logger.debug("status line:{}", response.getStatusLine());
			        
                }
			            
				// 获取返回数据
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null) {
					if (retDataType == null)
						retDataType = HttpRequestParamBean.RetDataType.OTHER;
					switch (retDataType) {
					case STR:
						InputStream in = resEntity.getContent();
						try {
							rt = IOUtils.toString(in, Constant.DEF_CHARTSET);
						} finally {
							if (in != null)
								in.close();
						}
						break;
					default:
						if (outputStream != null) {
							InputStream in1 = resEntity.getContent();
							try {
								byte[] buff = new byte[1024];
								int rdlen = -1;
								while (-1 != (rdlen = in1.read(buff, 0,
										buff.length))) {
									outputStream.write(buff, 0, rdlen);
									outputStream.flush();
								}
							} finally {
								if (in1 != null)
									in1.close();
							}
						}
						break;
					}
					
					if (logger.isDebugEnabled()) {
                        
					    logger.debug("Response content length:{}", resEntity.getContentLength());
					    
                    }
					        
				}
				EntityUtils.consume(resEntity);
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return rt;
	}

	/**
	 * 获取返回字符串
	 * 
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public static String getHttpRequestResult(CloseableHttpResponse response)
			throws Exception {

	    if (logger.isDebugEnabled()) {
            
	        logger.debug("status line:{}", response.getStatusLine());
	        
        }
	            
		// 获取返回数据
		HttpEntity resEntity = response.getEntity();
		InputStream in = resEntity.getContent();
		try {
			return IOUtils.toString(in, Constant.DEF_CHARTSET);

		} finally {
			if (in != null)
				in.close();
		}

	}

	/**
	 * json 字符串转为bean对象
	 * 
	 * @param json
	 * @param bean
	 */
	public static <T> T jsonStr2Bean(String json, Class<T> valueType)
			throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);
		return (T) mapper.readValue(json, valueType);
	}

	/**
	 * json 转为 List<Test>
	 * 
	 * @param json
	 * @param collectionClass
	 * @param elementClasses
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws Exception
	 */
	public static <T> T jsonStr2Bean(String json, Class<T> collectionClass,
			Class<?>... elementClasses) throws JsonParseException,
			JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		JavaType javaType = mapper.getTypeFactory().constructParametricType(
				collectionClass, elementClasses);
		return mapper.readValue(json, javaType);
	}

	/**
	 * bean 转 json str
	 * 
	 * @param bean
	 * @return
	 * @throws JsonProcessingException
	 * @throws Exception
	 */
	public static String bean2JsonStr(Object bean)
			throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		// mapper.setSerializationInclusion(Inclusion.NON_NULL);
		return mapper.writeValueAsString(bean);
	}

	/**
	 * bean 转 json str 单引号
	 * 
	 * @param bean
	 * @return
	 * @throws Exception
	 */
	public static String bean2JsonStrSingleQuotes(Object bean) throws Exception {

		String jstr = JSON
				.toJSONString(bean, SerializerFeature.UseSingleQuotes);
		return jstr;
	}

	/**
	 * 
	 * 项目名字:nd edu<br>
	 * 类描述:http 请求参数bean br> 创建人:wengmd<br>
	 * 创建时间:2015年1月23日<br>
	 * 修改人:<br>
	 * 修改时间:2015年1月23日<br>
	 * 修改备注:<br>
	 * 
	 * @version 0.1<br>
	 */
	static public class HttpRequestParamBean implements Serializable {
		/**
		 * 参数名称
		 */
		private String name;
		/**
		 * 文件名称
		 */
		private String fileName;
		/**
		 * 数据值
		 */
		private Object data;
		/**
		 * 参数类型
		 */
		private ParamType type;

		private String contentType;

		/**
		 * 
		 */
		public HttpRequestParamBean() {
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

		public ParamType getType() {
			return type;
		}

		public void setType(ParamType type) {
			this.type = type;
		}

		public String getContentType() {
			return contentType;
		}

		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public enum ParamType {
			// str:字符串
			// strbody stringBody类型
			//
			STR, STRBODY, FILE, STREAM, BYTES;
		}

		public enum RetDataType {
			STR, JPG, TXT, GIF, MP4, AVI, OTHER
		}
	}

	public static List<String> json2List(String s) throws JsonParseException,
			JsonMappingException, IOException {
		List<String> rt = jsonStr2Bean(s, List.class, String.class);
		return rt;
	}

	public static String list2Json(List<String> ls)
			throws JsonProcessingException {
		return bean2JsonStr(ls);
	}

	public static String timestamp2String(Timestamp timestamp) {
		return null;
	}

	public static Map<String, String> json2Map(String s)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, String> rt = (Map<String, String>) jsonStr2Bean(s,
				Map.class, String.class, String.class);
		return rt;
	}

	public static String map2Json(Map<String, String> map)
			throws JsonProcessingException {
		return bean2JsonStr(map);
	}

	

	public static void main(String[] args) throws Exception {
		String[] ss = new String[2];
		ss[0] = "abc";

		ss[1] = "000";

		System.out.println(bean2JsonStrSingleQuotes(ss));

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("k1", "1");
		map.put("k2", "2");
		map.put("k3", 3);
		System.out.println(bean2JsonStrSingleQuotes(map));

		// NdResource resource = new NdResource();
		// System.out.println(bean2JsonStrSingleQuotes(resource));
	}
}
