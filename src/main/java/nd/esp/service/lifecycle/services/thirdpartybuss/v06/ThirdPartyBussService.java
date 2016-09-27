package nd.esp.service.lifecycle.services.thirdpartybuss.v06;

import nd.esp.service.lifecycle.models.ApiModel;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModle;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface ThirdPartyBussService {
    
    /**
     * 注册第三方服务。
     * @Method POST
     * @urlpattern  /v0.6/3dbsys/servicekey/registry
     * 
     * @param service
     */
    ThirdPartyBsysModle registerService(ThirdPartyBsysModle service, boolean isAuto);
    
    /**
     * 查询第三方服务。
     * @Method GET
     * @urlpattern  /v0.6/3dbsys/servicekey?bsysname=备课&admin=johnny&limit=(0,20)
     * 
     * @param bsysname
     * @param admin
     * @param limit
     */
    ListViewModel<ThirdPartyBsysModle> queryServiceInfo(String bsysname, String admin, String limit);
    
    /**
     * 删除第三方服务。通过ID删除第三方服务
     * @Method DELETE
     * @urlpattern  /v0.6/3dbsys/servicekey/{uuid}
     * 
     * @param uuid
     */
    boolean deleteService(String uuid);
    
    /**
     * 修改第三方服务。通过ID修改第三方服务
     * @Method PUT
     * @urlpattern  /v0.6/3dbsys/servicekey/{uuid}
     * 
     * @param uuid
     */
    ThirdPartyBsysModle modifyService(String uuid, ThirdPartyBsysModle bsysModel);
    
    
    /**
     * 查询所有接口列表
     * @Method GET
     * @urlpattern  /v0.6/api/list 
     * 
     */
    ListViewModel<ApiModel> queryApiList();

    
}
