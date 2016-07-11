package nd.esp.service.lifecycle.daos.titan.inter;

import nd.esp.service.lifecycle.repository.model.ResourceCategory;

public interface TitanCategoryRepository extends TitanEspRepository<ResourceCategory>{

//	ResourceCategory add(ResourceCategory resourceCategory);
//	ResourceCategory update(ResourceCategory resourceCategory);
	void deleteAll(String primaryCategory, String identifier);
//	List<ResourceCategory> batchAdd(List<ResourceCategory> resourceCategories);
}
