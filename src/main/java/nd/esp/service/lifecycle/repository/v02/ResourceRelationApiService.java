package nd.esp.service.lifecycle.repository.v02;


import java.util.Collection;
import java.util.List;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Item;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.ResourceRelationStatistics;
import nd.esp.service.lifecycle.repository.model.ResourceRelationStatisticsRequest;
import nd.esp.service.lifecycle.repository.model.ResourceRelationStatisticsRequest2;

// TODO: Auto-generated Javadoc
/**
 * The Interface ResourceRelationApiService.
 */
public interface ResourceRelationApiService extends SearchApi<ResourceRelation>{
	
	/**
	 * Adds the.
	 *
	 * @param bean the bean
	 * @return the return info
	 * @throws EspStoreException the esp store exception
	 */
	public ReturnInfo<ResourceRelation>  add(ResourceRelation bean) throws EspStoreException;
	
	/**
	 * Update.
	 *
	 * @param bean the bean
	 * @return the return info
	 * @throws EspStoreException the esp store exception
	 */
	public ReturnInfo<ResourceRelation> update(ResourceRelation bean) throws EspStoreException;

	/**
	 * Delete.
	 *
	 * @param identifier the identifier
	 * @return true, if successful
	 * @throws EspStoreException the esp store exception
	 */
	public boolean delete(String identifier) throws EspStoreException;
	
	/**
	 * Delete.
	 *
	 * @param identifier the identifier
	 * @return true, if successful
	 * @throws EspStoreException the esp store exception
	 */
	public boolean deleteBatch(List<String> identifier) throws EspStoreException;

	/**
	 * Gets the list.
	 *
	 * @param ids the ids
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	public List<ResourceRelation> getList(List<String> ids) throws EspStoreException;

	/**
	 * Gets the detail.
	 *
	 * @param id the id
	 * @return the detail
	 * @throws EspStoreException the esp store exception
	 */
	public ReturnInfo<ResourceRelation> getDetail(String id) throws EspStoreException;
	
	/**
	 * Statistics.
	 *
	 * @param relationStatisticsRequest the relation statistics request
	 * @return the list
	 * @throws EspStoreException the esp store exception
	 */
	public List<ResourceRelationStatistics> statistics(ResourceRelationStatisticsRequest relationStatisticsRequest) throws EspStoreException;

	/**
	 * Gets the.
	 *
	 * @param uuid the uuid
	 * @param target the target
	 * @param relation the relation
	 * @return the return info
	 * @throws EspStoreException the esp store exception
	 */
	public ReturnInfo<ResourceRelation> get(String uuid, String target,
			String relation) throws EspStoreException;
	
	
	public List<ResourceRelation> getByExample(ResourceRelation example) throws EspStoreException;

	 
	/** 
	 * @Description 
	 * @author Rainy(yang.lin)
	 * @param id
	 * @return  
	 * @throws EspStoreException 
	 */
	  	
	List<Item> statisticsByTarget(ResourceRelationStatisticsRequest2 relationStatisticsRequest2) throws EspStoreException;

	public List<ResourceRelation> getByResTypeAndTargetTypeAndTargetId(String resType, String targetType,
																	   String targetId) throws EspStoreException;

	public List<ResourceRelation> getByResTypeAndTargetTypeAndSourceId(String resType, String targetType,
																	   String sourceId) throws EspStoreException;

	public List<ResourceRelation> getByTargetId(Collection<String> ids);
}
