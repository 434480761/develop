
/**   
 * @Title: KnowledgeRelationApiImpl.java 
 * @Package: com.nd.esp.repository.v02.impl 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月27日 下午7:02:11 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.KnowledgeRelationType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.KnowledgeRelation;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRelationRepository;
import nd.esp.service.lifecycle.repository.v02.KnowledgeRelationApi;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月27日 下午7:02:11 
 * @version V1.0
 */
@Repository("KnowledgeRelationApi")
public class KnowledgeRelationApiImpl extends BaseStoreApiImpl<KnowledgeRelation> implements KnowledgeRelationApi {

	@Autowired
	KnowledgeRelationRepository knowledgeRelationRepository;
	/**
	 * Description 
	 * @param relations
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.KnowledgeRelationApi#addBatch(java.util.List) 
	 */ 
		
	@Override
	public List<KnowledgeRelation> addBatch(List<KnowledgeRelation> relations)
			throws EspStoreException {
		for(KnowledgeRelation item : relations){
			add(item);
		}
		return relations;
	}

	/**
	 * Description 
	 * @param contextType
	 * @param contextObjectId
	 * @param sourceId
	 * @param relationType
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.KnowledgeRelationApi#queryTarget(java.lang.String, java.lang.String, java.lang.String, com.nd.esp.repository.common.KnowledgeRelationType) 
	 */ 
		
	@Override
	public List<KnowledgeRelation> queryTarget(String contextType,
			String contextObjectId, String sourceId,
			KnowledgeRelationType relationType) throws EspStoreException {
		KnowledgeRelation entity = new KnowledgeRelation();
		entity.setContextType(contextType);
		entity.setContextObject(contextObjectId);
		entity.setSource(sourceId);
		entity.setRelationType(String.valueOf(relationType.getValue()));
		return knowledgeRelationRepository.getAllByExample(entity);
	}

	/**
	 * Description 
	 * @param contextType
	 * @param contextObjectId
	 * @param sourceId
	 * @param relationType
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.KnowledgeRelationApi#deleteRelation(java.lang.String, java.lang.String, java.lang.String, com.nd.esp.repository.common.KnowledgeRelationType) 
	 */ 
		
	@Override
	public boolean deleteRelation(String contextType, String contextObjectId,
			String sourceId, KnowledgeRelationType relationType)
			throws EspStoreException {
		KnowledgeRelation entity = new KnowledgeRelation();
		entity.setContextType(contextType);
		entity.setContextObject(contextObjectId);
		entity.setSource(sourceId);
		entity.setRelationType(relationType.getMessage());
		knowledgeRelationRepository.deleteAllByExample(entity);
		return true;
	}

	/**
	 * Description 
	 * @param relation
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.KnowledgeRelationApi#addBatch(com.nd.esp.repository.model.KnowledgeRelation) 
	 */ 
		
	@Override
	public KnowledgeRelation addBatch(KnowledgeRelation relation)
			throws EspStoreException {
		return add(relation).getData();
	}

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.v02.impl.BaseStoreApiImpl#getResourceRepository() 
	 */ 
		
	@Override
	protected ResourceRepository<KnowledgeRelation> getResourceRepository() {
		return knowledgeRelationRepository;
	}


}
