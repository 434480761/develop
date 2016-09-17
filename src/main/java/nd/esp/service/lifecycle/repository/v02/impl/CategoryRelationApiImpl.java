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
import nd.esp.service.lifecycle.repository.model.CategoryRelation;
import nd.esp.service.lifecycle.repository.sdk.CategoryRelationRepository;
import nd.esp.service.lifecycle.repository.v02.CategoryRelationApi;

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
@Repository("CategoryRelationApi")
public class CategoryRelationApiImpl extends BaseStoreApiImpl<CategoryRelation> implements CategoryRelationApi {

    private static final Logger logger = LoggerFactory
			.getLogger(CategoryRelationApiImpl.class);

	@Autowired
	CategoryRelationRepository  categoryRelationRepository;
	
	@Override
	protected ResourceRepository<CategoryRelation> getResourceRepository() {
		return categoryRelationRepository;
	}

	/**
	 * Description 
	 * @param condition
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.CategoryRelationApi#getByCondition(com.nd.esp.repository.model.CategoryRelation) 
	 */ 
		
	@Override
	public List<CategoryRelation> getByCondition(CategoryRelation condition)
			throws EspStoreException {
		return categoryRelationRepository.getAllByExample(condition);
	}


}
