package nd.esp.service.lifecycle.services.educationrelation.v06;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.models.v06.BatchAdjustRelationOrderModel;
import nd.esp.service.lifecycle.models.v06.EducationRelationModel;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForPathViewModel;
import nd.esp.service.lifecycle.vos.educationrelation.v06.RelationForQueryViewModel;

public interface EducationRelationServiceV06 {
    
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
     * 获取资源之间的关系    -- 目前只针对 课件颗粒-课时-教材章节 使用
     * 
     * @param resType              源资源类型
     * @param sourceUuid           源资源的id标识
     * @param relationPath         查询的关系路径
     * @param reverse              关系是否进行反转
     * @param categoryPattern      分类维度的应用模式
     * @return
     */
    List<List<RelationForPathViewModel>> getRelationsByConditions(String resType,
                                                                  String sourceUuid,
                                                                  List<String> relationPath,
                                                                  boolean reverse,
                                                                  String categoryPattern);
    
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
    
    /**
     * 关系目标资源检索 
     * <p>Create Time: 2015年10月21日   </p>
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param categories        分类维度数据
     * @param targetType       目标资源类型
     * @param relationType     关系类型
     * @param limit            分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     * @param coverage         覆盖范围参数,格式:ctType/cTarget/ct
     * @return
     */
    public ListViewModel<RelationForQueryViewModel> queryListByResTypeByDB(String resType,
                                                                String sourceUuid,
                                                                String categories,
                                                                String targetType,
                                                                String relationType,
                                                                String limit,
                                                                boolean reverse,
                                                                String coverage);
    
    /**
     * 关系目标资源检索(支持label和tags)
     * 
     * @param resType
     * @param sourceUuid
     * @param categories
     * @param targetType
     * @param label
     * @param tags
     * @param relationType
     * @param limit
     * @param reverse
     * @param coverage
     * @return
     * @since
     */
    public ListViewModel<RelationForQueryViewModel> queryListByResTypeByDB(String resType,
                                                                    String sourceUuid,
                                                                    String categories,
                                                                    String targetType,
                                                                    String label,
                                                                    String tags,
                                                                    String relationType,
                                                                    String limit,
                                                                    boolean reverse,
                                                                    String coverage);

    /**
     * 获取教材章节下包含子节点的所有知识点列表 
     * <p>Create Time: 2015年6月10日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType          源资源类型
     * @param sourceUuid       源资源id
     * @param categories       分类维度数据
     * @param targetType       目标资源类型
     * @param label            资源关系标识
     * @param tags             资源关系标签
     * @param relationType     关系类型
     * @param limit            分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param coverage         覆盖范围参数,格式:ctType/cTarget/ct
     * @return
     * @throws Exception
     */
    public ListViewModel<RelationForQueryViewModel> recursionQueryResourcesByDB(String resType,
                                                                         String sourceUuid,
                                                                         String categories,
                                                                         String targetType,
                                                                         String label,
                                                                         String tags,
                                                                         String relationType,
                                                                         String limit,
                                                                         String coverage) throws EspStoreException;
    
    /**
     * 在有些情景下，单个的获取源资源的目标资源列表的接口，业务系统使用起来过于频繁。此时业务方提出需要能够进行设置批量的源资源ID，
     * 通过源资源的ID快速的查询目标资源的列表。
                 1.接口提供设置源资源ID的列表进行批量查询
                 2.接口提供设置关系的类型
                 3.接口提供设置目标资源的类型    
     * <p>Create Time: 2015年10月19日   </p>
     * <p>Create author: caocr   </p>
     * @param resType 源资源类型
     * @param sids 源资源id，可批量
     * @param targetType 目标资源类型
     * @param relationType 关系类型
     * @param limit 分页参数
     */
    public ListViewModel<RelationForQueryViewModel> batchQueryResourcesByDB(String resType,
                                                                     Set<String> sids,
                                                                     String targetType,
                                                                     String relationType,
                                                                     String limit);
    /**
     * 在有些情景下，单个的获取源资源的目标资源列表的接口，业务系统使用起来过于频繁。此时业务方提出需要能够进行设置批量的源资源ID，
     * 通过源资源的ID快速的查询目标资源的列表。
                 1.接口提供设置源资源ID的列表进行批量查询
                 2.接口提供设置关系的类型
                 3.接口提供设置目标资源的类型
     * <p>Create Time: 2015年10月19日   </p>
     * <p>Create author: caocr   </p>
     * @param resType 源资源类型
     * @param sids 源资源id，可批量
     * @param targetType 目标资源类型
     * @param label            资源关系标识
     * @param tags             资源关系标签
     * @param relationType 关系类型
     * @param limit 分页参数
     * @param reverse          指定查询的关系是否进行反向查询。如果reverse参数不携带，默认是从s->t查询，如果reverse=true，反向查询T->S
     */
    public ListViewModel<RelationForQueryViewModel> batchQueryResourcesByDB(String resType,
                                                                     Set<String> sids,
                                                                     String targetType,
                                                                     String label,
                                                                     String tags,
                                                                     String relationType,
                                                                     String limit,
                                                                     boolean reverse);

    /**
     * 判断关系是否存在
     * 
     * @param sourceId
     * @param targetId
     * @param relationType
     * @param label
     * @return
     * @since
     */
    public EducationRelationModel relationExist(String sourceId, String targetId, String relationType, String label);

    /**
     * 根据知识点id递归查找上级节点（直到一级目录）
     * 根据知识点id查找同级节点
     * @param uuid
     * @return
     */
    public List<Map<String,Object>> queryKnowledgeTree(String uuid);
    
    /**
     * 查询套件目录树
     * @return
     */
    public List<Map<String,Object>> querySuiteDirectory();
}
