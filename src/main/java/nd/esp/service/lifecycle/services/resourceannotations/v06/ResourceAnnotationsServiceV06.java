package nd.esp.service.lifecycle.services.resourceannotations.v06;

import nd.esp.service.lifecycle.models.v06.ResourceAnnotationModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

/**
 * Created by caocr on 2015/11/25 0025.
 */
public interface ResourceAnnotationsServiceV06 {
    /**
     * 添加资源评注
     * @param model 资源评注
     * @param resType 被添加资源类型
     * @param resId 被添加资源id
     * @return 添加后的资源评注
     */
    ResourceAnnotationModel addResourceAnnotation(ResourceAnnotationModel model, String resType, String resId);

    /**
     * 修改资源评注
     * @param model 资源评注
     * @param resType 被添加资源类型
     * @param resId 被添加资源id
     * @return 添加后的资源评注
     */
    ResourceAnnotationModel updateResourceAnnotation(ResourceAnnotationModel model, String resType, String resId);

    /**
     * 通过资源评注id删除资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param annoId 评注id
     * @return
     * @since
     */
    boolean deleteResourceAnnotationByAnnoId(String resType, String resId, String annoId);
    
    /**
     * 通过资源id删除资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @return
     * @since
     */
    boolean deleteResourceAnnotationByResId(String resType, String resId);
    
    /**
     * 通过用户id删除资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param entityId 用户id
     * @return
     * @since
     */
    boolean deleteResourceAnnotationByEntityId(String resType, String resId, String entityId);

    /**
     * 通过资源id查询资源评注
     * 
     * @param resType 资源类型
     * @param resId 资源id
     * @param limit 分页参数
     * @return 资源评注列表
     * @since
     */
    ListViewModel<ResourceAnnotationModel> queryResourceAnnotationsByResId(String resType, String resId, String limit);
}
