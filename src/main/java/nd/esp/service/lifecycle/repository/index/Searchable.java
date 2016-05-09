
/**   
 * @Title: Searchable.java 
 * @Package: com.nd.esp.repository.sdk 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月26日 上午10:07:44 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.index;

import nd.esp.service.lifecycle.repository.IndexMapper;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月26日 上午10:07:44 
 * @version V1.0
 */

public interface Searchable<T extends IndexMapper> {
	/**
	 * Search by example.
	 * 
	 * Example：
	 * AdaptQueryRequest<Shop> queryRequest = new AdaptQueryRequest<Shop>(); 
	 * Shop param = new Shop();
	 * param.setTitle("123"); 表示查询标题中包含123的过滤
	 * queryRequest.setParam(param);
	 * System.out.println(shopRepository.searchByExample(queryRequest));
	 * 
	 * @param queryRequest
	 *            the query request
	 * @return the query response
	 * @throws EspStoreException
	 *             the esp store exception
	 */
	public  QueryResponse<T> searchByExample(AdaptQueryRequest<T> queryRequest) throws EspStoreException;
	
	/**
	 * Search.
	 *
	 * @param queryRequest the query request
	 * @return the query response
	 * @throws EspStoreException the esp store exception
	 */
	public  QueryResponse<T> search(QueryRequest queryRequest) throws EspStoreException;
	
}
