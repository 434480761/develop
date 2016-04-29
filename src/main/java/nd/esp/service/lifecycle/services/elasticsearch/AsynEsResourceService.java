package nd.esp.service.lifecycle.services.elasticsearch;

import java.util.Set;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;

public interface AsynEsResourceService {
	void asynAdd(Resource resource);
	void asynBatchAdd(Set<Resource> resources);
}
