package nd.esp.service.lifecycle.controller.v06;

import com.alibaba.fastjson.JSON;
import com.nd.gaea.rest.testconfig.MockUtil;
import nd.esp.service.lifecycle.daos.securitykey.v06.SecurityKeyDao;
import nd.esp.service.lifecycle.utils.common.CusBaseControllerConfig;
import nd.esp.service.lifecycle.utils.common.StringTestUtil;
import nd.esp.service.lifecycle.utils.common.TestException;
import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: TestSecurityKeyController </p>
 * <p>Description: TestSecurityKeyController </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/26 </p>
 *
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSecurityKeyController extends CusBaseControllerConfig {
	private static Logger logger = LoggerFactory.getLogger(TestSecurityKeyController.class);
	private static final String TEST_BASE_URL_SECURITY_KEY = "/v0.6/security";
    private static Long id;
	private static final String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2BvIiWx04MCaXZrfr0xnmG9/SiDAILYPgCjAg9XDlIXzXy/kgP8Ee87Mrit+cbJABcT3J0zAFTtphd1w8TblIVHuvP0KlTRX/YoeTLg6OJbK+5ACiktN+zcZZlF/2rwTtHec74cAHKICgf7666moXfjyoEgnlS5KKAZLbrlH02RgRHBInAdY+XEGHub5VSiezHr0oMj0rbp1WJKzcZg1p+l+d7YM3kgr9ty4QZI9e23zY1ji8mAnF0H+zyEVERW4ZRIAqhP1h62/8J2IC+McXn2INxc/igSWtTNcvsFIftTctuZY4Qr+iD92CsB660Lr/iqwrjR5BYsekqsR4VZlRQIDAQAB";


    @Autowired
	private SecurityKeyDao securityKeyDao;
	
	/**
	 * 用例：根据用户id和客户端的公钥获取服务端生成的密钥    [get] /v0.6/security
	 * @author gaoq
	 * @throws Exception
	 */
	@Test
	public void Test0001GetSecurityKey()  throws Exception{
		String url = TEST_BASE_URL_SECURITY_KEY ;
		logger.info("==================");
		logger.info("name:{}, url:{}", new Object[] {"根据用户id和客户端的公钥获取服务端生成的密钥", url});
		logger.info("=== start ===");
		

		//例1：user_id 为null 【期望结果：error】
		url = TEST_BASE_URL_SECURITY_KEY + "?user_id=&key=test";
		String result = MockUtil.mockGet(mockMvc, url, "{}");
		if(!"LC/CHECK_PARAM_VALID_FAIL".equals(JSON.parseObject(result).getString("code"))){
            throw new TestException();
        }
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		
		//例2：user_id 非user_id参数 【期望结果：error】
        url = TEST_BASE_URL_SECURITY_KEY + "?user_id=x&key=test";
        result = MockUtil.mockGet(mockMvc, url, "{}");
        if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
            throw new TestException();
        }
        logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		
        //例3：key 为null 【期望结果：error】
        
        url = TEST_BASE_URL_SECURITY_KEY + "?user_id="+this.userId+"&key=";
        result = MockUtil.mockGet(mockMvc, url, "{}");
        if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
            throw new TestException();
        }
        logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例4：key为无效参数，新增场景 【期望结果：error】
		url = TEST_BASE_URL_SECURITY_KEY + "?user_id="+this.userId+"&key="+StringTestUtil.randomNumbers(10);
		result = MockUtil.mockGet(mockMvc, url, "{}");
		String msg = JSON.parseObject(result).getString("msg");
		if (!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}" });
		
		//例5：所有参数正确【期望结果：pass】
       
		url = TEST_BASE_URL_SECURITY_KEY + "?user_id="+this.userId+"&key="+pubKey;
		result = MockUtil.mockGet(mockMvc, url, "{}");
		String msg5 = JSON.parseObject(result).getString("msg");
		if(StringUtils.isNoneBlank(msg5)) {
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
		} else {
			throw new TestException();
		}
	}

}
