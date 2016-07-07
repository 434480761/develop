package nd.esp.service.lifecycle.daos.thirdpartybsys.v06;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.daos.thirdpartybsys.v06.v06.ThirdPartyBsysDao;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;
import nd.esp.service.lifecycle.utils.common.TestException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: Dao层ThirdPartyBsysDao测试</p>
 * <p>Description: ThirdPartyBsysDaoTest</p>
 * <p>Copyright: Copyright (c) 2016 </p>
 * <p>Company: ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/2 </p>
 * <p>MethodSorters 按字母顺序 TEST若有顺序要求，要注意方法名称书写 </p>
 * @author gaoq
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ThirdPartyBsysDaoTest extends BaseControllerConfig{
    
    private static Logger logger = LoggerFactory.getLogger(ThirdPartyBsysDaoTest.class);
    private static String userId = "2080538299";

    @Autowired 
    private ThirdPartyBsysDao thirdPartyBsysDao;
    
    /**
     * 用例：查询第三方服务
     * @author lanyl
     * @throws Exception
     */
    @Test
    public void Test001FindThirdPartyBsys() throws Exception {
    	//例1：userId 为空 【期望结果：通过】
		ThirdPartyBsysModel model = this.thirdPartyBsysDao.findThirdPartyBsys(null);
    	if(model == null){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ThirdPartyBsysDaoTest", "Test001FindThirdPartyBsys", "pass", "Test001FindThirdPartyBsys test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userId 不为空 值为不存在 【期望结果：通过】
		ThirdPartyBsysModel model2 = this.thirdPartyBsysDao.findThirdPartyBsys("-1");
    	if(model2 == null){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ThirdPartyBsysDaoTest", "Test001FindThirdPartyBsys", "pass", "Test001FindThirdPartyBsys test pass"});
    	} else {
			throw new TestException();
		}
    	
    	//例2：userId 不为空 值为存在 【期望结果：通过】
		ThirdPartyBsysModel model3 = this.thirdPartyBsysDao.findThirdPartyBsys(userId);
    	if(model3 != null){
            logger.info("class:{}, method:{}, status:{}, msg:{}", new Object[] {"ThirdPartyBsysDaoTest", "Test001FindThirdPartyBsys", "pass", "Test001FindThirdPartyBsys test pass"});
    	} else {
			throw new TestException();
		}
    	
    }

}
