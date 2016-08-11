package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;

import java.util.List;

public interface TitanRelationRepository extends TitanEspRepository<ResourceRelation>{

	void deleteRelationSoft(String primaryCategory, String identifier);

	boolean delete(String identifier);

	public void batchAdd4Import(List<ResourceRelation> resourceRelations);

	public void batchUpdate4Import(List<ResourceRelation> resourceRelations);

}
