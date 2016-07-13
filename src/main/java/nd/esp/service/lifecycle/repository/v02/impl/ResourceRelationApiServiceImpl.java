
/**   
 * @Title: ResourceRelationApiServiceImpl.java 
 * @Package: com.nd.esp.repository.v02.impl 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月25日 下午1:36:15 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.v02.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.common.StoreCfg;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Item;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.model.ResourceRelationStatistics;
import nd.esp.service.lifecycle.repository.model.ResourceRelationStatisticsRequest;
import nd.esp.service.lifecycle.repository.model.ResourceRelationStatisticsRequest2;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.HibernateParter;
import nd.esp.service.lifecycle.repository.v02.ResourceRelationApiService;
import nd.esp.service.lifecycle.repository.v02.ReturnInfo;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月25日 下午1:36:15 
 * @version V1.0
 */
@Repository("ResourceRelationApiService")
public class ResourceRelationApiServiceImpl extends BaseStoreApiImpl<ResourceRelation> implements ResourceRelationApiService{
	private static final Logger logger = LoggerFactory
			.getLogger(ResourceRelationApiServiceImpl.class);

	@Autowired
	ResourceRelationRepository  resourceRelationRepository;
	@PersistenceContext(unitName="entityManagerFactory")
	private  EntityManager entityManager;
	/**
	 * Description 
	 * @param identifier
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.ResourceRelationApiService#deleteBatch(java.util.List) 
	 */ 
		
	@Override
	public boolean deleteBatch(List<String> identifier)
			throws EspStoreException {
		for(String id : identifier){
			resourceRelationRepository.del(id);
		}	
		return true;
	}

	/**
	 * Description 
	 * @param relationStatisticsRequest
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.ResourceRelationApiService#statistics(com.nd.esp.repository.model.ResourceRelationStatisticsRequest) 
	 */ 
		
	@Override
	public List<ResourceRelationStatistics> statistics(
			ResourceRelationStatisticsRequest relationStatisticsRequest)
			throws EspStoreException {
//		SolrServer server = resourceRelationRepository.getSolrServer();
//		
//		StringBuffer sb = new StringBuffer("index_type_int:"+IndexSourceType.ResourceRelationType.getType()+" AND resType:"+relationStatisticsRequest.getGroup());
//		
//		sb.append(doListAppend("sourceUuid",relationStatisticsRequest.getSources())).append(doListAppend("categorys",relationStatisticsRequest.getCategorys())).append(doListAppend("resourceTargetType",relationStatisticsRequest.getTargetType()));
//		
//		SolrQuery query = new SolrQuery(sb.toString());
//		query.setFacet(true);
//		query.add("facet.pivot", "sourceUuid,resourceTargetType");
//		org.apache.solr.client.solrj.response.QueryResponse response = null;
//		try {
//			response = server.query(query);
//		} catch (SolrServerException e) {
//		    
//		    if (logger.isErrorEnabled()) {
//                
//		        logger.error("统计资源异常:{} {}", e.getCause(),e);
//            }
//		    
//			        
//			throw new EspStoreException("统计资源异常:"+e.getCause());
//		}
//		 
//		NamedList<List<PivotField>> namedList = response.getFacetPivot();
//        List<ResourceRelationStatistics> resourceRelationStatisticss = Lists.newArrayList();
//        if(namedList != null){
//        	List<PivotField> pivotList = null;
//        	 for(int i=0;i<namedList.size();i++){
//        		 //ResourceRelationStatistics relationStatistics = null;
//                 pivotList = namedList.getVal(i);
//                 if(pivotList!=null){
//                	 ResourceRelationStatistics relationStatistics = null;
//                	 for(PivotField pivot:pivotList){
//                		 relationStatistics = new ResourceRelationStatistics();
//                		 relationStatistics.setIdentifier((String)pivot.getValue());
//                		 relationStatistics.setResourceType(relationStatisticsRequest.getGroup());
//                		 relationStatistics.setCount(pivot.getCount());
//                		 List<PivotField>  itemPivots  = pivot.getPivot();
//                		 List<Item> items = Lists.newArrayList();
//                		 if(itemPivots!=null){
//                			 Item item = null;
//	                		 for(PivotField pivotItem:itemPivots){
//	                			 item = new Item();
//	                			 item.setCount(pivotItem.getCount());
//	                			 item.setResourceType((String)pivotItem.getValue());
//	                			 items.add(item);
//	                		 }
//                		 }
//                		 relationStatistics.setItems(items);
//                		 resourceRelationStatisticss.add(relationStatistics);
//                	 }
//                 }
//        	 }
//        }
//		return resourceRelationStatisticss;
		return null;
	}

