package nd.esp.service.lifecycle.daos.thirdpartybsys.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;

/**
 * <p>Title: ThirdPartyBsysDao</p>
 * <p>Description: ThirdPartyBsysDao</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/30 </p>
 *
 * @author lianggz
 */
public interface ThirdPartyBsysDao {

	/**
	 * 查询第三方列表
	 * @return
	 * @author lianggz
	 */
	List<ThirdPartyBsysModel> findThirdPartyBsysList();

}
