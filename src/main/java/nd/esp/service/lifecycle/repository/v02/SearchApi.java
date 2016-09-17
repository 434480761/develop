/**   
 * @Title: SearchApi.java 
 * @Package: com.nd.esp.store.sdk 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年4月23日 下午7:19:26 
 * @version 1.3.1 
 */

package nd.esp.service.lifecycle.repository.v02;

import nd.esp.service.lifecycle.repository.IndexMapper;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;

/**
 * @Description
 * @author Rainy(yang.lin)
 * @date 2015年4月23日 下午7:19:26
 * @version V1.0
 */

public interface SearchApi<T extends IndexMapper> {
	/**
	 * 资源检索
	 * 
	 * @Title: search
	 * @Description: TODO
	 * @param @param queryRequest
	 * @param @return
	 * @return SearchResponse<Asset>
	 * @throws
	 */
	public QueryResponse<T> searchByExample(AdaptQueryRequest<T> queryRequest)
			throws EspStoreException;

	/**
	 * 资源检索
	 * 
	 * @Title: search
	 * @Description: TODO
	 * @param @param queryRequest
	 * @param @return
	 * @return SearchResponse<Asset>
	 * @throws
	 */
	public QueryResponse<T> search(QueryRequest queryRequest)
			throws EspStoreException;

}
