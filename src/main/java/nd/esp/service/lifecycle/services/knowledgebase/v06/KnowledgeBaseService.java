package nd.esp.service.lifecycle.services.knowledgebase.v06;

import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;

public interface KnowledgeBaseService {
	/**
	 * 创建知识库
	 * @param kbm
	 * @return
	 */
	public KnowledgeBaseModel createKnowledgeBase(KnowledgeBaseModel kbm);
	
	/**
	 * 创建知识库
	 * @param kbm
	 * @return
	 */
	public List<KnowledgeBaseModel> batchCreateKnowledgeBase(KnowledgeBaseModel kbm);
	
	/**
	 * 根据知识子结构查找知识库
	 * @param kpid
	 * @return
	 */
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByKpid(String kpid);
	
	/**
	 * 根据知识类型、知识子结构、知识点的title查找知识库
	 * @param kcid
	 * @param kpid
	 * @param knTitle
	 * @return
	 */
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByCond(String kcid,String kpid,String knTitle);
	
	public void batchAddKbWhenKpAdd(final String kcCode, final String kpid);
	
	public List<Map<String,Object>> queryInstructionalObjectiveByCond(String kbid,String ocid);
	
	public List<Map<String,Object>> queryInstructionalObjectiveByKid(String kcCode,String kpId);
	
	/**
	 * 根据知识类型、知识点的title查找知识库
	 * @param kcCode
	 * @param title
	 * @return
	 */
	public List<KnowledgeBaseModel> queryKnowledgeBaseListByKcCode(String kcCode,String title);
}
