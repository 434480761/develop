package nd.esp.service.lifecycle.services.resourcesecuritykey.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;
import nd.esp.service.lifecycle.services.resourcesecuritykey.ResourceSecurityKeyService;
import nd.esp.service.lifecycle.utils.common.StringTestUtil;
import nd.esp.service.lifecycle.utils.common.TestException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * <p>Title: Service层ResourceSecurityKeyService测试  </p>
 * <p>Description: ResourceSecurityKeyServiceTest </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourceSecurityKeyServiceTest extends BaseControllerConfig {
    
    private final static Logger LOG = LoggerFactory.getLogger(ResourceSecurityKeyServiceTest.class);
    
    private static String uuid;
    private static String publicKey;
    
    @Autowired
    @Qualifier("ResourceSecurityKeyServiceImpl")
    private ResourceSecurityKeyService resourceSecurityKeyService;
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
     * 用例： 查询或者新增密钥信息     
     * @throws Exception
     * @author gaoq
     */
    @Test
    public void test001FindOrInsert() throws Exception {
    	//例1：新增密钥信息 ，其他参数正确 【期望结果：正确】
        uuid = UUID.randomUUID().toString();
        publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2BvIiWx04MCaXZrfr0xnmG9/SiDAILYPgCjAg9XDlIXzXy/kgP8Ee87Mrit+cbJABcT3J0zAFTtphd1w8TblIVHuvP0KlTRX/YoeTLg6OJbK+5ACiktN+zcZZlF/2rwTtHec74cAHKICgf7666moXfjyoEgnlS5KKAZLbrlH02RgRHBInAdY+XEGHub5VSiezHr0oMj0rbp1WJKzcZg1p+l+d7YM3kgr9ty4QZI9e23zY1ji8mAnF0H+zyEVERW4ZRIAqhP1h62/8J2IC+McXn2INxc/igSWtTNcvsFIftTctuZY4Qr+iD92CsB660Lr/iqwrjR5BYsekqsR4VZlRQIDAQAB"; 
        try {
			this.resourceSecurityKeyService.findOrInsert(uuid, publicKey);
			 LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyServiceTest", "test001FindOrInsert", 
	                    "pass", "test001FindOrInsert test pass"});
		} catch (Exception e) {
			 throw new TestException();
		}
        
        //例2：查询密钥信息 ，其他参数正确 【期望结果：正确】
        try {
			this.resourceSecurityKeyService.findOrInsert(uuid, publicKey);
			LOG.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyServiceTest", "test001FindOrInsert2", 
	                    "pass", "test001FindOrInsert2 test pass"});
		} catch (Exception e) {
			 throw new TestException();
		}
 
        this.delete(uuid);
    }
    
}
