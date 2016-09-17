package nd.esp.service.lifecycle;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.app.LifeCircleSecurityConfig;
import nd.esp.service.lifecycle.app.LifeCircleWebConfig;
import nd.esp.service.lifecycle.app.LifeCycleEspStoreConfig;
import nd.esp.service.lifecycle.app.LifeCycleRedisCacheConfig;
import nd.esp.service.lifecycle.utils.common.CusAbstractSpringJunit4Config;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @title 接口单元测试基础配置
 * @Desc TODO
 * @see http 
 *      ://doc.sdp.nd/index.php?title=%E5%A6%82%E4%BD%95%E4%BD%BF%E7%94%A8WAF
 *      %E8%
 *      BF%9B%E8%A1%8C%E6%95%8F%E6%8D%B7%E6%B5%8B%E8%AF%95%EF%BC%88%E4%BA%8C%
 *      EF%BC%89
 * @author liuwx
 * @version 1.0
 * @create 2015年3月10日 下午3:15:09
 */
@ContextConfiguration(classes = {LifeCircleWebConfig.class,LifeCircleSecurityConfig.class,LifeCycleRedisCacheConfig.class,LifeCycleEspStoreConfig.class })
public class BaseControllerConfig extends CusAbstractSpringJunit4Config {
	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	private final static String USER_ID = (String)LifeCircleApplicationInitializer.properties.get("junit.userId");
	
	@Before
	public void before() {
		logger.info("BaseControllerConfig init data..");
		//createData();
		logger.info("BaseControllerConfig init data finish");
	}

	@Test
	public void doTest() {
		logger.error("nothing test to do");
	}

	@After
	public void after() {
	}

	@Override
	protected void initRealm() {
		//this.setRealm("waf.web.nd");
		this.setRealm("lc.service.esp.nd");
	}

	@Override
	protected void initUserId() {
		//this.setUserId("2107168459");
		this.setUserId(USER_ID);
	}
}
