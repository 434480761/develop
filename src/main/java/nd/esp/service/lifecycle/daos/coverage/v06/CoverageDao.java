package nd.esp.service.lifecycle.daos.coverage.v06;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nd.esp.service.lifecycle.repository.model.ResCoverage;
import nd.esp.service.lifecycle.support.DbName;
import nd.esp.service.lifecycle.vos.coverage.v06.CoverageViewModel;
/**
 * 资源覆盖范围的DAO
 * <p>Create Time: 2015年8月17日           </p>
 * @author xiezy
 */
public interface CoverageDao {
    
    /**
     * 批量获取多个资源所覆盖的范围   
     * <p>Create Time: 2015年8月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param resType       源资源类型
     * @param rids
     * @return
     */
    public Map<String, List<CoverageViewModel>> batchGetCoverageByResource(String resType, List<String> rids,DbName dbName);
    public List<ResCoverage> queryCoverageByResource(String resourceType,
			Set<String> uuids);
}
