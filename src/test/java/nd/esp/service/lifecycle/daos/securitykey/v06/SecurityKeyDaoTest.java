package nd.esp.service.lifecycle.daos.securitykey.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;
import nd.esp.service.lifecycle.models.SecurityKeyModel;
import nd.esp.service.lifecycle.utils.common.StringTestUtil;
import nd.esp.service.lifecycle.utils.common.TestException;
import nd.esp.service.lifecycle.utils.encrypt.DESUtils;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: Dao层SecurityKeyDaoTest测试  </p>
 * <p>Description: SecurityKeyDaoTest </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月26日           </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityKeyDaoTest extends BaseControllerConfig {
    private final static Logger logger = LoggerFactory.getLogger(SecurityKeyDaoTest.class);
    
    @Autowired
    private SecurityKeyDao securityKeyDao;
    private static String uuid;
    private static String securityKey;
    private static String userId;

    @Autowired
	private BaseDao<SecurityKeyModel> baseDao;
	private static String  TABLE_POSTFIX = "security_key";

    
    /**
     * 删除资源文件密钥信息      
     * @param resourceSecurityKeyModel
     * @return
     * @author gaoq
     */
	private int delete(String userId){
		return this.baseDao.delete(" and user_id = ?", new Object[] { userId }, TABLE_POSTFIX);
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
        securityKey = DESUtils.getSecurityKey(); 
        userId = StringTestUtil.randomNumbers(20);
        SecurityKeyModel securityKeyModel = new SecurityKeyModel();
        securityKeyModel.setUserId(userId);
        securityKeyModel.setSecurityKey(securityKey);
        int id = 0;
        try {
        	id = this.securityKeyDao.insert(securityKeyModel);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert", "error", e});
		}
        if( id > 0){
			 throw new TestException();
        }
        
        //例2：uuid 不为空 长度超过36，其他参数正确 【期望结果：错误】
        uuid = StringTestUtil.randomNumbers(37);
        SecurityKeyModel securityKeyModel2 = new SecurityKeyModel();
        securityKeyModel2.setIdentifier(uuid);
        securityKeyModel2.setUserId(userId);
        securityKeyModel2.setSecurityKey(securityKey);
        int id2 = 0;
        try {
        	id2 = this.securityKeyDao.insert(securityKeyModel2);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert2", "error", e});
		}
        if( id2 > 0){
			 throw new TestException();
        }
        uuid = UUID.randomUUID().toString();
        
        //例3：securityKey 为空 ，其他参数正确 【期望结果：错误】
        SecurityKeyModel securityKeyModel3 = new SecurityKeyModel();
        securityKeyModel3.setIdentifier(uuid);
        securityKeyModel3.setUserId(userId);
        int id3 = 0;
        try {
        	id3 = this.securityKeyDao.insert(securityKeyModel3);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert3", "error", e});
		}
        if( id3 > 0){
			 throw new TestException();
        }
        
        
        //例4：securityKey 不为空 长度超过64，其他参数正确 【期望结果：错误】
        securityKey = StringTestUtil.randomNumbers(65);
        SecurityKeyModel securityKeyModel4 = new SecurityKeyModel();
        securityKeyModel4.setIdentifier(uuid);
        securityKeyModel4.setSecurityKey(securityKey);
        securityKeyModel4.setUserId(userId);

        int id4 = 0;
        try {
        	id4 = this.securityKeyDao.insert(securityKeyModel4);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert4", "error", e});
		}
        if( id4 > 0){
			 throw new TestException();
        }
        securityKey = StringTestUtil.randomNumbers(60);
        
        //例5：userId 为空 ，其他参数正确 【期望结果：错误】
        SecurityKeyModel securityKeyModel5 = new SecurityKeyModel();
        securityKeyModel5.setIdentifier(uuid);
        securityKeyModel5.setSecurityKey(securityKey);
        int id5 = 0;
        try {
        	id5 = this.securityKeyDao.insert(securityKeyModel5);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert5", "error", e});
		}
        if( id5 > 0){
			 throw new TestException();
        }
        
        
        //例4：userId 不为空 长度超过36，其他参数正确 【期望结果：错误】
        SecurityKeyModel securityKeyModel6 = new SecurityKeyModel();
        securityKeyModel6.setIdentifier(uuid);
        securityKeyModel6.setSecurityKey(securityKey);
        securityKeyModel6.setUserId(StringTestUtil.randomNumbers(37));

        int id6 = 0;
        try {
        	id6 = this.securityKeyDao.insert(securityKeyModel6);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert6", "error", e});
		}
        if( id6 > 0){
			 throw new TestException();
        }
        
        //例7：所有参数正确 【期望结果：通过】
        SecurityKeyModel securityKeyModel7 = new SecurityKeyModel();
        uuid = StringTestUtil.randomNumbers(30);
        securityKeyModel7.setIdentifier(uuid);
        securityKeyModel7.setSecurityKey(securityKey);
        securityKeyModel7.setUserId(userId);
        this.securityKeyDao.insert(securityKeyModel7);

        SecurityKeyModel model = this.securityKeyDao.findSecurityKeyInfo(this.userId);
        if(model != null ){
        	logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert7",  "pass", "test001Insert7 test pass"});
        } else {
			 throw new TestException();
        }
       
        //例8：唯一性验证 其他参数正确 【期望结果：错误】
        SecurityKeyModel securityKeyModel8 = new SecurityKeyModel();
        securityKeyModel8.setIdentifier(uuid);
        securityKeyModel8.setSecurityKey(securityKey);
        securityKeyModel8.setUserId(userId);
        int id8 = 0;
        try {
        	id8 = this.securityKeyDao.insert(securityKeyModel8);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test001Insert8", "error", e});
		}
        if( id8 > 0){
			 throw new TestException();
        }
    }

    /**
     * 用例： 更新资源文件密钥信息    
     * @throws Exception
     * @author gaoq
     */
    @Test
    public void test002Update() throws Exception {
    	SecurityKeyModel securityKeyModel = new SecurityKeyModel();
    	securityKeyModel.setSecurityKey(securityKey);
    	securityKeyModel.setUserId(userId);
    	securityKeyModel.setIdentifier(uuid);
    	
        //例1：uuid 长度超过36 其他参数正确【期望结果：错误】
    	securityKeyModel.setIdentifier(StringTestUtil.randomNumbers(37));
    	int count = 0;
        try {
        	count = this.securityKeyDao.update(securityKeyModel);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test002Update1", "error", e});
		}
        if( count > 0){
			 throw new TestException();
        }
    	securityKeyModel.setIdentifier(uuid);
  	
    	
        //例2：securityKey 长度超过64 其他参数正确【期望结果：错误】
    	securityKeyModel.setIdentifier(StringTestUtil.randomNumbers(70));
    	int count2 = 0;
        try {
        	count2 = this.securityKeyDao.update(securityKeyModel);		
		} catch (Exception e) {
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test002Update2", "error", e});
		}
        if( count2 > 0){
			 throw new TestException();
        }
    	securityKeyModel.setIdentifier(securityKey);          

        //例6：所有参数正确【期望结果：正确】
    	securityKeyModel = this.securityKeyDao.findSecurityKeyInfo(userId);
    	securityKeyModel.setSecurityKey(StringTestUtil.randomNumbers(30));
    	//securityKeyModel.setIdentifier(StringTestUtil.randomNumbers(10));
    	int count6 = this.securityKeyDao.update(securityKeyModel);	      
    	if(count6 > 0 ){
        	logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test002Update6",  "pass", "test002Update6 test pass"});
        } else {
			 throw new TestException();
        }
    }

    /**
     * 用例： 查询用户密钥信息    
     * @throws Exception
     * @author gaoq
     */
    @Test
    public void test003FindSecurityKeyInfo() throws Exception {
    	//例1：userid为空，其他参数正确 【期望结果：通过】
    	SecurityKeyModel securityKeyModel = this.securityKeyDao.findSecurityKeyInfo(null);
    	if(securityKeyModel == null ){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test003FindSecurityKeyInfo",  "pass", "test003FindSecurityKeyInfo test pass"});
        } else {
 			throw new TestException();
        }
    	
    	
    	//例2：所有参数正确 【期望结果：通过】
    	SecurityKeyModel securityKeyModel2 = this.securityKeyDao.findSecurityKeyInfo(this.userId);
    	if(securityKeyModel2 != null ){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"SecurityKeyDaoTest", "test003FindSecurityKeyInfo2",  "pass", "test003FindSecurityKeyInfo2 test pass"});
        } else {
 			throw new TestException();
        }
    	
    	this.delete(this.userId);
    }
}
