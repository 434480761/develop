package nd.esp.service.lifecycle.daos.resourcesecuritykey.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.resourcesecuritykey.ResourceSecurityKeyDao;
import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;
import nd.esp.service.lifecycle.utils.common.StringTestUtil;
import nd.esp.service.lifecycle.utils.common.TestException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: Service层ResourceSecurityKeyDao测试  </p>
 * <p>Description: ResourceSecurityKeyDaoTest </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourceSecurityKeyDaoTest extends BaseControllerConfig {
    
    private final static Logger logger = LoggerFactory.getLogger(ResourceSecurityKeyDaoTest.class);
    private static String uuid;
    
    @Autowired
    private ResourceSecurityKeyDao resourceSecurityKeyDao;
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
    public void test001Insert() throws Exception {
    	//例1：uuid为空，其他参数正确 【期望结果：错误】
        uuid = UUID.randomUUID().toString();
        String securityKey = StringTestUtil.randomNumbers(60); 
        ResourceSecurityKeyModel resourceSecurityKeyModel = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel.setIdentifier(null);
        resourceSecurityKeyModel.setSecurityKey(securityKey);
        int id = 0;
        try {
        	id = this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test001Insert", "error", e});
		}
        if( id > 0){
			 throw new TestException();
        }
        
        //例2：uuid 不为空 长度超过36，其他参数正确 【期望结果：错误】
        uuid = StringTestUtil.randomNumbers(37);
        ResourceSecurityKeyModel resourceSecurityKeyModel2 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel2 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel2.setIdentifier(uuid);
        resourceSecurityKeyModel2.setSecurityKey(securityKey);
        int id2 = 0;
        try {
        	id2 = this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel2);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test001Insert2", "error", e});
		}
        if( id2 > 0){
			 throw new TestException();
        }
        uuid = UUID.randomUUID().toString();
        
        //例3：securityKey 为空 ，其他参数正确 【期望结果：错误】
        ResourceSecurityKeyModel resourceSecurityKeyModel5 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel5 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel5.setIdentifier(uuid);
        resourceSecurityKeyModel5.setSecurityKey(null);
        int id5 = 0;
        try {
        	id5 = this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel5);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test001Insert3", "error", e});
		}
        if( id5 > 0){
			 throw new TestException();
        }
        
        
        //例4：securityKey 不为空 长度超过64，其他参数正确 【期望结果：错误】
        securityKey = StringTestUtil.randomNumbers(65);
        ResourceSecurityKeyModel resourceSecurityKeyModel6 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel6 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel6.setIdentifier(uuid);
        resourceSecurityKeyModel6.setSecurityKey(securityKey);
        int id6 = 0;
        try {
        	id6 = this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel6);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test001Insert4", "error", e});
		}
        if( id6 > 0){
			 throw new TestException();
        }
        securityKey = StringTestUtil.randomNumbers(60);
        
        //例5：所有参数正确 【期望结果：通过】
        ResourceSecurityKeyModel resourceSecurityKeyModel7 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel7 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel7.setIdentifier(uuid);
        resourceSecurityKeyModel7.setSecurityKey(securityKey);
        this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel7);
        ResourceSecurityKeyModel model = this.resourceSecurityKeyDao.findSecurityKeyInfo(uuid);
        if(model != null ){
        	logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test001Insert5",  "pass", "test001Insert7 test pass"});
        } else {
			 throw new TestException();
        }
       
        //例8：唯一性验证 其他参数正确 【期望结果：错误】
        ResourceSecurityKeyModel resourceSecurityKeyModel8 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel8 = new ResourceSecurityKeyModel();
        resourceSecurityKeyModel8.setIdentifier(uuid);
        resourceSecurityKeyModel8.setSecurityKey(securityKey);
        int id8 = 0;
        try {
        	id8 = this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel8);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test001Insert6", "error", e});
		}
        if( id8 > 0){
			 throw new TestException();
        }
    }
    
    /**
     * 用例： 查询资源文件密钥信息    
     * @throws Exception
     * @author gaoq
     */
    @Test
    public void test002FindSecurityKeyInfo() throws Exception {
    	//例1：uuid为空，其他参数正确 【期望结果：通过】
    	ResourceSecurityKeyModel resourceSecurityKeyModel = this.resourceSecurityKeyDao.findSecurityKeyInfo(null);
    	if(resourceSecurityKeyModel == null ){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test002FindSecurityKeyInfo1",  "pass", "test001Insert7 test pass"});
        } else {
 			throw new TestException();
        }
    	
    	
    	//例2：所有参数正确 【期望结果：通过】
    	ResourceSecurityKeyModel resourceSecurityKeyModel3 = this.resourceSecurityKeyDao.findSecurityKeyInfo(uuid);
    	if(resourceSecurityKeyModel3 != null ){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ResourceSecurityKeyDaoTest", "test002FindSecurityKeyInfo3",  "pass", "test001Insert7 test pass"});
        } else {
 			throw new TestException();
        }
    	
    	this.delete(uuid);
    }
    
}
