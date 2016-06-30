package nd.esp.service.lifecycle.services.thirdpartybsys.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;

/**
 * <p>Title: ThirdPartyBsysService  </p>
 * <p>Description: ThirdPartyBsysService </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年6月30日           </p>
 * @author lianggz
 */
public interface ThirdPartyBsysService {
    /**
     * 查询第三方列表
     * @return
     * @author lianggz
     */
    List<ThirdPartyBsysModel> findThirdPartyBsysList();

}
