package nd.esp.service.lifecycle.educommon.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.support.busi.tree.preorder.TreeTrargetAndParentModel;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.repository.model.Chapter;

/**
 * 教育资源通用接口的Service
 * <p>Create Time: 2015年6月23日           </p>
 * @author xiezy
 */
public interface NDResourceService {
    
    /**
     * 资源检索 
     * <p>Description:  资源检索升级目的主要是使得查询效率更高，准确度更高。
     * 使得用户可以根据分类维度数据，关系维度数据，覆盖范围，属性，关键字进行分页查询。 
     * 在这个几个条件下，优化数据结构，提高检索效率。            </p>
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           资源类型
     * @param includes    默认情况下，只返回资源的通用属性，不返回资源的其他扩展属性。
     *         ST：存储属性, LC：生命周期属性, EDU：教育属性, CG：分类维度数据属性, CV：覆盖范围属性, REP：存储空间属性, RE：关系属性
     *         该检索接口只支持:ST,EDU,LC,CG 
     * @param categories        分类维度数据
     * @param relations         关系维度数据
     * @param coverages         覆盖范围，根据目标类型，目标值以及覆盖方式进行查询
     * @param propsMap          资源属性参数
     * @param words             关键字
     * @param limit             分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @return
     */
	 public ListViewModel<ResourceModel> resourceQueryByEla(String resType,List<String> includes,Set<String> categories,
	    		Set<String> categoryExclude,List<Map<String,String>> relations,List<String> coverages,
	            Map<String,Set<String>> propsMap,Map<String, String> orderMap, String words,String limit,boolean isNotManagement,boolean reverse,
	            Boolean printable, String printableKey);
    
    /**
     * 资源检索 -- 直接查询数据库,数据可以保证实时性
     * <p>Description:  资源检索升级目的主要是使得查询效率更高，准确度更高。
     * 使得用户可以根据分类维度数据，关系维度数据，覆盖范围，属性，关键字进行分页查询。 
     * 在这个几个条件下，优化数据结构，提高检索效率。            </p>
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           资源类型
     * @param resCodes          支持多种资源查询,resType=eduresource时生效
     * @param includes    默认情况下，只返回资源的通用属性，不返回资源的其他扩展属性。
     *                    TI：技术属性, LC：生命周期属性, EDU：教育属性, CG：分类维度数据属性, CR:版权信息
     *         该检索接口只支持:TI,EDU,LC,CG,CR
     * @param categories        分类维度数据
     * @param relations         关系维度数据
     * @param coverages         覆盖范围，根据目标类型，目标值以及覆盖方式进行查询
     * @param words             关键字
     * @param limit             分页参数，第一个值为记录索引参数，第二个值为偏移量
     * @param isNotManagement 判断是否需要对ND库下的资源只允许查出ONLINE的限制
     * @param reverse 判断关系查询是否反转
     * @return
     */
    public ListViewModel<ResourceModel> resourceQueryByDB(
            String resType,String resCodes,List<String> includes,
            Set<String> categories,Set<String> categoryExclude,
            List<Map<String,String>> relations,List<String> coverages,
            Map<String,Set<String>> propsMap,Map<String, String> orderMap,
            String words,String limit,boolean isNotManagement,boolean reverse,
            Boolean printable, String printableKey,String statisticsType,String statisticsPlatform,
            boolean forceStatus,boolean showVersion);
    
    /**
     * 获取智能出题
     * <p>Create Time: 2015年12月24日   </p>
     * <p>Create author: xiezy   </p>
     * @param includesList
     * @param chapterId
     * @param pageSize
     * @param offset
     * @return
     */
    public ListViewModel<ResourceViewModel> resourceQuery4IntelliKnowledge(List<String> includesList,String chapterId,String pageSize,String offset);
    
