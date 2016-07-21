package nd.esp.service.lifecycle.repository.sdk;



  
import java.util.Collection;
import java.util.List;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月25日 下午1:35:42 
 * @version V1.0
 */ 
  	
public interface ResourceRelationRepository extends ResourceRepository<ResourceRelation>,
		JpaRepository<ResourceRelation, String> {
	
	@Query("SELECT DISTINCT(p.target) FROM ResourceRelation p where p.sourceUuid in (?1) AND p.resourceTargetType=?2 AND relationType = ?3")
	List<String> findTargetIdsBySourceIdsAndTargetType(List<String> sourceIds,String targetType,String relationType);
	
	
	@Query("SELECT DISTINCT(p.sourceUuid) FROM ResourceRelation p where p.target in (?1) AND p.resType=?2 AND relationType = ?3")
	List<String> findSourceIdsByTargetIdsAndResType(List<String> targetIds,String resType,String relationType);

	@Query("SELECT t1 FROM ResourceRelation t1 where t1.resType=?1 and t1.resourceTargetType=?2 and t1.enable=1 and EXISTS(select 1 from Education t2 where t1.sourceUuid=t2.identifier and t2.enable=1) and EXISTS(select 1 from Education t2 where t1.target=t2.identifier and t2.enable=1) and t1.target=?3 ")
	List<ResourceRelation> findByResTypeAndTargetTypeAndTargetId(String resType, String targetType, String targetId);

	@Query("SELECT t1 FROM ResourceRelation t1 where t1.resType=?1 and t1.resourceTargetType=?2 and t1.enable=1 and EXISTS(select 1 from Education t2 where t1.sourceUuid=t2.identifier and t2.enable=1) and EXISTS(select 1 from Education t2 where t1.target=t2.identifier and t2.enable=1) and t1.sourceUuid=?3 ")
	List<ResourceRelation> findByResTypeAndTargetTypeAndSourceId(String resType, String targetType, String sourceId);

	@Query("SELECT t FROM ResourceRelation t where t.target in (?1)")
	List<ResourceRelation> findByTarget(Collection<String> ids);
}