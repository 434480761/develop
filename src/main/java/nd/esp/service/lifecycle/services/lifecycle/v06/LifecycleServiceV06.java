package nd.esp.service.lifecycle.services.lifecycle.v06;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.models.ResContributeModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.lifecycle.v06.ResContributeViewModel;

/**
 * @author qil
 * @version 1.0
 * @created 17-7月-2015 12:06:04
 */
public interface LifecycleServiceV06 {
    
    /**
     * 增加生命周期阶段。 对于生命周期的某个环节，进行添加
     * @Method POST
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    public ResContributeViewModel addLifecycleStep(String resType, String resId, ResContributeModel contributeModel);
    
    public ResContributeViewModel addLifecycleStep(String resType, String resId, ResContributeModel contributeModel, boolean bUpdateTime);
    
    /**
     * 根据状态来添加生命周期，并更新资源状态 
     * @author linsm
     * @param resType
     * @param resId
     * @param isSuccess 操作是否成功 (目前只适用于转码，与状态藕合在一起)
     * @param message
     * @return
     * @since
     */
    public ResContributeViewModel addLifecycleStep(String resType, String resId, Boolean isSuccess, String message);

    /**
     * 批量资源增加生命周期阶段。 可以对批量资源进行同一生命周期及阶段的添加
     * @Method POST
     * 
     * @urlpattern  {res_type}/lifecycle/steps/bulk
     * 
     * @param resType
     * @param resIds
     * @param contribute
     */
    public Map<String,ResContributeViewModel> addLifecycleStepBulk(String resType, List<String> resIds, ResContributeModel contributeModel);

    /**
     * 获取指定资源的生命周期阶段详细。通过资源uuid获取资源阶段的详细信息
     * @Method GET
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps?limit=(0,20)
     * 
     * @param resType
     * @param uuid
     */
    public ListViewModel<ResContributeViewModel> getLifecycleSteps(String resType, String uuid, String limit);

    /**
     * 修改资源生命周期阶段。通过资源的uuid，生命周期阶段id，修改当前阶段的信息
     * @Method PUT
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps/{uuid}
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    public ResContributeViewModel modifyLifecycleStep(String resType, String resId, ResContributeModel contributeModel);
    
    /**
     * 批量资源修改生命周期阶段。 批量修改资源的阶段生命周期阶段信息
     * @Method PUT
     * @urlpattern  {res_type}/lifecycle/steps/bulk
     * 
     * @param resType
     * @param resIds
     * @param contributes
     */
    public Map<String,ResContributeViewModel> modifyLifecycleStepBulk(String resType, List<String> resIds, 
            List<ResContributeModel> contributeModels);
    
    /**
     * 删除资源生命周期阶段。通过ID删除生命周期阶段信息
     * @Method DELETE
     * @urlpattern  {res_type}/{uuid}/lifecycle/steps
     * 
     * @param resType
     * @param resId
     * @param contribute
     */
    public boolean delLifecycleStep(String resType, String resId, String stepId);
    
    /**
     * 批量删除资源生命周期阶段。 通过ID数组，批量删除生命周期阶段信息
     * @Method DELETE
     * @urlpattern  {res_type}/lifecycle/steps/bulk
     * 
     * @param resType
     * @param resId
     * @param contributes
     */
    public boolean delLifecycleStepsBulk(String resType, String resId, Set<String> stepIds);
}
