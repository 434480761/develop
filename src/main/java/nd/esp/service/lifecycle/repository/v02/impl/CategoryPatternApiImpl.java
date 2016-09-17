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
import nd.esp.service.lifecycle.repository.model.CategoryPattern;
import nd.esp.service.lifecycle.repository.sdk.CategoryPatternRepository;
import nd.esp.service.lifecycle.repository.v02.CategoryPatternApi;
import nd.esp.service.lifecycle.repository.v02.ReturnInfo;

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
@Repository("CategoryPatternApi")
public class CategoryPatternApiImpl extends BaseStoreApiImpl<CategoryPattern> implements CategoryPatternApi {

	private static final Logger logger = LoggerFactory
			.getLogger(CategoryPatternApiImpl.class);

	@Autowired
	CategoryPatternRepository  categoryPatternRepository;
	
	@Override
	protected ResourceRepository<CategoryPattern> getResourceRepository() {
		return categoryPatternRepository;
	}

	/**
	 * Description 
	 * @param patternName
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.CategoryPatternApi#loadCategoryPatternByPatternName(java.lang.String) 
	 */ 
		
	@Override
	public ReturnInfo<CategoryPattern> loadCategoryPatternByPatternName(
			String patternName) throws EspStoreException {
		ReturnInfo<CategoryPattern> rt = new ReturnInfo<CategoryPattern>();
		
		CategoryPattern param = new CategoryPattern();
		param.setPatternName(patternName);
		CategoryPattern data = categoryPatternRepository.getByExample(param);
		rt.setData(data);
		rt.setCode(1);
		return rt;
	}


}
