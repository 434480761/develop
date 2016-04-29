/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.Knowledge;
import nd.esp.service.lifecycle.repository.sdk.KnowledgeRepository;
import nd.esp.service.lifecycle.repository.v02.KnowledgeApi;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */
@Repository("KnowledgeApi")
public class KnowledgeApiImpl extends BaseStoreApiImpl<Knowledge> implements KnowledgeApi {

    private static final Logger logger = LoggerFactory
			.getLogger(KnowledgeApiImpl.class);

	@Autowired
	KnowledgeRepository  knowledgeRepository;
	
	@Override
	protected ResourceRepository<Knowledge> getResourceRepository() {
		return knowledgeRepository;
	}

	/**
	 * Description 
	 * @param subjectId
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.KnowledgeApi#queryBySubject(java.lang.String) 
	 */ 
		
	@Override
	public List<Knowledge> queryBySubject(String subjectId)
			throws EspStoreException {
		Knowledge entity = new Knowledge();
		entity.setSubject(subjectId);
		return knowledgeRepository.getAllByExample(entity);
	}

	/**
	 * Description 
	 * @param subjectId
	 * @param request
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.KnowledgeApi#queryBySubject(java.lang.String, com.nd.esp.repository.index.QueryRequest) 
	 */ 
		
	@Override
	public QueryResponse<Knowledge> queryBySubject(String subjectId,
			QueryRequest request) throws EspStoreException {
		AdaptQueryRequest<Knowledge> queryRequest = new AdaptQueryRequest<Knowledge>(request);
		Knowledge bean = new Knowledge();
		bean.setSubject(subjectId);
		queryRequest.setParam(bean);
		return knowledgeRepository.searchByExample(queryRequest);
	}
}
