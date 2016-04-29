/**
 * 
 */
package nd.esp.service.lifecycle.repository.v02.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Category;
import nd.esp.service.lifecycle.repository.sdk.CategoryRepository;
import nd.esp.service.lifecycle.repository.v02.CategoryApi;
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
@Repository("CategoryApi")
public class CategoryApiImpl extends BaseStoreApiImpl<Category> implements CategoryApi {

	private static final Logger logger = LoggerFactory
			.getLogger(CategoryApiImpl.class);

	@Autowired
	CategoryRepository  categoryRepository;
	
	@Override
	protected ResourceRepository<Category> getResourceRepository() {
		return categoryRepository;
	}

	/**
	 * Description 
	 * @param ndCode
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.CategoryApi#getDetailByNdCode(java.lang.String) 
	 */ 
		
	@Override
	public ReturnInfo<Category> getDetailByNdCode(String ndCode)
			throws EspStoreException {
	    
	    if (logger.isDebugEnabled()) {
            
	        logger.debug("getDetailByNdCode : ndcode is :{}", ndCode);
	        
        }
		
		ReturnInfo<Category> rt = new ReturnInfo<Category>();
		Item<String> item = new Item<>();
		item.setKey("ndCode");
		item.setComparsionOperator(ComparsionOperator.EQ);
		item.setLogicalOperator(LogicalOperator.AND);
		item.setValue(ValueUtils.newValue(ndCode));
		List<Item<? extends Object>> items = new ArrayList<>();
		items.add(item);
		
		Category category = categoryRepository.findOneByItems(items);
		rt.setData(category);
		rt.setCode(1);
		return rt;
	}


}
