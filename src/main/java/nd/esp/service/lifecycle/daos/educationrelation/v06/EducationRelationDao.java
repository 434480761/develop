package nd.esp.service.lifecycle.daos.educationrelation.v06;

import java.util.List;
import java.util.Set;

import nd.esp.service.lifecycle.models.v06.ResourceRelationResultModel;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;

/**
 * 资源关系查询目标资源
 * 
 * @author caocr
 *
 */
public interface EducationRelationDao {
    
    /**
     * 通过关系检索目标资源
     * 
     * @param resType 源资源类型
     * @param sourceUuid 源资源id（在通过资源关系批量检索源资源的目标资源列表API LCMS API RR00071中传null）
     * @param categories 目标资源分类维度，可以传多个，格式为："d1,d2,d3"（在通过资源关系批量检索源资源的目标资源列表API LCMS API RR00071中传null）
     * @param targetType 目标资源类型
     * @param label 资源关系标识
     * @param tags 资源关系标签
     * @param relationType 资源关系类型，目前默认为：ASSOCIATE
     * @param limit 分页参数：(0,20)
     * @param reverse 指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S（LCMS API
     *            RR00070传false；递归查询和章节关系的目标资源传false）
     * @param coverage 覆盖范围参数,格式:ctType/cTarget/ct（在通过资源关系批量检索源资源的目标资源列表API LCMS API RR00071中传null）
     * @param chapterIds 批量源资源id，在关系目标资源检索接口LCMS API RR00070中的递归查询和章节关系的目标资源是指章节和子章节id；在通过资源关系批量检索源资源的目标资源列表API LCMS API
     *            RR00071中是指源资源id。
     * @param isNeedSid 是否需要显示源资源id
     * @return
     * @since
     */
    ListViewModel<RelationForQueryViewModel> queryResByRelation(String resType,
                                                                List<String>  sourceUuids,
                                                                String categories,
                                                                String targetType,
                                                                String label,
                                                                String tags, 
                                                                String relationType,
                                                                String limit,
                                                                boolean reverse,
                                                                String coverage,
                                                                boolean isPortal);

    /**
     * 获取资源关系
     * 
     * @param source
     * @param resType
     * @return
     * @since
     */
    List<ResourceRelationResultModel> getResourceRelationsWithOrder(String source, String resType);
    
    /**
     * 获取资源关系
     * 
     * @param source
     * @param resType
     * @param resourceTargetType
     * @return
     * @since
     */
    List<ResourceRelationResultModel> getResourceRelations(String source, String resType, String resourceTargetType);

    List<ResourceRelation> batchGetRelationByResourceSourceOrTarget(String primaryCategory, Set<String> uuidsSet);
}
