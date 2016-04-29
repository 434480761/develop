package nd.esp.service.lifecycle.repository;

import java.util.List;

import org.apache.solr.client.solrj.response.UpdateResponse;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.Searchable;


// TODO: Auto-generated Javadoc
/**
 *  
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @param <T> the generic type
 * @Description 
 * @date 2015年5月15日 下午6:09:38
 */ 
  	
public interface IndexRepository<T extends IndexMapper> extends Searchable<T> {
	
	/**
	 * Adds the index.
	 *
	 * @param bean the bean
	 * @return the update response
	 * @throws EspStoreException the esp store exception
	 */
	public UpdateResponse addIndex(Object bean) throws EspStoreException;
	
	/**
	 * Batch add index.
	 *
	 * @param beans the beans
	 * @return the update response
	 * @throws EspStoreException the esp store exception
	 */
	public UpdateResponse batchAddIndex(List<?> beans)
			throws EspStoreException;
	
	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return the update response
	 * @throws EspStoreException the esp store exception
	 */
	public UpdateResponse delete(String id)throws EspStoreException;
	
	
	/**
	 * Delete.
	 *
	 * @param id the id
	 * @return the update response
	 * @throws EspStoreException the esp store exception
	 */
	public UpdateResponse batchDelete(List<String> ids)throws EspStoreException;
}
