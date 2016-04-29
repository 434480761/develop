package nd.esp.service.lifecycle.repository.sdk.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.client.solrj.response.AnalysisResponseBase.AnalysisPhase;
import org.apache.solr.client.solrj.response.AnalysisResponseBase.TokenInfo;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse;
import org.apache.solr.client.solrj.response.FieldAnalysisResponse.Analysis;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.IndexMapper;
import nd.esp.service.lifecycle.repository.IndexRepository;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.Hits;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.index.ResponseHeader;
import nd.esp.service.lifecycle.repository.index.SolrServerFactory;


/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月15日 下午6:10:11 
 * @version V1.0
 * @param <T>
 */ 
  	
public  class IndexRepositoryImpl<T extends EspEntity>
		implements IndexRepository<T> {
	/** The Constant DEBUG. */
	public static boolean DEBUG = false;
	
	/**
	 * Logging
	 */
	private static Logger logger = LoggerFactory
			.getLogger(IndexRepositoryImpl.class);
	
	/** The repository. */
	private ResourceRepository<T> repository;
	
	private Class<T> domainClass;
	

	public IndexRepositoryImpl(ResourceRepository<T> repository,
			Class<T> domainClass) {
		super();
		this.repository = repository;
		this.domainClass = domainClass;
	}

	/**
	 * Description 
	 * @param queryRequest
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.IndexRepository#searchByExample(com.nd.esp.repository.index.AdaptQueryRequest) 
	 */ 
		
	@Override
	public QueryResponse<T> searchByExample(AdaptQueryRequest<T> queryRequest)
			throws EspStoreException {
		QueryResponse<T> response = new QueryResponse<T>();

		Hits<T> hits = new Hits<T>();

		hits.setTotal(0);

		ResponseHeader responseHeader = new ResponseHeader();

		SolrServer server = SolrServerFactory.getSingleton().getDefaulSolrServer();
		
		T bean = null ;
		if(queryRequest.getParam() == null){
			try {
				bean = domainClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("反射创建实力出错！{}",e);
			        
                }
				        
				throw new EspStoreException(e);
			}
			queryRequest.setParam(bean);
		}
		
		
		SolrParams params = null;
		

		org.apache.solr.client.solrj.response.QueryResponse sds = null;
		try {
			Map<String,String> param = queryRequest.getQueryMap(!StringUtils.isEmpty(queryRequest.getKeyword())?anaylize(server, ClientUtils.escapeQueryChars(queryRequest.getKeyword())):"");
			
			if (logger.isDebugEnabled()) {
                
			    logger.debug("index param is :{}", param);
			    
            }
			
			params = new MapSolrParams(param);
			
			sds = server.query(params);
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e);
		        
            }
			        
			throw new EspStoreException(e);
		} catch (IOException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e);
		        
            }
			        
			throw new EspStoreException(e);
		}
		
			SolrDocumentList scs = sds.getResults();
			
			// 额外查询(在第一次查询条件没命中的情况下尝试对参数前后加模糊匹配查询,只有在debug为true时才开启)
			try {
				scs = extraSearch(server, scs, queryRequest);
			} catch (SolrServerException e) {
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("额外查询异常{}", e);
			        
                }
				        
				throw new EspStoreException(e);
			}

			List<String> ids = Lists.newArrayList();

			for (SolrDocument item : scs) {
				if (item.get("identifier") != null)
					ids.add((String) item.get("identifier"));
			}

			List<T> result = Lists.newArrayList();
			
			if(ids.size()!=0){
				result = repository.getAll(ids);
			}

			if (result.size() != ids.size()) {
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("数据库中数据和索引数据不一致!");
			        
                }
				        
			}

			if (logger.isDebugEnabled()) {
                
			    logger.debug("search found num is :{}", scs.getNumFound());
			    
            }
			        
			hits.setTotal(scs.getNumFound());
			hits.setDocs(result);
		
			responseHeader.setQTime(sds.getQTime());
			response.setResponseHeader(responseHeader);
			response.setHits(hits);
			response.setResponseHeader(responseHeader);
			return response;
	}

	/**
	 * Description 
	 * @param queryRequest
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.IndexRepository#search(com.nd.esp.repository.index.QueryRequest) 
	 */ 
		
	@Override
	public QueryResponse<T> search(QueryRequest queryRequest)
			throws EspStoreException {
		QueryResponse<T> response = new QueryResponse<T>();

		Hits<T> hits = new Hits<T>();

		hits.setTotal(0);

		ResponseHeader responseHeader = new ResponseHeader();

		SolrServer server = SolrServerFactory.getSingleton().getDefaulSolrServer();
		
		T bean = null;
		try {
			bean = domainClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("反射创建实力出错！{}",e1);
		        
            }
			        
			throw new EspStoreException(e1);
		}
		
		StringBuffer sb = new StringBuffer();
		if(bean.getIndexType()!=null){
			sb.append(" index_type_int:"+bean.getIndexType().getType());
			sb.append(" AND index_subtype_int:"+bean.getIndexType().getSubtype()+" ");
		}
		
		queryRequest.setKeyword(StringUtils.isEmpty(queryRequest.getKeyword())?"":ClientUtils.escapeQueryChars(queryRequest.getKeyword()));
		try {
			queryRequest.setKeyword(anaylize(server, queryRequest.getKeyword()));
		} catch (SolrServerException | IOException e1) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e1);
		        
            }
			        
			throw new EspStoreException(e1);
		}
		
		sb.append(queryRequest.getQuerySyntax());
		
		if (logger.isDebugEnabled()) {
            
		    logger.debug("index param is :{}", sb.toString());
		    
        }
		
		SolrQuery solrQuery = new SolrQuery(sb.toString());
		
		solrQuery.setRows(queryRequest.getLimit());
		solrQuery.setStart(queryRequest.getOffset());
		solrQuery.setSort("sorttime", ORDER.desc);
		
		org.apache.solr.client.solrj.response.QueryResponse sds = null;
		try {
			sds = server.query(solrQuery);
			
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e);
		        
            }
			        
			throw new EspStoreException(e);
		}
		
		SolrDocumentList scs = sds.getResults();
		
		// 额外查询(在第一次查询条件没命中的情况下尝试对参数前后加模糊匹配查询,只有在debug为true时才开启)
		try {
			scs = extraSearch(server, scs, queryRequest);
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("额外查询异常{}", e);
		        
            }
			        
			throw new EspStoreException(e);
		}
		
		List<String> ids = Lists.newArrayList();
		
		for (SolrDocument item : scs) {
			if (item.get("identifier") != null)
				ids.add((String) item.get("identifier"));
		}
		
		List<T> result = Lists.newArrayList();
		
		if(ids.size()!=0){
			result = repository.getAll(ids);
		}
		
		if(result.size() != ids.size()){
		    
		    if (logger.isWarnEnabled()) {
                
		        logger.warn("数据库中数据和索引数据不一致!");
		        
            }
			        
		}
		
		hits.setTotal(scs.getNumFound());
		hits.setDocs(result);

		responseHeader.setQTime(sds.getQTime());
		response.setResponseHeader(responseHeader);
		response.setHits(hits);
		response.setResponseHeader(responseHeader);
		return response;
	}

	@Override
	public QueryResponse<String> searchFullText(QueryRequest queryRequest)
			throws EspStoreException {
		QueryResponse<String> response = new QueryResponse<>();

		Hits<String> hits = new Hits<>();

		hits.setTotal(0);

		ResponseHeader responseHeader = new ResponseHeader();

		SolrServer server = SolrServerFactory.getSingleton().getDefaulSolrServer();
		
		T bean = null;
		try {
			bean = domainClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("反射创建实力出错！{}",e1);
		        
            }
			        
			throw new EspStoreException(e1);
		}
		
		StringBuffer sb = new StringBuffer();
		if(bean.getIndexType()!=null){
			sb.append(" index_type_int:"+bean.getIndexType().getType());
			sb.append(" AND index_subtype_int:"+bean.getIndexType().getSubtype()+" ");
		}
		
		queryRequest.setKeyword(StringUtils.isEmpty(queryRequest.getKeyword())?"":ClientUtils.escapeQueryChars(queryRequest.getKeyword()));
