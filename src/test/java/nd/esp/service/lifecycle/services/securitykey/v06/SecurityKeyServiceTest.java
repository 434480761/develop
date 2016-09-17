package nd.esp.service.lifecycle.services.securitykey.v06;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.services.resourcesecuritykey.v06.ResourceSecurityKeyServiceTest;
import nd.esp.service.lifecycle.utils.common.StringTestUtil;
import nd.esp.service.lifecycle.utils.common.TestException;

import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>Title: Service层SecurityKeyServiceTest测试  </p>
 * <p>Description: SecurityKeyServiceTest </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月27日           </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityKeyServiceTest extends BaseControllerConfig {
	
	 private final static Logger LOG = LoggerFactory.getLogger(ResourceSecurityKeyServiceTest.class);
	 private static final String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2BvIiWx04MCaXZrfr0xnmG9/SiDAILYPgCjAg9XDlIXzXy/kgP8Ee87Mrit+cbJABcT3J0zAFTtphd1w8TblIVHuvP0KlTRX/YoeTLg6OJbK+5ACiktN+zcZZlF/2rwTtHec74cAHKICgf7666moXfjyoEgnlS5KKAZLbrlH02RgRHBInAdY+XEGHub5VSiezHr0oMj0rbp1WJKzcZg1p+l+d7YM3kgr9ty4QZI9e23zY1ji8mAnF0H+zyEVERW4ZRIAqhP1h62/8J2IC+McXn2INxc/igSWtTNcvsFIftTctuZY4Qr+iD92CsB660Lr/iqwrjR5BYsekqsR4VZlRQIDAQAB";

	 @Autowired
	 @Qualifier("SecurityKeyServiceImpl")
	 private SecurityKeyService securityKeyService;
	 
	 
	 /**
	  * 用例： 查询或者新增密钥信息     
	  * @throws Exception
	  * @author gaoq
	  */
	 @Test
	  public void test001GetRsaEncryptDesKey() throws Exception {
		 //例1：在密钥信息 更新密钥 【期望结果：正确】
		 String desKey = "";
		 desKey = this.securityKeyService.getRsaEncryptDesKey(this.userId, pubKey);				
		 if(StringUtils.isNotBlank(desKey)){
			 LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyServiceTest", "test001GetRsaEncryptDesKey", 
	                    "pass", "test001GetRsaEncryptDesKey test pass"});	 
		 } else {
			 throw new TestException(); 
		 }
		 
		 //例2：在密钥信息不存在  新增密钥 【期望结果：正确】
		 String desKey2 = "";
		 desKey2 = this.securityKeyService.getRsaEncryptDesKey(StringTestUtil.randomNumbers(10), pubKey);				
		 if(StringUtils.isNotBlank(desKey2)){
			 LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyServiceTest2", "test001GetRsaEncryptDesKey", 
	                    "pass", "test001GetRsaEncryptDesKey2 test pass"});	 
		 } else {
			 throw new TestException(); 
		 }
	    	
	  }

	 /**
	  * 用例： 获取desKey
	  * @throws Exception
	  * @author gaoq
	  */
	 @Test
	  public void test002GetDesKey() throws Exception {
		 //例1：在密钥信息 更新密钥 【期望结果：正确】
		 String securityKey = "";
		 securityKey = this.securityKeyService.getDesKey(this.userId);				
		 if(StringUtils.isNotBlank(securityKey)){
			 LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyServiceTest", "test001GetDesKey", 
	                    "pass", "test001GetDesKey test pass"});	 
		 } else {
			 throw new TestException(); 
		 }
		 
		 //例2：在密钥信息不存在  新增密钥 【期望结果：正确】
		 String securityKey2 = "";
		 securityKey2 = this.securityKeyService.getDesKey(StringTestUtil.randomNumbers(15));				
		 if(StringUtils.isNotBlank(securityKey2)){
			 LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyServiceTest2", "test001GetDesKey2", 
	                    "pass", "test001GetDesKey2 test pass"});	 
		 } else {
			 throw new TestException(); 
		 }
	    	
	  }
}
