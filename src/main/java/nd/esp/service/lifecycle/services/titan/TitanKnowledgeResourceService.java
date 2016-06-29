package nd.esp.service.lifecycle.services.titan;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.models.v06.KnowledgeModel;
import nd.esp.service.lifecycle.models.v06.KnowledgePathViewModel;

public interface TitanKnowledgeResourceService {

	KnowledgeModel create(KnowledgeModel r);
	void delete(String id);
	KnowledgeModel get(String id);

	KnowledgePathViewModel queryKnowledgePath(String startId, String endId, int minDepth, int maxDepth);
	KnowledgePathViewModel queryStartNode(String endId, int limit);

	KnowledgePathViewModel queryEndNode(String startId, int limit);
}
