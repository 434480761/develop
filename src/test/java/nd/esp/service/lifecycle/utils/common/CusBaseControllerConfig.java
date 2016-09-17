package nd.esp.service.lifecycle.utils.common;

import nd.esp.service.lifecycle.app.*;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;


/**
 * <p>Title: CusBaseControllerConfig </p>
 * <p>Description: CusBaseControllerConfig </p>
 * <p>Copyright: Copyright (c) 2015 </p>
 * <p>Company: ND Websoft Inc. </p>
 * <p>Create Time: 2016年07月01日 </p>
 * @author lanyl
 * @version 0.1
 */
@ContextConfiguration(classes = {LifeCircleWebConfig.class,LifeCircleSecurityConfig.class,LifeCycleRedisCacheConfig.class,LifeCycleEspStoreConfig.class })
public class CusBaseControllerConfig extends CusAbstractSpringJunit4Config {
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
