package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.CategoryRelation;

// TODO: Auto-generated Javadoc
/**
 * 项目名字:nd esp<br>
 * 类描述:<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月4日<br>
 * 修改人:<br>
 * 修改时间:2015年2月4日<br>
 * 修改备注:<br>.
 *
 * @version 0.2<br>
 */
public interface CategoryRelationApi extends StoreApi<CategoryRelation>,SearchApi<CategoryRelation> {
	/**
	 * Search.
	 *
	 * @param query the query
	 * @return the query response
	 * @throws EspStoreException the esp store exception
	 */
	public QueryResponse<CategoryRelation> search(QueryRequest query) throws EspStoreException;	
	
	/**
	 * Gets the by condition.({pattern_path},{enable=true}&{levelParent=uuid})
	 *
	 * @param condition the condition
	 * @return the by condition
	 * @throws EspStoreException the esp store exception
	 */
	public List<CategoryRelation>  getByCondition(CategoryRelation condition)throws EspStoreException;
	
}