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
import nd.esp.service.lifecycle.repository.model.TeachingMaterial;
import nd.esp.service.lifecycle.repository.sdk.TeachingMaterialRepository;
import nd.esp.service.lifecycle.repository.v02.TeachingMaterialApi;

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
@Repository("TeachingMaterialApi")
public class TeachingMaterialApiImpl extends BaseStoreApiImpl<TeachingMaterial> implements TeachingMaterialApi {

	private static final Logger logger = LoggerFactory
			.getLogger(TeachingMaterialApiImpl.class);

	@Autowired
	TeachingMaterialRepository  teachingmaterialRepository;
	
	@Override
	protected ResourceRepository<TeachingMaterial> getResourceRepository() {
		return teachingmaterialRepository;
	}

	/**
	 * Description 
	 * @param phase
	 * @param grade
	 * @param subject
	 * @param edition
	 * @param request
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.TeachingMaterialApi#getListByCondition(java.lang.String, java.lang.String, java.lang.String, java.lang.String, com.nd.esp.repository.index.QueryRequest) 
	 */ 
		
	@Override
	public QueryResponse<TeachingMaterial> getListByCondition(String phase,
			String grade, String subject, String edition, QueryRequest request)
			throws EspStoreException {
	    
	    if (logger.isDebugEnabled()) {
            
	        logger.debug("getListByCondition param is [phase: {}] [grade: {}],[subject: {}],[edition: {}],[request:{}]",phase,grade,subject,edition,request);
	        
        }
		        
		AdaptQueryRequest<TeachingMaterial> adaptQueryRequest = new AdaptQueryRequest<TeachingMaterial>(request);
		TeachingMaterial teachingMaterial = new TeachingMaterial();
		teachingMaterial.setPhase(phase);
		teachingMaterial.setGrade(grade);
		teachingMaterial.setSubject(subject);
		teachingMaterial.setEdition(edition);
		adaptQueryRequest.setParam(teachingMaterial);
		return teachingmaterialRepository.searchByExample(adaptQueryRequest);
	}


}
