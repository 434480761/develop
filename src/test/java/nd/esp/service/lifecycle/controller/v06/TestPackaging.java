package nd.esp.service.lifecycle.controller.v06;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.app.LifeCircleWebConfig;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.apache.commons.io.Charsets;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;








import com.fasterxml.jackson.annotation.JsonProperty;
import com.nd.gaea.rest.o2o.JacksonCustomObjectMapper;
import com.nd.gaea.rest.testconfig.AbstractSpringJunit4Config;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
* 用于测试资源打包接口
* 
* @author qil
* @since
*/
@ContextConfiguration(classes = { LifeCircleWebConfig.class })
public class TestPackaging extends SimpleJunitTest4ResourceImpl {
   protected final Logger logger = Logger.getLogger(this.getClass().getName());
   private JacksonCustomObjectMapper objectMapper = new JacksonCustomObjectMapper();

   private final String VERSION = "v0.6";


   @Before
   public void myBefore() {

   }

   /**
    * 测试上传功能：none, existeduuid, renew
    * 
    * @since
    */
   @Test
   public void testPackaging() {
       String resourceType = "";

       // homeworks
       resourceType = "homeworks";
       logger.info(resourceType + "打包测试开始");
       
       String uuidInvalid = createInvalidResource(resourceType);
       String uuidValid = createValidResource(resourceType);
       String status = queryPackagingStatus(resourceType, uuidValid, false);
       Assert.assertEquals("测试资源打包状态不通过", "unpack", status);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           logger.error(e);
       }
       
       String reqState = requestPackaging(resourceType, uuidInvalid);
       Assert.assertEquals("测试请求资源打包不通过", null, reqState);
       reqState = requestPackaging(resourceType, uuidValid);
       Assert.assertEquals("测试请求资源打包不通过", "start", reqState);
       status = queryPackagingStatus(resourceType, uuidValid, false);
       Assert.assertEquals("测试资源打包状态不通过", "pending", status);
       try {
           Thread.sleep(15000);
       } catch (InterruptedException e) {
           logger.error(e);
       }
       
       status = queryPackagingStatus(resourceType, uuidValid, false);
//       Assert.assertEquals("测试资源打包状态不通过", "ready", status);
       
       testDelete(resourceType, uuidInvalid);
       testDelete(resourceType, uuidValid);
       
       logger.info(resourceType + "打包测试结束");

   }

   /**
    * 测试下载功能，存在两种情况：有无key,(access_url 若无， 则是href ,若有，则从store_info中取），暂不验证这个，key 不赋值
    */
   @Test
   public void testPackagingWebp() {
       String resourceType = "";

       // homeworks
       resourceType = "homeworks";
       logger.info(resourceType + "打包测试开始");
       
       String uuidInvalid = createInvalidResource(resourceType);
       String uuidValid = createValidResource(resourceType);
       String status = queryPackagingStatus(resourceType, uuidValid, false);
       Assert.assertEquals("测试资源打包状态不通过", "unpack", status);
       try {
           Thread.sleep(1000);
       } catch (InterruptedException e) {
           logger.error(e);
       }
       
       String reqState = requestPackagingWebp(resourceType, uuidInvalid);
       Assert.assertEquals("测试请求资源打包不通过", null, reqState);
       
       reqState = requestPackaging(resourceType, uuidValid);
       Assert.assertEquals("测试请求资源打包不通过", "start", reqState);
       
       status = queryPackagingStatus(resourceType, uuidValid, false);
       Assert.assertEquals("测试资源打包状态不通过", "pending", status);
       try {
           Thread.sleep(15000);
       } catch (InterruptedException e) {
           logger.error(e);
       }
       
       status = queryPackagingStatus(resourceType, uuidValid, false);
//       Assert.assertEquals("测试资源打包状态不通过", "ready", status);
       
       testDelete(resourceType, uuidInvalid);
       testDelete(resourceType, uuidValid);
       
       logger.info(resourceType + "打包测试结束");

   }

   @After
   public void myAfter() {

   }
   
   private String createInvalidResource(String resourceType) {
       String uuid = UUID.randomUUID().toString();
       String resStr = postCreate(resourceType,uuid,null);
       ResourceViewModel rtView = fromJson(resStr, ResourceViewModel.class);
       Assert.assertEquals("测试创建资源不通过", uuid, rtView.getIdentifier());
       return uuid;
   }
   
   private String createValidResource(String resourceType) {
       String uuid = UUID.randomUUID().toString();
       ResourceViewModel rvm = getDefaultResouceViewModel();
       rvm.setIdentifier(uuid);
       Map<String,ResTechInfoViewModel> techInfoMap = new HashMap<String, ResTechInfoViewModel>();
       techInfoMap.put("href", getValidHref(resourceType, uuid, "image/jpg"));
       rvm.setTechInfo(techInfoMap);
       String param = toJson(rvm);
       String resStr = postCreate(resourceType,uuid,param);
       ResourceViewModel rtView = fromJson(resStr, ResourceViewModel.class);
       Assert.assertEquals("测试创建资源不通过", uuid, rtView.getIdentifier());
       return uuid;
   }
   
   private String requestPackaging(String resType,String uuid){
       StringBuffer uri = new StringBuffer("/"+VERSION+"/"+resType);
       uri.append("/"+uuid).append("/archive");
       String resStr = null;
       try {
           resStr = MockUtil.mockPost(mockMvc, uri.toString(), "");
       } catch (Exception e) {
           logger.error("Request packaging error", e);
       }
       Map<String,String> rtMap = ObjectUtils.fromJson(resStr, Map.class);
       return rtMap.get("archive_state");
   }

   private String requestPackagingWebp(String resType,String uuid){
       StringBuffer uri = new StringBuffer("/"+VERSION+"/"+resType);
       uri.append("/"+uuid).append("/archive_webp");
       String resStr = null;
       try {
           resStr = MockUtil.mockPost(mockMvc, uri.toString(), "");
       } catch (Exception e) {
           logger.error("Request packaging error", e);
       }
       Map<String,String> rtMap = ObjectUtils.fromJson(resStr, Map.class);
       return rtMap.get("archive_state");
   }
   
   private String queryPackagingStatus(String resType, String uuid, boolean bWebp){
       StringBuffer uri = new StringBuffer("/"+VERSION+"/"+resType);
       uri.append("/"+uuid).append("/archiveinfo");
       if(bWebp) {
           uri.append("?webp=true");
       }
       String resStr = null;
       try {
           resStr = MockUtil.mockGet(mockMvc, uri.toString(), "");
       } catch (Exception e) {
           logger.error("Request packaging error", e);
       }
       Map<String,String> rtMap = ObjectUtils.fromJson(resStr, Map.class);
       return rtMap.get("archive_state");
   }

}