	/**
	 * Description 
	 * @param uuid
	 * @param target
	 * @param relation
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.ResourceRelationApiService#get(java.lang.String, java.lang.String, java.lang.String) 
	 */ 
		
	@Override
	public ReturnInfo<ResourceRelation> get(String uuid, String target,
			String relation) throws EspStoreException {
		ReturnInfo<ResourceRelation> rt = new ReturnInfo<ResourceRelation>();
		ResourceRelation entity = new ResourceRelation();
		entity.setSourceUuid(uuid);
		entity.setTarget(target);
		entity.setRelationType(relation);
		ResourceRelation resourceRelation =  resourceRelationRepository.getByExample(entity);
		rt.setData(resourceRelation);
		return rt;
	}

	/**
	 * Description 
	 * @param example
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.v02.ResourceRelationApiService#getByExample(com.nd.esp.repository.model.ResourceRelation) 
	 */ 
		
	@Override
	public List<ResourceRelation> getByExample(ResourceRelation example)
			throws EspStoreException {
		return resourceRelationRepository.getAllByExample(example);
	}

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.v02.impl.BaseStoreApiImpl#getResourceRepository() 
	 */ 
		
	@Override
	protected ResourceRepository<ResourceRelation> getResourceRepository() {
		return resourceRelationRepository;
	}
	
	private String doListAppend(String paramName,List<String> value){
		StringBuffer sb = new StringBuffer();
		if(value!=null &&value.size()!=0){
			for(int i=0;i<value.size();i++){
				if(!StringUtils.isEmpty(value.get(i).trim())){
					if(i == 0 ){
						sb.append(" AND "+paramName+":(");
					}
					sb.append(i==0?""+value.get(i):" "+value.get(i));
					if(i == value.size()-1){
						sb.append(") ");
					}
				}
			}
		}
		return sb.toString();
	}
	
	static Cache<String, List<Item>> cache = CacheBuilder.newBuilder().expireAfterWrite(StoreCfg.getInstance().getCacheExpireTime(), TimeUnit.MINUTES).maximumSize(10000).build();
	
