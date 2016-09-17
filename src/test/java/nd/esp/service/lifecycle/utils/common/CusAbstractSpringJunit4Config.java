package nd.esp.service.lifecycle.utils.common;

/**
 * <p>Title: CusAbstractSpringJunit4Config </p>
 * <p>Description: CusAbstractSpringJunit4Config </p>
 * <p>Copyright: Copyright (c) 2015 </p>
 * <p>Company: ND Websoft Inc. </p>
 * <p>Create Time: 2016年07月01日 </p>
 * @author lanyl
 * @version 0.1
 */
public abstract class CusAbstractSpringJunit4Config extends CusBaseSpringJunit4Config {
	public CusAbstractSpringJunit4Config() {
	}

	public void setUp() {
		this.initRealm();
		this.initUserId();
		super.setUp();
	}

	protected abstract void initRealm();

	protected abstract void initUserId();
}