    /**
     * 通用的资源统计
     * @param resType
     * @param categories
     * @param coverages
     * @param propsMap
     * @param groupBy
     * @param isNotManagement
     * @return
     */
    public Map<String, Integer> resourceStatistics(String resType, Set<String> categories, List<String> coverages,
    		Map<String, Set<String>> propsMap, String groupBy, boolean isNotManagement);
    
    /**
     * 获取资源详细
     * <p>Create Time: 2016年3月1日   </p>
     * <p>Create author: lsm   </p>
     * @param resourceType
     * @param uuid
     * @param includeList
     * @return
     */
    public ResourceModel getDetail(String resourceType, String uuid, List<String> includeList);
    
    /**
     * 
     * @author linsm
     * @param resourceType
     * @param uuid
     * @param includeList
     * @param isAll 不管状态enable
     * @return
     * @since
     */
    public ResourceModel getDetail(String resourceType, String uuid, List<String> includeList,Boolean isAll);
    
    /**
     * 批量获取资源详细
     * <p>Create Time: 2016年3月1日   </p>
     * <p>Create author: lsm   </p>
     * @param resourceType
     * @param uuidSet
     * @param includeList
     * @return
     */
    public List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList);
    
    /**
     * 批量获取资源详细
     * <p>Create Time: 2016年3月1日   </p>
     * <p>Create author: lsm   </p>
     * @param resourceType
     * @param uuidSet
     * @param includeList
     * @param isAll  不管状态enable
     * @return
     */
    public List<ResourceModel> batchDetail(String resourceType, Set<String> uuidSet, List<String> includeList,Boolean isAll);
  
    /**
     * 删除习题库的资源
     * @param resourceType
     * @param uuid
     */
    public void deleteInQuestionDB(String resourceType, String uuid);
	  
    /**
     * 资源删除	
     * <p>Create Time: 2016年3月1日   </p>
     * <p>Create author: lsm   </p>
     * @param resourceType
     * @param uuid
     */
    public void delete(String resourceType, String uuid);
    
    /**
     * 创建资源
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @return
     * @since
     */
    public ResourceModel create(String resourceType, ResourceModel resourceModel);
    
    /**
     * 更新资源
     * @author linsm
     * @param resourceType
     * @param resourceModel
     * @return
     * @since
     */
    public ResourceModel update(String resourceType, ResourceModel resourceModel);
    
    /**
     * 创建资源(支持分库)
     * @author xuzy
     * @param resourceType
     * @param resourceModel
     * @return
     * @since
     */
    public ResourceModel create(String resourceType, ResourceModel resourceModel,DbName dbName);
    
    /**
     * 更新资源(支持分库)
     * @author xuzy
     * @param resourceType
     * @param resourceModel
     * @return
     * @since
     */
    public ResourceModel update(String resourceType, ResourceModel resourceModel,DbName dbName);
    
    /**
     * CS文件上传
     * 
     * <br>Created 2015年5月13日 下午4:05:01
     * @param resourceType 资源类型
     * @param uuid 资源id
     * @param uid 用户id
     * @return
     * @author       linsm
     */
    AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew, String coverage);
    AccessModel getUploadUrl(String resourceType, String uuid, String uid, Boolean renew);
    
    /**
     * 下载
     * <p>Create Time: 2016年3月1日   </p>
     * <p>Create author: lsm   </p>
     * @param resourceType
     * @param uuid
     * @param uid
     * @param key
     * @return
     */
    AccessModel getDownloadUrl(String resourceType, String uuid, String uid, String key);
    
    /**
     * 获取资源的预览列表
     * 
     * @author:xuzy
     * @date:2015年9月28日
     * @param resType
     * @param uuid
     * @return
     */
    public Map<String,Object> getResPreviewUrls(String resType,String uuid);

    Map<String, Object> getResPreviewByHref(String resType,String location);
    
    public TreeTrargetAndParentModel getTreeTargetAndParent(KnowledgeModel knowledgeModel, Chapter knowledge);
}
