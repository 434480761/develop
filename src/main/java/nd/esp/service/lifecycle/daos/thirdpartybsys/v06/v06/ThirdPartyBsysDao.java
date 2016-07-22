package nd.esp.service.lifecycle.daos.thirdpartybsys.v06.v06;

import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;

/**
 * <p>Title: ThirdPartyBsysDao</p>
 * <p>Description: ThirdPartyBsysDao</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
public interface ThirdPartyBsysDao {

	/**
	 * 查询第三方服务
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	public ThirdPartyBsysModel findThirdPartyBsys(String userId);

}
