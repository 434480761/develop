package nd.esp.service.lifecycle.services.educationrelation.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.v06.BatchAdjustRelationOrderModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;

public interface EducationRelationServiceForQuestionV06 {
    
    /**
     * 创建资源关系(实际上有批量创建资源关系的能力)
     * 
     * @param educationRelationModels               创建时传入的model
     * @param isCreateWithResource                 判断是否是在创建资源时同时创建关系
     * @return
     */
    List<EducationRelationModel> createRelation(List<EducationRelationModel> educationRelationModels,
                                              boolean isCreateWithResource);

    /**
     * 修改资源关系
     * 
     * @param resType 资源类型
     * @param sourceUuid 源资源id
     * @param rid 资源关系id
     * @param educationRelationModel 资源关系
     * @return
     * @since
     */
    EducationRelationModel updateRelation(String resType,
                                          String sourceUuid,
                                          String rid,
                                          EducationRelationModel educationRelationModel);

    /**
     * 刪除资源关系
     * 
     * @param rid 资源关系id
     * @param resType 源资源类型
     * @param sourceUuid 源资源id
     * @return
     * @since
     */
    boolean deleteRelation(String rid, String sourceUuid, String resType);

    /**
     * 根据目标类型删除资源之间的关系
     *   
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param targetType       目标对象类型
     * @param relationType     关系类型
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @return
     */
    boolean deleteRelationByTargetType(String resType,
                                       String sourceUuid,
                                       List<String> targetType,
                                       String relationType,
                                       boolean reverse);

    /**
     * 条件删除资源之间的关系  
     * 
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param target           目标对象的id集合
     * @param relationType     关系类型
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @return
     */
    boolean deleteRelationByTarget(String resType,
                                   String sourceUuid,
                                   List<String> target,
                                   String relationType,
                                   boolean reverse);

    /**
     * 批量修改资源关系的顺序  
     * 
     * @param resType          元资源的类型
     * @param sourceUuid       源资源的id
     * @param target           需要移动的目标对象
     * @param destination      移动目的地靶心对象
     * @param adjoin           相邻对象的id，如果在第一个和最后一个的时候，不存在相邻对象，传入为none。
     * @param at               移动的方向标识，first是移动到第一个位置，last是将这个关系增加到列表的最后，middle是将目标增加到destination和adjoin中间。
     */
    void batchAdjustRelationOrder(String resType,
                                  String sourceUuid,
                                  List<BatchAdjustRelationOrderModel> batchAdjustRelationOrderModels);
    
}
