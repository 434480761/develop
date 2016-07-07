package nd.esp.service.lifecycle.services.thirdpartybsys.v06;

/**
 * <p>Title: ThirdPartyBsysService  </p>
 * <p>Description: ThirdPartyBsysService </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年6月30日           </p>
 * @author lanyl
 */
public interface ThirdPartyBsysService {
    /**
     * 查询第三方列表是否存在
     * @param userId
     * @return
     * @author lanyl
     */
    boolean checkThirdPartyBsysList(String userId);

}
