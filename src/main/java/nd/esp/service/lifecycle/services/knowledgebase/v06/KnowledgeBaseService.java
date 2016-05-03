package nd.esp.service.lifecycle.services.knowledgebase.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.v06.KnowledgeBaseModel;

public interface KnowledgeBaseService {
	/**
	 * 创建知识库
	 * @param kbm
	 * @return
	 */
	public KnowledgeBaseModel createKnowledgeBase(KnowledgeBaseModel kbm);
	
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
}
