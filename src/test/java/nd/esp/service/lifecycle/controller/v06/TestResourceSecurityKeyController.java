package nd.esp.service.lifecycle.controller.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;
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

import com.alibaba.fastjson.JSON;
import com.nd.gaea.rest.testconfig.MockUtil;

/**
 * <p>Title: TestUserRoleController </p>
 * <p>Description: TestUserRoleController </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/2 </p>
 *
 * @author lanyl
 */
/**
 * 
 * <p>Title: TestResourceSecurityKeyController  </p>
 * <p>Description: TestResourceSecurityKeyController </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestResourceSecurityKeyController extends CusBaseControllerConfig {

	private static Logger logger = LoggerFactory.getLogger(TestResourceSecurityKeyController.class);
	private static final String TEST_BASE_URL = "/v0.6/resseck/";
	
	private static final String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2BvIiWx04MCaXZrfr0xnmG9/SiDAILYPgCjAg9XDlIXzXy/kgP8Ee87Mrit+cbJABcT3J0zAFTtphd1w8TblIVHuvP0KlTRX/YoeTLg6OJbK+5ACiktN+zcZZlF/2rwTtHec74cAHKICgf7666moXfjyoEgnlS5KKAZLbrlH02RgRHBInAdY+XEGHub5VSiezHr0oMj0rbp1WJKzcZg1p+l+d7YM3kgr9ty4QZI9e23zY1ji8mAnF0H+zyEVERW4ZRIAqhP1h62/8J2IC+McXn2INxc/igSWtTNcvsFIftTctuZY4Qr+iD92CsB660Lr/iqwrjR5BYsekqsR4VZlRQIDAQAB";
      
	private String uuid = null;

	@Autowired
    private BaseDao<ResourceSecurityKeyModel> baseDao;
    private static String  TABLE_POSTFIX = "resource_security_key";
	
	/**
     * 删除资源文件密钥信息      
     * @param resourceSecurityKeyModel
     * @return
     * @author gaoq
     */
  private int delete(String uuid){
      return this.baseDao.delete(" and identifier = ?", new Object[] { uuid }, TABLE_POSTFIX);
  }
	
	/**
	 * 用例：根据资源UUID和客户端的公钥获取服务端生成的密钥    [get] /v0.6/resseck
	 * @author lianggz
	 * @throws Exception
	 */
	@Test
	public void Test0001GetResourceSecurityKey()  throws Exception{
		String url = TEST_BASE_URL ;
		logger.info("==================");
		logger.info("name:{}, url:{}", new Object[] {"根据资源UUID和客户端的公钥获取服务端生成的密钥", url});
		logger.info("=== start ===");

		//例1：uuid 为null 【期望结果：error】
		url = TEST_BASE_URL + "?uuid=&key=test";
		String result = MockUtil.mockGet(mockMvc, url, "{}");
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
            throw new TestException();
        }
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例2：uuid 非UUID参数 【期望结果：error】
        url = TEST_BASE_URL + "?uuid=x&key=test";
        result = MockUtil.mockGet(mockMvc, url, "{}");
        if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
            throw new TestException();
        }
        logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
        
        //例3：key 为null 【期望结果：error】
        url = TEST_BASE_URL + "?uuid=1402ca03-0035-4730-baba-1785dbc0e2b7&key=";
        result = MockUtil.mockGet(mockMvc, url, "{}");
        if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
            throw new TestException();
        }
        logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例4：不存在，新增场景 【期望结果：pass】
        uuid = UUID.randomUUID().toString();
		url = TEST_BASE_URL + "?uuid="+uuid+"&key="+pubKey;
		result = MockUtil.mockGet(mockMvc, url, "{}");
		String msg = JSON.parseObject(result).getString("msg");
		if(StringUtils.isNoneBlank(msg)) {
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
		} else {
			throw new TestException();
		}
		
		//例5：存在，查询场景 【期望结果：pass】
        /*url = TEST_BASE_URL + "?uuid="+uuid+"&key="+pubKey;
        result = MockUtil.mockGet(mockMvc, url, "{}");
        String msg2 = JSON.parseObject(result).getString("msg");
        if(msg.equals(msg2)) {
            logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
        } else {
            throw new TestException();
        }
        this.delete(uuid);
		logger.info("=== END ===");*/
	}
}