	@Override
	public List<Item> statisticsByTarget(final ResourceRelationStatisticsRequest2 relationStatisticsRequest2) throws EspStoreException{
		final Map<String,Object> param = HibernateParter.getParam(relationStatisticsRequest2.getEntity());
		
		StringBuffer sb = new StringBuffer();
		
		for(Entry<String, Object> item : param.entrySet()){
			sb.append(" AND ").append("c."+item.getKey()).append("=:").append(item.getKey());
		}
		EspEntity entity = relationStatisticsRequest2.getEntity();
		
		String tableName = HibernateParter.getTableName(entity.getClass());
		
		if(StringUtils.isEmpty(tableName)){
			throw new EspStoreException("无法获取表名异常!");
		}
		
		String cacheKey = tableName + "-" + param.entrySet() + "-" + relationStatisticsRequest2.getTargetType();
		
		if (logger.isInfoEnabled()) {
            
		    logger.info("cache key is {}",cacheKey);
		    
        }
		
		final String sql = "select count(*),temp.resource_target_type from (select DISTINCT(target),resource_target_type from resource_relations r,"+tableName+" c WHERE r.source_uuid = c.identifier "+sb.toString()+") as temp group by temp.resource_target_type";
		
		/*		logger.info("sql is:"+sql);
		
		javax.persistence.Query query = entityManager.createNativeQuery(sql);
		i = 1;
		for(Entry<String, Object> item : param.entrySet()){
			query.setParameter(item.getKey(), item.getValue());
		}
		List<?> list =query.getResultList();
		
		for(Object object:list){
		
			Item item = new Item();
			Object [] temp = (Object[])object;
			BigInteger count = (BigInteger) temp[0];
			String name = (String) temp[1];
			        
			        
			        logger.debug("count:"+count+", name"+name+"\n");
			        
			item.setCount(count.intValue());
			item.setResourceType(name);
			
			//如果指定返回类型则返回指定类型，否则全部返回
			if(relationStatisticsRequest2.getTargetType()!=null&&relationStatisticsRequest2.getTargetType().size()>0){
				boolean isRet = false;
				for(String ret: relationStatisticsRequest2.getTargetType()){
					if(ret.equalsIgnoreCase(item.getResourceType())){
						isRet = true;
						break;
					}
				}
				
				if(isRet){
					reItems.add(item);
				}
			}else{
				reItems.add(item);
			}
		}
		return reItems;*/
		try {
			return cache.get(cacheKey,new Callable<List<Item>>() {

				@Override
				public List<Item> call() throws Exception {
					List<Item> reItems =Lists.newArrayList();
					
					if (logger.isInfoEnabled()){
					    
					    logger.info("sql is:{}", sql);
					    
					}
					
					javax.persistence.Query query = entityManager.createNativeQuery(sql);
					
					for(Entry<String, Object> item : param.entrySet()){
						query.setParameter(item.getKey(), item.getValue());
					}
					List<?> list =query.getResultList();
					
					for(Object object:list){
					
						Item item = new Item();
						Object [] temp = (Object[])object;
						BigInteger count = (BigInteger) temp[0];
						String name = (String) temp[1];
						
						if (logger.isDebugEnabled()) {
                            
						    logger.debug("count:{}, name{}\n", count, name);
						    
                        }
						        
						item.setCount(count.intValue());
						item.setResourceType(name);
						
						//如果指定返回类型则返回指定类型，否则全部返回
						if(relationStatisticsRequest2.getTargetType()!=null&&relationStatisticsRequest2.getTargetType().size()>0){
							boolean isRet = false;
							for(String ret: relationStatisticsRequest2.getTargetType()){
								if(ret.equalsIgnoreCase(item.getResourceType())){
									isRet = true;
									break;
								}
							}
							
							if(isRet){
								reItems.add(item);
							}
						}else{
							reItems.add(item);
						}
					}
					return reItems;
				}
			});
		} catch (ExecutionException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("统计数据异常！{}",e);
		        
            }
			        
			new EspStoreException(e);
		}
		return null;
	}

	/**
	 * 根据关联关系和子id获取ResourceRelation列表
	 *
	 * @param resType（chapters、lessons、instructionalobjectives）
	 * @param targetType（chapters、lessons、instructionalobjectives）
	 * @param targetId
	 * @return
	 * @throws EspStoreException
	 */
	@Override
	public List<ResourceRelation> getByResTypeAndTargetTypeAndTargetId(String resType, String targetType,
																	   String targetId) throws EspStoreException {
		List<ResourceRelation> resourceRelationList = resourceRelationRepository.findByResTypeAndTargetTypeAndTargetId(resType, targetType, targetId);
		return resourceRelationList;
	}

	/**
	 * 根据关联关系和父id获取ResourceRelation列表
	 *
	 * @param resType（chapters、lessons、instructionalobjectives）
	 * @param targetType（chapters、lessons、instructionalobjectives）
	 * @param sourceId
	 * @return
	 * @throws EspStoreException
	 */

	@Override
	public List<ResourceRelation> getByResTypeAndTargetTypeAndSourceId(String resType, String targetType,
																	   String sourceId) throws EspStoreException {
		List<ResourceRelation> resourceRelationList = resourceRelationRepository.findByResTypeAndTargetTypeAndSourceId(resType, targetType, sourceId);
		return resourceRelationList;
	}

}
