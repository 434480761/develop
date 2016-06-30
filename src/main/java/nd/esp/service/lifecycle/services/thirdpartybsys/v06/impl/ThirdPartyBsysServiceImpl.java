package nd.esp.service.lifecycle.services.thirdpartybsys.v06.impl;

import java.util.List;

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
 * @author lianggz
 */
@Service(value="ThirdPartyBsysServiceImpl")
public class ThirdPartyBsysServiceImpl implements ThirdPartyBsysService {

	@Autowired
	private ThirdPartyBsysDao thirdPartyBsysDao;

    @Override
    public List<ThirdPartyBsysModel> findThirdPartyBsysList() {
        return this.thirdPartyBsysDao.findThirdPartyBsysList();
    }
}
