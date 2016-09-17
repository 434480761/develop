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

import com.google.common.collect.Lists;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.ds.ComparsionOperator;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.LogicalOperator;
import nd.esp.service.lifecycle.repository.ds.ValueUtils;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.v02.CategoryDataApi;
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
@Repository("CategoryDataApi")
public class CategoryDataApiImpl extends BaseStoreApiImpl<CategoryData> implements CategoryDataApi {

	private static final Logger logger = LoggerFactory
			.getLogger(CategoryDataApiImpl.class);

	@Autowired
	CategoryDataRepository  categoryDataRepository;
	
	@Override
	protected ResourceRepository<CategoryData> getResourceRepository() {
		return categoryDataRepository;
	}

	/**
	 * Description 
	 * @param ndCode
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.CategoryDataApi#getDetailByNdCode(java.lang.String) 
	 */ 
		
	@Override
	public ReturnInfo<CategoryData> getDetailByNdCode(String ndCode)
			throws EspStoreException {
	    
	    if (logger.isDebugEnabled()) {
            
	        logger.debug("getDetailByNdCode : ndcode is :{}", ndCode);
	        
        }
		
		ReturnInfo<CategoryData> rt = new ReturnInfo<CategoryData>();
		Item<String> item = new Item<>();
		item.setKey("ndCode");
		item.setComparsionOperator(ComparsionOperator.EQ);
		item.setLogicalOperator(LogicalOperator.AND);
		item.setValue(ValueUtils.newValue(ndCode));
		List<Item<? extends Object>> items = new ArrayList<>();
		items.add(item);
		
		CategoryData category = categoryDataRepository.findOneByItems(items);
		rt.setData(category);
		rt.setCode(1);
		return rt;
	}

	/**
	 * Description 
	 * @param ndCodes
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.CategoryDataApi#getListByNdCode(java.util.List) 
	 */ 
		
	@Override
	public List<CategoryData> getListByNdCode(List<String> ndCodes)
			throws EspStoreException {
		List<Object> temp = Lists.newArrayList();
		for(String item : ndCodes){
			temp.add(item);
		}
		return categoryDataRepository.getListWhereInCondition("ndCode",temp);
	}

	/**
	 * Description 
	 * @param query
	 * @param category
	 * @param parent
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.CategoryDataApi#search(com.nd.esp.repository.index.QueryRequest, java.lang.String, java.lang.String) 
	 */ 
		
	@Override
	public QueryResponse<CategoryData> search(QueryRequest query,
			String category, String parent) throws EspStoreException {
		AdaptQueryRequest<CategoryData> adaptQueryRequest = new AdaptQueryRequest<>(query);
		CategoryData categoryData = new CategoryData();
		categoryData.setCategory(category);
		categoryData.setParent(parent);
		adaptQueryRequest.setParam(categoryData);
		return categoryDataRepository.searchByExample(adaptQueryRequest);
	}


}
