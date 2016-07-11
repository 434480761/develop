package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.ResourceRelation;

public interface TitanRelationRepository extends TitanEspRepository<ResourceRelation>{

	void deleteRelationSoft(String primaryCategory, String identifier);

	boolean delete(String identifier);

}