//		try {
//			queryRequest.setKeyword(anaylize(server, queryRequest.getKeyword()));
//		} catch (SolrServerException | IOException e1) {
//			logger.error("全文检索异常",e1);
		
//			throw new EspStoreException(e1);
//		}
		
		sb.append(queryRequest.getQuerySyntax());
		
		//logger.debug("index param is :"+sb.toString());
		
		SolrQuery solrQuery = new SolrQuery(sb.toString());
		
		solrQuery.setRows(queryRequest.getLimit());
		solrQuery.setStart(queryRequest.getOffset());
		solrQuery.setSort("sorttime", ORDER.desc);
		
		String queryStr = solrQuery.getQuery();
		
		if (logger.isDebugEnabled()) {
            
		    logger.debug("index param is :{}", new Object[]{queryStr});
		    logger.debug("index param is :{}", new Object[]{solrQuery});

		}
		
		org.apache.solr.client.solrj.response.QueryResponse sds = null;
		try {
			sds = server.query(solrQuery);
			
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e);
		        
            }
			        
			throw new EspStoreException(e);
		}
		
		SolrDocumentList scs = sds.getResults();
		
		// 额外查询(在第一次查询条件没命中的情况下尝试对参数前后加模糊匹配查询,只有在debug为true时才开启)
		try {
			scs = extraSearch(server, scs, queryRequest);
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("额外查询异常{}", e);
		        
            }
			        
			throw new EspStoreException(e);
		}
		
		List<String> ids = Lists.newArrayList();
		
		for (SolrDocument item : scs) {
			if (item.get("identifier") != null)
				ids.add((String) item.get("identifier"));
			
		}
		
		hits.setTotal(scs.getNumFound());
		hits.setDocs(ids);

		responseHeader.setQTime(sds.getQTime());
		response.setResponseHeader(responseHeader);
		response.setHits(hits);
		response.setResponseHeader(responseHeader);
		return response;
	}
	
	@Override
	public QueryResponse<String> searchFullTextLike(QueryRequest queryRequest)
			throws EspStoreException {
		
		QueryResponse<String> response = new QueryResponse<>();

		Hits<String> hits = new Hits<>();

		hits.setTotal(0);

		ResponseHeader responseHeader = new ResponseHeader();

		SolrServer server = SolrServerFactory.getSingleton().getDefaulSolrServer();
		
		T bean = null;
		try {
			bean = domainClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("反射创建实力出错！{}",e1);
		        
            }
			        
			throw new EspStoreException(e1);
		}
		
		StringBuffer sb = new StringBuffer();
		if(bean.getIndexType()!=null){
			sb.append(" index_type_int:"+bean.getIndexType().getType());
			sb.append(" AND index_subtype_int:"+bean.getIndexType().getSubtype()+" ");
		}
		
		queryRequest.setKeyword(StringUtils.isEmpty(queryRequest.getKeyword())?"":ClientUtils.escapeQueryChars(queryRequest.getKeyword()));
		try {
			queryRequest.setKeyword(anaylize(server, queryRequest.getKeyword()));
		} catch (SolrServerException | IOException e1) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e1);
		        
            }
			        
			throw new EspStoreException(e1);
		}
		
		sb.append(queryRequest.getQuerySyntax());
		
		//logger.debug("index param is :"+sb.toString());
		
		SolrQuery solrQuery = new SolrQuery(sb.toString());
		
		solrQuery.setRows(queryRequest.getLimit());
		solrQuery.setStart(queryRequest.getOffset());
		solrQuery.setSort("sorttime", ORDER.desc);
		
		String queryStr = solrQuery.getQuery();
		
		if (logger.isDebugEnabled()) {
            
		    logger.debug("index param is :{}", new Object[]{queryStr});
		    logger.debug("index param is :{}", new Object[]{solrQuery});

		}
		
		org.apache.solr.client.solrj.response.QueryResponse sds = null;
		try {
			sds = server.query(solrQuery);
			
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("全文检索异常{}",e);
		        
            }
		            
			throw new EspStoreException(e);
		}
		
		SolrDocumentList scs = sds.getResults();
		
		// 额外查询(在第一次查询条件没命中的情况下尝试对参数前后加模糊匹配查询,只有在debug为true时才开启)
		try {
			scs = extraSearch(server, scs, queryRequest);
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("额外查询异常{}", e);
		        
            }
			        
			throw new EspStoreException(e);
		}
		
		List<String> ids = Lists.newArrayList();
		
		for (SolrDocument item : scs) {
			if (item.get("identifier") != null)
				ids.add((String) item.get("identifier"));
			
		}
		
		hits.setTotal(scs.getNumFound());
		hits.setDocs(ids);

		responseHeader.setQTime(sds.getQTime());
		response.setResponseHeader(responseHeader);
		response.setHits(hits);
		response.setResponseHeader(responseHeader);
		return response;
	}
	
	/**
	 * Gets the repository.
	 *
	 * @return the repository
	 */
	public ResourceRepository<T> getRepository() {
		return repository;
	}

	/**
	 * Sets the repository.
	 *
	 * @param repository the new repository
	 */
	public void setRepository(ResourceRepository<T> repository) {
		this.repository = repository;
	}

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexRepository#getSolrServer() 
	 */ 
		
	@Override
	public SolrServer getSolrServer() {
		return SolrServerFactory.getSingleton().getDefaulSolrServer();
	}
	
	
	///======================
	
	
	/**
	 * Anaylize.
	 *
	 * @param solrServer the solr server
	 * @param keyword the keyword
	 * @return the string
	 * @throws SolrServerException the solr server exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String anaylize(SolrServer solrServer, String keyword)
			throws SolrServerException, IOException {

		FieldAnalysisRequest request = new FieldAnalysisRequest();
		request.addFieldName("text");
		request.setFieldValue(keyword);

		FieldAnalysisResponse response = request.process(solrServer);
		Analysis analysis = response.getFieldNameAnalysis("text");
		Iterable<AnalysisPhase> analysisCollection = analysis.getIndexPhases();
		Iterator<AnalysisPhase> ite = analysisCollection.iterator();
		StringBuffer sb = new StringBuffer();

		while (ite.hasNext()) {
			AnalysisPhase ap = ite.next();
			List<TokenInfo> tokenInfos = ap.getTokens();
			if(tokenInfos.size() !=0){
				sb.append("(");
			}
			for (TokenInfo tokenInfo : tokenInfos) {
				String token = tokenInfo.getText();
				sb.append(" *" + token + "* ");
			}
			
			if(tokenInfos.size() !=0){
				sb.append(")");
			}
		}
		
		if (logger.isDebugEnabled()) {
            
		    logger.debug("after anaylize words is {}", sb.toString());
		    
        }
		        
		if("".equals(sb.toString().trim())){
			sb.append(" *" + ClientUtils.escapeQueryChars(keyword)+ "* ");
		}
		return sb.toString();
	}
	
	
	/**
	 * Extra search.
	 *
	 * @param server the cloud solr server
	 * @param docs the docs
	 * @param query the query
	 * @return the solr document list
	 * @throws SolrServerException the solr server exception
	 */
	private SolrDocumentList extraSearch(SolrServer server,
			SolrDocumentList docs, QueryRequest query)
			throws SolrServerException {
		if (DEBUG) {
			// 前缀加引号
			if (docs.getNumFound() == 0) {
				query.setKeyword(" *" + query.getKeyword());
				SolrQuery s_query = new SolrQuery(query.getQuerySyntax());
				org.apache.solr.client.solrj.response.QueryResponse rsp = server.query(s_query);
				docs = rsp.getResults();
				// 后缀加引号
				if (docs.getNumFound() == 0) {
					query.setExtraKeyWord(query.getKeyword() + "* ");
					s_query = new SolrQuery(query.getQuerySyntax());
					rsp = server.query(s_query);
					docs = rsp.getResults();

					// 前后加引号
					if (docs.getNumFound() == 0) {
						query.setExtraKeyWord(" *" + query.getKeyword() + "* ");
						s_query = new SolrQuery(query.getQuerySyntax());
						rsp = server.query(s_query);
						docs = rsp.getResults();
						return docs;
					} else {
						return docs;
					}

				} else {
					return docs;
				}

			} else {
				return docs;
			}

		} else {
			return docs;
		}
	}

	/**
	 * Adds the index.
	 *
	 * @param bean
	 *            the bean
	 * @return the update response
	 * @throws EspStoreException
	 *             the esp store exception
	 */
	public UpdateResponse addIndex(Object bean) throws EspStoreException {
		
		UpdateResponse response = null;
		
		NoIndexBean noIndex = bean.getClass().getAnnotation(NoIndexBean.class);
		
		if(noIndex!=null){
		    
		    if (logger.isInfoEnabled()) {
                
		        logger.info("======no add index==============");
		        
            }
			        
			return response;
		}
		
		if(bean instanceof List){
			return batchAddIndex((List<?>) bean);
		}
		
		SolrInputDocument doc = null;
		try {
			doc = getSolrServer().getBinder()
					.toSolrInputDocument(bean);

			if(bean instanceof IndexMapper){
				IndexMapper indexBean = (IndexMapper) bean;
				if (indexBean.getAdditionSearchFields() != null && indexBean.getAdditionSearchFields().size() > 0) {
					for (Entry<String, Object> entry : indexBean.getAdditionSearchFields()
							.entrySet()) {
						if(doc.containsKey(entry.getKey())){
							doc.remove(entry.getKey());
						}
						doc.addField(entry.getKey(), entry.getValue());
					}
				}
			}
			response = getSolrServer().add(doc);
			if(SolrServerFactory.getSingleton().getConfig().isCommit()){
				response = getSolrServer().commit();
			}

		} catch (IOException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("ProxyRepositoryImpl.add {}", e);
		        
            }
			        
			throw new EspStoreException(e);
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("ProxyRepositoryImpl.add {}", e);
		        
            }
			        
			throw new EspStoreException(e);
		}

		return response;
	}

	/**
	 * Batch add index.
	 *
	 * @param beans
	 *            the beans
	 * @return the update response
	 * @throws EspStoreException
	 *             the esp store exception
	 */
	public UpdateResponse batchAddIndex(List<?> beans)
			throws EspStoreException {
		UpdateResponse response = null;
		
		if(beans==null || beans.size()==0){
			response = new UpdateResponse();
			response.setElapsedTime(0);
			return response;
		}
		
		NoIndexBean noIndex = beans.get(0).getClass().getAnnotation(NoIndexBean.class);
		
		if(noIndex!=null){
		    
		    if (logger.isInfoEnabled()) {
                
		        logger.info("======no add index==============");
		        
            }
			        
			return response;
		}
		
		SolrInputDocument doc = null;
		List<SolrInputDocument> docs = Lists.newArrayList();
		for (Object bean : beans) {
			doc = getSolrServer().getBinder()
					.toSolrInputDocument(bean);
			if(bean instanceof IndexMapper){
				IndexMapper indexBean = (IndexMapper) bean;
				Map<String, Object> map = null;
				try {
					map = indexBean.getAdditionSearchFields();
				} catch (Exception e) {
				    
				    if (logger.isErrorEnabled()) {
                        
				        logger.error("获取额外索引属性异常！{}", indexBean);
				        
                    }
					        
					continue;
				}

				if (map != null && map.size() > 0) {
					for (Entry<String, Object> entry : map
							.entrySet()) {
						if(doc.containsKey(entry.getKey())){
							doc.remove(entry.getKey());
						}
						doc.addField(entry.getKey(), entry.getValue());
					}
				}
			}
			docs.add(doc);
		}
		try {
			SolrServer solrServer = getSolrServer();
			response = solrServer.add(docs);
//				if(SolrServerFactory.getSingleton().getConfig().isCommit()){
//					response = getSolrServer().commit();
//				}
			response = solrServer.commit();
			if (logger.isDebugEnabled()) {
                
    			    logger.debug(">>>>>>>>>>>>>", response);
    			    logger.debug("add index solr size:{}",beans.size());

			}
			
		} catch (IOException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("ProxyRepositoryImpl.add{}", e);
		        
            }
			        
			throw new EspStoreException(e);
		} catch (SolrServerException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("ProxyRepositoryImpl.add{}", e);
		        
            }
			        
			throw new EspStoreException(e);
		}

		return response;
	}

	/**
	 * Description 
	 * @param id
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.IndexRepository#delete(java.lang.String) 
	 */ 
		
	@Override
	public UpdateResponse delete(String id) throws EspStoreException {
		try {
			getSolrServer().deleteById(id);
			return getSolrServer().commit();
		} catch (SolrServerException | IOException e) {
		    
		    if (logger.isErrorEnabled()) {
                
		        logger.error("删除索引数据异常：{} , {}", id,e);
		        
            }
			        
			throw new EspStoreException(e);
		}
	}

	/**
	 * Description 
	 * @param ids
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.IndexRepository#batchDelete(java.util.List) 
	 */ 
		
	@Override
	public UpdateResponse batchDelete(List<String> ids)
			throws EspStoreException {
		try {
			getSolrServer().deleteById(ids);
			return getSolrServer().commit();
		} catch (SolrServerException | IOException e) {
			throw new EspStoreException(e);
		}
	}
}
