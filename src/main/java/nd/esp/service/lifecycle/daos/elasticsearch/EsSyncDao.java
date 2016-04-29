package nd.esp.service.lifecycle.daos.elasticsearch;

import java.util.Set;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;



public interface EsSyncDao {
	
	void beforeUpdate(Resource resource)throws EspStoreException;
	void batchBeforeUpdate(Set<Resource> resource)throws EspStoreException;
	void afterUpdate(Resource resource)throws EspStoreException;
	void batchAfterUpdate(Set<Resource> resoruce)throws EspStoreException;
	void beforeDelete(Resource resource)throws EspStoreException;
	void batchBeforeDelete(Set<Resource> resourceSet)throws EspStoreException;
	void afterDelete(Resource resource)throws EspStoreException;
	void batchAfterDelete(Set<Resource> resourceSet)throws EspStoreException;

}
