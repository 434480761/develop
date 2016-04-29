package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.common.KnowledgeRelationType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.KnowledgeRelation;

/**
 * @title 知识点关联关系接口
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @create 2015年4月1日 下午8:58:25
 */
public interface KnowledgeRelationApi extends StoreApi<KnowledgeRelation>,SearchApi<KnowledgeRelation>{
	
	
	public boolean delete(String id) throws EspStoreException;
	
	
	
	/**
	 * @desc 批量增加知识点关联关系
	 * @param relations
	 * @return
	 * @author liuwx
	 */
	public List<KnowledgeRelation>addBatch(List<KnowledgeRelation> relations) throws EspStoreException ;
	
	
	/**
	 * @desc  通过source知识点查询关联的
	 * @param contextType 上下文类型
	 * @param contextObjectId 上下文对象
	 * @param sourceId 源教学目标Id
	 * @param relationType 知识点关系
	 * @return
	 * @author liuwx
	 */
	public List<KnowledgeRelation>queryTarget(String contextType,String contextObjectId,String sourceId,KnowledgeRelationType relationType ) throws EspStoreException ;
	
	
	/**
	 * @desc  通过source删除关联关系
	 * @param contextType 上下文类型
	 * @param contextObjectId 上下文对象
	 * @param sourceId 源教学目标Id
	 * @param relationType 知识点关系
	 * @return
	 * @author liuwx
	 */
	public boolean deleteRelation(String contextType,String contextObjectId,String sourceId,KnowledgeRelationType relationType ) throws EspStoreException;
	
	
	/**
	 * @desc 增加知识点关联关系
	 * @param relations
	 * @return
	 * @author liuwx
	 */
	public KnowledgeRelation addBatch(KnowledgeRelation relation) throws EspStoreException;
	
	
	

}
