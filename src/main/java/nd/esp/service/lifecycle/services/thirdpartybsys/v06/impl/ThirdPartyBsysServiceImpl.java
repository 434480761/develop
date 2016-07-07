package nd.esp.service.lifecycle.services.thirdpartybsys.v06.impl;

import nd.esp.service.lifecycle.daos.thirdpartybsys.v06.ThirdPartyBsysDao;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;
import nd.esp.service.lifecycle.services.thirdpartybsys.v06.ThirdPartyBsysService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Title: ThirdPartyBsysServiceImpl   </p>
 * <p>Description: ThirdPartyBsysServiceImpl </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年6月30日           </p>
 * @author lanyl
 */
@Service(value="ThirdPartyBsysServiceImpl")
public class ThirdPartyBsysServiceImpl implements ThirdPartyBsysService {

	@Autowired
	private ThirdPartyBsysDao thirdPartyBsysDao;

	/**
	 * 查询第三方列表是否存在
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	public boolean checkThirdPartyBsysList(String userId) {
		ThirdPartyBsysModel model = this.thirdPartyBsysDao.findThirdPartyBsys(userId);
		if(model != null){
			return true;
		}else {
			return false;
		}
    }
}
