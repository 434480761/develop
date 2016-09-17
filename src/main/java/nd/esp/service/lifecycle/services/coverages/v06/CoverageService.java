package nd.esp.service.lifecycle.services.coverages.v06;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.models.coverage.v06.CoverageModel;
import nd.esp.service.lifecycle.models.coverage.v06.CoverageModelForUpdate;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;

/**
 * 公私有资源库 业务逻辑层
 * <p>Create Time: 2015年6月17日           </p>
 * @author xiezy
 */
public interface CoverageService {
    
    /**
     * 增加覆盖范围
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverageModel     创建时的入参
     * @return
     */
    public CoverageViewModel createCoverage(CoverageModel coverageModel);
    
    /**
     * 批量增加覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverageModels        创建时的入参集合
     * @return
     */
    public List<CoverageViewModel> batchCreateCoverage(List<CoverageModel> coverageModels,boolean isCreateWithResource);
    
    /**
     * 获取资源覆盖范围 
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcid      覆盖范围的id
     * @return
     */
    public CoverageViewModel getCoverageDetail(String rcid);
    
    /**
     * 批量获取资源覆盖范围 
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcids      覆盖范围的id集合
     * @return
     */
    public Map<String,CoverageViewModel> batchGetCoverageDetail(List<String> rcids);
    
    /**
     * 修改资源覆盖范围	
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param coverageModelForUpdate    修改时的入参
     * @return
     */
    public CoverageViewModel updateCoverage(CoverageModelForUpdate coverageModelForUpdate);
    
    /**
     * 删除资源覆盖范围	
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcid      资源覆盖范围id
     * @return
     */
    public boolean deleteCoverage(String rcid);
    
    /**
     * 批量删除资源覆盖范围 
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param rcids      资源覆盖范围id集合
     * @return
     */
    public boolean batchDeleteCoverage(List<String> rcids);
    
    /**
     * 通过目标类型，覆盖范围策略，目标范围的标识，资源类型，目标资源标识删除覆盖范围  
     * <p>Create Time: 2015年7月1日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType           源资源类型
     * @param resourceId        源资源Id
     * @param target            目标标识
     * @param targetType        覆盖范围的目标类型
     * @param strategy          覆盖策略
     * @return
     */
    public boolean batchDeleteCoverageByCondition(String resType,String resourceId,String target,String targetType,String strategy);
    
    /**
     * 获取某个资源所覆盖的范围 
     * <p>Create Time: 2015年6月19日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType       源资源类型
     * @param resUuid       源资源id
     * @return
     */
    public List<CoverageViewModel> getCoveragesByResource(String resType,String resUuid,String targetType,String target,String strategy);
    
    /**
     * 批量获取多个资源所覆盖的范围   
     * <p>Create Time: 2015年8月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType       源资源类型
     * @param rids
     * @return
     */
    public Map<String, List<CoverageViewModel>> batchGetCoverageByResource(String resType,List<String> rids);
    
    /**   ===============================Helper=================================   **/
    
    /**
     * 根据条件获取覆盖范围
     * <p>Create Time: 2015年6月18日   </p>
     * <p>Create author: xiezy   </p>
     * @param targetType    覆盖范围的类型
     * @param target        覆盖目标的标识
     * @param strategy      资源操作类型
     * @param resource      资源的唯一标识
     * @return
     */
    public CoverageViewModel getCoverageByCondition(String targetType,String target,String strategy,String resource);
    
}
