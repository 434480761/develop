package nd.esp.service.lifecycle.repository.sdk;



  
import java.util.List;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** 
 * 资源关系(习题库)
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月25日 下午1:35:42 
 * @version V1.0
 */ 
  	
public interface ResourceRelation4QuestionDBRepository extends ResourceRepository<ResourceRelation>,
		JpaRepository<ResourceRelation, String> {
	
	@Query("SELECT DISTINCT(p.target) FROM ResourceRelation p where p.sourceUuid in (?1) AND p.resourceTargetType=?2 AND relationType = ?3")
	List<String> findTargetIdsBySourceIdsAndTargetType(List<String> sourceIds,String targetType,String relationType);
	
	
	@Query("SELECT DISTINCT(p.sourceUuid) FROM ResourceRelation p where p.target in (?1) AND p.resType=?2 AND relationType = ?3")
	List<String> findSourceIdsByTargetIdsAndResType(List<String> targetIds,String resType,String relationType);

}