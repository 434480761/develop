package nd.esp.service.lifecycle.daos.knowledgebase.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;
import nd.esp.service.lifecycle.repository.model.KnowledgeBase;

public interface KnowledgeBaseDao {
	public KnowledgeBaseModel createKnowledgeBase(KnowledgeBaseModel kb);
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByKpid(String kpid);
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByCond(String kcid,String kpid, String knTitle);
	
	public List<String> queryKnowledgeByKcCode(String kcCode);
	public void batchCreateKnowledgeBase(List<KnowledgeBase> kbList);
}
