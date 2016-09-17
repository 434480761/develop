package nd.esp.service.lifecycle.repository.v02;

import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.CategoryData;

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
public interface CategoryDataApi extends StoreApi<CategoryData>,SearchApi<CategoryData> {
	
	/**
	 * Gets the detail.
	 *
	 * @param id the id
	 * @return the detail
	 * @throws EspStoreException the esp store exception
	 */
	public ReturnInfo<CategoryData> getDetailByNdCode(String ndCode) throws EspStoreException;
	
	/**
	 * Gets the detail.
	 *
	 * @param id the id
	 * @return the detail
	 * @throws EspStoreException the esp store exception
	 */
	public List<CategoryData> getListByNdCode(List<String> ndCodes) throws EspStoreException;
	
	/**
	 * Search.
	 *
	 * @param query the query
	 * @return the query response
	 * @throws EspStoreException the esp store exception
	 */
	public QueryResponse<CategoryData> search(QueryRequest query) throws EspStoreException;	
	
	
	/**
	 * Search.
	 *
	 * @param query the query
	 * @return the query response
	 * @throws EspStoreException the esp store exception
	 */
	public QueryResponse<CategoryData> search(QueryRequest query,String category,String parent) throws EspStoreException;	
}