/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.InstructionalObjective;
import nd.esp.service.lifecycle.repository.sdk.InstructionalobjectiveRepository;
import nd.esp.service.lifecycle.repository.v02.InstructionalobjectiveApi;

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
@Repository("InstructionalobjectiveApi")
public class InstructionalobjectiveApiImpl extends BaseStoreApiImpl<InstructionalObjective> implements InstructionalobjectiveApi {

	private static final Logger logger = LoggerFactory
			.getLogger(InstructionalobjectiveApiImpl.class);

	@Autowired
	InstructionalobjectiveRepository  instructionalobjectiveRepository;
	
	@Override
	protected ResourceRepository<InstructionalObjective> getResourceRepository() {
		return instructionalobjectiveRepository;
	}

	/**
	 * Description 
	 * @param request
	 * @param lessonId
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.InstructionalobjectiveApi#search(com.nd.esp.repository.index.QueryRequest, java.lang.String) 
	 */ 
		
	@Override
	public QueryResponse<InstructionalObjective> search(QueryRequest request,
			String lessonId) throws EspStoreException {
		AdaptQueryRequest<InstructionalObjective> queryRequest = new AdaptQueryRequest<InstructionalObjective>(request);
		InstructionalObjective bean = new InstructionalObjective();
		bean.setLesson(lessonId);
		queryRequest.setParam(bean);
		return instructionalobjectiveRepository.searchByExample(queryRequest);
	}


}
