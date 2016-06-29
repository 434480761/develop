package nd.esp.service.lifecycle.repository.sdk.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import nd.esp.service.lifecycle.daos.titan.inter.TitanRepository;
import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.ResourceRepository;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.ds.Item;
import nd.esp.service.lifecycle.repository.ds.ItemsSpecification;
import nd.esp.service.lifecycle.repository.ds.jpa.Criteria;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.index.AdaptQueryRequest;
import nd.esp.service.lifecycle.repository.index.Hits;
import nd.esp.service.lifecycle.repository.index.OffsetPageRequest;
import nd.esp.service.lifecycle.repository.index.QueryRequest;
import nd.esp.service.lifecycle.repository.index.QueryResponse;
import nd.esp.service.lifecycle.repository.model.ResourceRelation;
import nd.esp.service.lifecycle.repository.sdk.DBCallBack;
import nd.esp.service.lifecycle.utils.SpringContextUtil;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;



/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年5月27日 下午9:20:04 
 * @version V1.0
 * @param <T>
 * @param <ID>
 */ 
  	
@NoRepositoryBean
//@Transactional(rollbackFor=Exception.class)
public class ProxyRepositoryImpl<T extends EspEntity, ID> extends
		SimpleJpaRepository<T, String> implements ResourceRepository<T> {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(ProxyRepositoryImpl.class);

	/** The entity manager. */
	private final EntityManager entityManager;

	/** The entity manager. */
	private final Class<T> domainClass;

	private JdbcTemplate jdbcTemplate;

	private  TransactionTemplate transactionTemplate;
	
	public TitanRepository titanRepository;
	
	/**
	 * Instantiates a new proxy repository impl.
	 *
	 * @param domainClass
	 *            the domain class
	 * @param em
	 *            the em
	 * @param jdbcTemplate2 
	 */
	public ProxyRepositoryImpl(Class<T> domainClass, EntityManager em, JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
		super(domainClass, em);
		entityManager = em;
		this.domainClass = domainClass;
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate=transactionTemplate;

	}

	/**
	 * Description.
	 *
	 * @param bean
	 *            the bean
	 * @return the t
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#add(com.nd.esp.repository.Education)
	 */

	@Override
	public T add(T bean) throws EspStoreException {
		converterIn(bean);
		T result = save(bean);

		if (logger.isDebugEnabled()) {
            
		    logger.debug(">>>>>>>>>>>>>>doe adda!@");
		    
        }
		
		//TODO titan repository code add
				getTitanRepository().add(bean);
		
		return converterOut(result);
	}
	
	
	/**
	 * Description.
	 *
	 * @param id
	 *            the id
	 * @see org.springframework.data.jpa.repository.support.SimpleJpaRepository#delete(java.io.Serializable)
	 */

	@Deprecated
	@Override
	public void delete(String id) {
		super.delete(id);
	}

	/**
	 * Description.
	 *
	 * @param bean
	 *            the bean
	 * @return the t
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#update(com.nd.esp.repository.Education)
	 */

	@Override
	public T update(T bean) throws EspStoreException {
		converterIn(bean);
		T result = super.save(bean);
		
		if (logger.isDebugEnabled()) {
            
		    logger.debug(">>>>>>>>>>>>>>doe update!@");
		    
        }
		
		//TODO titan repository code update
				getTitanRepository().update(bean);
		
		return converterOut(result);
	}

	/**
	 * Description.
	 *
	 * @param id
	 *            the id
	 * @return the t
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#get(java.lang.String)
	 */

	@Override
	public T get(String id) throws EspStoreException {
		return converterOut(findOne(id));
	}

	/**
	 * Description.
	 *
	 * @param ids
	 *            the ids
	 * @return the all
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#getAll(java.util.List)
	 */

	@Override
	public List<T> getAll(List<String> ids) throws EspStoreException {
		if(ids!=null && ids.size()!=0){
			//return getListWhereInCondition("identifier", ids);
			return batchConverterOut(getListWhereInCondition("identifier", ids));
		}else{
			return Lists.newArrayList();
		}
	}

	/**
	 * Description.
	 *
	 * @param queryRequest
	 *            the query request
	 * @return the query response
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.IndexRepository#searchByExample(com.nd.esp.repository.index.AdaptQueryRequest)
	 */

	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public QueryResponse<T> searchByExample(AdaptQueryRequest<T> queryRequest)
			throws EspStoreException {
	    if(queryRequest.getOffset()<0) {
	        queryRequest.setOffset(0);
	    }
	    if(queryRequest.getLimit()<=0) {
	        queryRequest.setLimit(20);
	    }
	    
	    
	    
	    QueryResponse<T> response = new QueryResponse<T>();
	    Pageable pageable = null;
	    T bean = queryRequest.getParam();
	    converterIn(bean);

        if(bean != null && bean.getIndexType().equals(IndexSourceType.ResourceRelationType)
            && ( ((ResourceRelation)bean).getResourceTargetType().equals(IndexSourceType.LessonType.getName())
                || ((ResourceRelation)bean).getResourceTargetType().equals(IndexSourceType.InstructionalObjectiveType.getName())
                || ( ((ResourceRelation)bean).getResourceTargetType().equals(IndexSourceType.ChapterType.getName())
                    && ((ResourceRelation)bean).getResType().equals(IndexSourceType.LessonType.getName()) ) ) ) {
            pageable = new PageRequest(queryRequest.getOffset()/queryRequest.getLimit(), queryRequest.getLimit(), 
                    Sort.Direction.ASC, "orderNum");
        }else {
            String column = HibernateParter.getColumnName(domainClass, "createTime");
            if(null != column) {
                pageable = new OffsetPageRequest(queryRequest.getOffset(), queryRequest.getLimit(), 
                        Sort.Direction.DESC, "createTime");
            } else {
                pageable = new OffsetPageRequest(queryRequest.getOffset(), queryRequest.getLimit());
            }
        }
        Page<T> page  = findAll(searchExampleSpecification(queryRequest), pageable);
        int pageOffset = queryRequest.getOffset()%queryRequest.getLimit();
//        List<T> resultList = new ArrayList<T>();
//        if(pageOffset!=0) {
//            if(pageOffset<page.getNumberOfElements()) {
//                resultList.addAll(page.getContent().subList(pageOffset, page.getNumberOfElements()));
//                if(page.hasNextPage()) {
//                    pageable = new OffsetPageRequest(queryRequest.getOffset()/queryRequest.getLimit()+1, queryRequest.getLimit());
//                    page = findAll(searchExampleSpecification(queryRequest), pageable);
//                    if(page.getNumberOfElements()<pageOffset) {
//                        resultList.addAll(page.getContent());
//                    } else {
//                        resultList.addAll(page.getContent().subList(0, pageOffset));
//                    }
//                }
//            }
//        } else {
//            resultList = page.getContent();
//        }
        
        batchConverterOut(page.getContent());
        Hits<T> hits = new Hits<T>();
        hits.setTotal(page.getTotalElements());
        hits.setDocs(page.getContent());
        response.setHits(hits);
        return response;
        
		//return indexRepository.searchByExample(queryRequest);
	}
	

	/**
	 * Description.
	 *
	 * @param queryRequest
	 *            the query request
	 * @return the query response
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.IndexRepository#search(com.nd.esp.repository.index.QueryRequest)
	 */

	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public QueryResponse<T> search(QueryRequest queryRequest)
			throws EspStoreException {

		AdaptQueryRequest<T>adaptQueryRequest=new AdaptQueryRequest();
		ModelMapper modelMapper=new ModelMapper();
		adaptQueryRequest=modelMapper.map(queryRequest, AdaptQueryRequest.class);

	    QueryResponse<T> response = searchByExample(adaptQueryRequest);
        
        return response;
		//return indexRepository.search(queryRequest);
	}
	
	/**
	 * Gets the entity manager.
	 *
	 * @return the entity manager
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Description.
	 *
	 * @param callBack
	 *            the call back
	 * @return the all
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#getAll(com.nd.esp.repository.sdk.DBCallBack)
	 */

	@Override
	public int getAll(DBCallBack callBack) throws EspStoreException {

		if (callBack != null) {
			int page = 0;
			int rows = 500;
			Pageable pageable = null;
			Page<T> list = null;
			do{				
				
				 pageable = new PageRequest(page, rows,Sort.Direction.ASC, EspEntity.PROP_IDENTIFIER);
				 list = findAll(pageable);
				for (T row : list) {
					if (callBack.execute(converterOut(row))) {
						callBack.finish();
						break;
					}
				}
				
				if (logger.isInfoEnabled()) {
                    
				    logger.info("getAll no index:{}", new Object[]{page*rows});
				    
                }
			}while(++page<list.getTotalPages());
			
			if (logger.isInfoEnabled()) {
                
			    logger.info("end getAll no index{}:", new Object[]{page*rows});
			    
            }
			
			return callBack.finish();
		}
		return 0;
	}

	/**
	 * Description.
	 *
	 * @param id
	 *            the id
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#del(java.lang.String)
	 */

	@Override
	public void del(String id) throws EspStoreException {
		/*T bean = get(id);
		bean.setRecordStatus(1);
		super.save(bean);*/
		try {
			    
		    if (logger.isInfoEnabled()) {
                
		        logger.info("delete res {} : {}", domainClass.getName(), id);
		        
            }
		            
			super.delete(id);
			
			List<String> ids = new ArrayList<String>();
			ids.add(id);
		}catch (Exception e) {
			if(e instanceof EmptyResultDataAccessException){
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("被删除的数据：{}不存在！{}", id ,e);
			        
                }
				        
				throw new EspStoreException("被删除的数据："+id+"不存在！");
			}
			
			if (logger.isErrorEnabled()) {
                
			    logger.error("删除的数据异常{}", e);
			    
            }
			        
			throw new EspStoreException(e);
		}

	}

	/**
	 * Description.
	 *
	 * @param bean
	 *            the bean
	 * @return the list
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#batchAdd(java.util.List)
	 */

	@Override
	public List<T> batchAdd(List<T> bean) throws EspStoreException {
		batchConverterIn(bean);
		List<T> result = this.save(bean);
		
		//TODO titan repository code batchAdd
				getTitanRepository().batchAdd(bean);

		return batchConverterOut(result);
	}

	/**
	 * Description.
	 *
	 * @param ids
	 *            the ids
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#batchDel(java.util.List)
	 */

	@Override
	public void batchDel(List<String> ids) throws EspStoreException {
		        
	    if (logger.isInfoEnabled()) {
            
	        logger.info("batch delete res {} : {}", domainClass.getName(), ids);
	        
        }
	    
	            
		List<T> beans = findAll(ids);
/*		for (String id : ids) {
			T bean = null;
			try {
				bean = domainClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new EspStoreException(e);
			}
			bean.setIdentifier(id);
		}*/
		try {
			//deleteInBatch(beans);
			for (T t : beans) {
				getEntityManager().remove(t);
			}
			
		}catch (Exception e) {
			if(e instanceof EmptyResultDataAccessException){
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("被删除的数据：{}不存在！{}", ids, e);
			        
                }
				        
				throw new EspStoreException("被删除的数据："+ids+"不存在！");
			}
			throw new EspStoreException(e);
		}
	}

	/**
	 * Description.
	 *
	 * @param items
	 *            the items
	 * @return the list
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#findByItems(java.util.List)
	 */

	@Override
	public List<T> findByItems(List<Item<? extends Object>> items) throws EspStoreException {
		return batchConverterOut(findAll(new ItemsSpecification<T>(items)));
	}

	/**
	 * Description
	 * 
	 * @param entity
	 * @return
	 * @throws EspStoreException
	 * @see com.nd.esp.repository.EspRepository#getByExample(com.nd.esp.repository.Education)
	 */

	public T getByExample(T entity) throws EspStoreException {
		Pageable page = new PageRequest(0, 1);
		Page<T> list = findAll(exampleSpecification(entity), page);
		return list.getContent().size()>0?converterOut(list.getContent().get(0)):null;
	}

	/**
	 * Description
	 * 
	 * @param entity
	 * @return
	 * @see com.nd.esp.repository.EspRepository#getAllByExample(com.nd.esp.repository.Education)
	 */

	public List<T> getAllByExample(T entity) throws EspStoreException {
		return batchConverterOut(findAll(exampleSpecification(entity)));
	}

	/**
	 * Example specification.
	 *
	 * @param entity
	 *            the entity
	 * @return the specification
	 */
	private Specification<T> exampleSpecification(final T entity) throws EspStoreException {
		//判断组合条件是否有错误
		final Boolean[] isErr = {false};
		Specification<T> spec = new Specification<T>() {
			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				try {
					Predicate p = cb.conjunction();
					Metamodel mm = entityManager.getMetamodel();

					EntityType<T> et = mm.entity(domainClass);
					Set<Attribute<? super T, ?>> attrs = et.getAttributes();
					for (Attribute<? super T, ?> a : attrs) {
						String name = a.getName();
						String javaName = a.getJavaMember().getName();
						Object value;
						try {
							value = PropertyUtils.getProperty(entity, javaName);
						} catch (IllegalAccessException
								| InvocationTargetException
								| NoSuchMethodException e) {
						    
						    if (logger.isErrorEnabled()) {
                                
						        logger.error("获取读取exampleSpecification[{};id={}, {}] 值错误{}"
						                ,entity.getClass() , entity.getIdentifier(), javaName, e);

						    }
							
							throw e;
						}
						if (value == null)
							continue;
						if (value instanceof String) {
							if (StringUtils.isEmpty((String) value)) {
								continue;
							}
						}

						p = cb.and(p, cb.equal(root.get(name), value));
					}
					
					return p;
				} catch (Exception e) {
				    
				    if (logger.isErrorEnabled()) {
                        
				        logger.error("exampleSpecification{}", e);
				        
                    }
					        
					isErr[0] = true;
					return null;
				}
			}
		};
		if (isErr[0])
			throw new EspStoreException("exampleSpecification 构造条件错误");
		return spec;
	}
	
	/**
     * Example specification.
     *
     * @param entity
     *            the entity
     * @return the specification
     */
    private Specification<T> searchExampleSpecification(final AdaptQueryRequest<T> queryRequest) throws EspStoreException {
        //判断组合条件是否有错误
        final Boolean[] isErr = {false};
        Specification<T> spec = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query,
                    CriteriaBuilder cb) {
                try {
                    Predicate p;
                    if(StringUtils.isNotEmpty(queryRequest.getKeyword())) {
                        Predicate p1 = cb.like(root.get("title").as(String.class), "%"+queryRequest.getKeyword()+"%");
                        Predicate p2 = cb.like(root.get("description").as(String.class), "%"+queryRequest.getKeyword()+"%");
                        p = cb.or(p1, p2);
                        String dbKey = HibernateParter.getColumnName(domainClass, "dbkeywords");
                        if(null != dbKey) {
                            Predicate p3 = cb.like(root.get("dbkeywords").as(String.class), "%"+queryRequest.getKeyword()+"%");
                            p = cb.or(p, p3);
                        }
                        String crDes = HibernateParter.getColumnName(domainClass, "crDescription");
                        if(null != crDes) {
                            Predicate p4 = cb.like(root.get("crDescription").as(String.class), "%"+queryRequest.getKeyword()+"%");
                            p = cb.or(p, p4);
                        }
                        String dbEduDes = HibernateParter.getColumnName(domainClass, "dbEduDescription");
                        if(null != dbEduDes) {
                            Predicate p5 = cb.like(root.get("dbEduDescription").as(String.class), "%"+queryRequest.getKeyword()+"%");
                            p = cb.or(p, p5);
                        }

                    } else {
                        p = cb.conjunction();
                    }
                    
                    Map<String,Object> extraParam = queryRequest.getExtraParam();
                    if(null != extraParam) {
                        for(Entry<String, Object> entry : extraParam.entrySet()){
                            String field = entry.getKey();
                            Object value = entry.getValue();
    
                            if(value instanceof Collection) {
                                if(((Collection) value).size()>0) {
                                    p = cb.and(p, root.get(field).in((Collection)value));
                                }
                            } else if(value instanceof String) {
                                if(StringUtils.isNotEmpty((String)value)){
                                    p = cb.and(p, cb.equal(root.get(field), value));
                                }
                            } else {
                                p = cb.and(p, cb.equal(root.get(field), value));
                            }
                        }
                    }
                    
                    T entity = queryRequest.getParam();
                    if(entity!=null) {
                        Metamodel mm = entityManager.getMetamodel();
    
                        EntityType<T> et = mm.entity(domainClass);
                        Set<Attribute<? super T, ?>> attrs = et.getAttributes();
                        for (Attribute<? super T, ?> a : attrs) {
                            String name = a.getName();
                            String javaName = a.getJavaMember().getName();
                            Object value;
                            try {
                                value = PropertyUtils.getProperty(entity, javaName);
                            } catch (IllegalAccessException
                                    | InvocationTargetException
                                    | NoSuchMethodException e) {
                                
                                if (logger.isErrorEnabled()) {
                                    
                                    logger.error("获取读取exampleSpecification[{};id={}, {}] 值错误{}"
                                            ,entity.getClass() , entity.getIdentifier(), javaName, e);
    
                                }
                                
                                throw e;
                            }
                            if (value == null)
                                continue;
                            if (value instanceof String) {
                                if (StringUtils.isEmpty((String) value) 
                                        || ((String) value).equals("{}") || ((String) value).equals("[]")) {
                                    continue;
                                }
                            }
                            if (value instanceof Collection) {
                                if(((Collection) value).isEmpty()) {
                                    continue;
                                }
                            }
                            if(javaName.equals("dbcategories") && value instanceof String) {
                                List<String> categories = JSON.parseArray(
                                        (String) value, String.class);
                                if(null != categories) {
                                    Predicate pPath = cb.disjunction();
                                    Predicate pCode = cb.disjunction();
                                    boolean bHasPath = false;
                                    boolean bHasCode = false;
                                    for(String category:categories) {
                                        if(category.contains("/")) {
                                            category = category.replace('*', '%');
                                            pPath = cb.or(pPath, cb.like(root.get(name).as(String.class), "%"+category+"%"));
                                            bHasPath = true;
                                        } else {
                                            pCode = cb.or(pCode, cb.like(root.get(name).as(String.class), "%"+category+"%"));
                                            bHasCode = true;
                                        }
                                    }
                                    if(bHasPath) {
                                        p = cb.and(p, pPath);
                                    }
                                    if(bHasCode) {
                                        p = cb.and(p, pCode);
                                    }
                                }
                            } else {
                                p = cb.and(p, cb.equal(root.get(name), value));
                            }
                        }
                    }
                    return p;
                } catch (Exception e) {
                    
                    if (logger.isErrorEnabled()) {
                        
                        logger.error("exampleSpecification{}", e);
                        
                    }
                            
                    isErr[0] = true;
                    return null;
                }
            }
        };
        if (isErr[0])
            throw new EspStoreException("exampleSpecification 构造条件错误");
        return spec;
    }

	/**
	 * Description
	 * 
	 * @param entity
	 * @param pageable
	 * @return
	 * @see com.nd.esp.repository.EspRepository#getPageByExample(com.nd.esp.repository.Education,
	 *      org.springframework.data.domain.Pageable)
	 */

	public Page<T> getPageByExample(final T entity, Pageable pageable) throws EspStoreException {
		if (entity == null)
			return null;
		Page<T> page  = findAll(exampleSpecification(entity), pageable);
		batchConverterOut(page.getContent());
		return page;
	}

	/**
	 * Description.
	 *
	 * @param items
	 *            the items
	 * @return the t
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#findOneByItems(java.util.List)
	 */

	@Override
	public T findOneByItems(List<Item<? extends Object>> items) throws EspStoreException {
		List<T> beans = this.findByItems(items);
		if (beans != null && beans.size() != 0) {
			return beans.get(0);
		}
		return null;
	}

	/**
	 * Description.
	 *
	 * @param propertyName
	 *            the property name
	 * @param propertyValues
	 *            the property values
	 * @return the list where in condition
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#getListInProperty(java.lang.String,
	 *      java.util.List)
	 */

	@Override
	public List<T> getListWhereInCondition(final String propertyName,
			List<?> propertyValues) throws EspStoreException {
	    
	    if (logger.isInfoEnabled()) {
            
	        logger.info("DOSQL2:propertyName is{}  propertyValues {}", propertyName, propertyValues);
	        
        }
		        
		String dbName = HibernateParter.getColumnName(domainClass, propertyName);
		
		if (logger.isInfoEnabled()) {
            
		    logger.info("where property name is {}", dbName);
		    
        }
		        
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder
				.createQuery(domainClass);
		Root<T> root = criteriaQuery.from(domainClass);
		root.get(propertyName);
		//System.out.println(HibernateParter.getTableName(domainClass));
		StringBuffer sb = new StringBuffer();
//		sb.append("select * from ").append(HibernateParter.getTableName(domainClass)).
//		append(" where ").append(dbName).
//		append(" in (:values)").append(" order by ").
//		append(" field(").append(dbName).append(",").
//		append(" :values").append(")");
		
		sb.append("select a from ").append(domainClass.getName()).append(" as a").
		append(" where ").append(dbName).
		append(" in (:values)").append(" order by ").
		append(" field(").append(dbName).append(",").
		append(" :values").append(")");
		
		if (logger.isInfoEnabled()) {
            
		    logger.info("DOSQL: {}", sb.toString());
		    
        }
		        
		Query query = entityManager.createQuery(sb.toString(), domainClass);
		query.setParameter("values", propertyValues);
		//query.getResultList();
		/*CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> criteriaQuery = criteriaBuilder
				.createQuery(domainClass);
		Root<T> root = criteriaQuery.from(domainClass);
		Path<Object> exp = root.get(propertyName);
		criteriaQuery.where(exp.in(propertyValues));
		criteriaQuery.c
		return batchConverterOut(entityManager.createQuery(criteriaQuery).getResultList());*/
		return batchConverterOut(query.getResultList());
//		return query.getResultList();
	}

	/**
	 * Description .
	 *
	 * @param entity
	 *            the entity
	 * @throws EspStoreException
	 *             the esp store exception
	 * @see com.nd.esp.repository.EspRepository#deleteAllByExample(com.nd.esp.repository.Education)
	 */

	@Override
	public void deleteAllByExample(T entity) throws EspStoreException {
		List<T> beans = findAll(exampleSpecification(entity));
		List<String> ids = Lists.newArrayList();
		for (T bean : beans) {
			ids.add(bean.getIdentifier());
		}
		super.delete(beans);
	}

	/**
	 * Description 
	 * @param items
	 * @param page
	 * @return 
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#findByItems(java.util.List, org.springframework.data.domain.Pageable) 
	 */ 
		
	@Override
	public Page<T> findByItems(List<Item<? extends Object>> items, Pageable page) throws EspStoreException {
		Page<T> result  = findAll(new ItemsSpecification<T>(items),page);
		batchConverterOut(result.getContent());
		return result;
	}
	
	
	
	public void converterIn(final T bean) throws EspStoreException {
		if (bean == null)
			return;
		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {

			@Override
			public void doWith(java.lang.reflect.Field field)
					throws IllegalArgumentException, IllegalAccessException {

				DataConverter dataConverter = field
						.getAnnotation(DataConverter.class);
				if (dataConverter != null) {
					String targetProp = dataConverter.target();
					try {
						Object targetPropValue = PropertyUtils.getProperty(
								bean, targetProp);
						if (targetPropValue != null) {
							if (targetPropValue instanceof List) {
								// String v = MyUtils
								// .list2Json((List<String>) targetPropValue);
								String v = JSON
										.toJSONString((List<String>) targetPropValue);
								PropertyUtils.setProperty(bean,
										field.getName(), v);
							} else if (targetPropValue instanceof Map) {
								// String v = MyUtils
								// .map2Json((Map<String, String>)
								// targetPropValue);
								String v = JSON
										.toJSONString((Map<String, String>) targetPropValue);
								PropertyUtils.setProperty(bean,
										field.getName(), v);
							}
						}
					} catch (InvocationTargetException | NoSuchMethodException e) {
					    
					    if (logger.isErrorEnabled()) {
                            
					        logger.error("添加实体:{}异常{}", bean, e);
					        
                        }
						        
						throw new RuntimeException("添加实体" + bean + "异常", e);
					}
				}
			}
		});
	}

	public T converterOut(final T bean) throws EspStoreException {
		if (bean == null)
			return null;

		ReflectionUtils.doWithFields(bean.getClass(), new FieldCallback() {

			@Override
			public void doWith(java.lang.reflect.Field field)
					throws IllegalArgumentException, IllegalAccessException {

				DataConverter dataConverter = field
						.getAnnotation(DataConverter.class);
				if (dataConverter != null) {
					String targetProp = dataConverter.target();
					Class<?> targetType = dataConverter.type();

					try {
						Object propValue = PropertyUtils.getProperty(bean,
								field.getName());
						if (propValue != null) {
							if (targetType == List.class) {
								// List<String> v = MyUtils
								// .json2List((String) propValue);
								List<String> v = JSON.parseArray(
										(String) propValue, String.class);
								PropertyUtils.setProperty(bean, targetProp, v);
							} else if (targetType == Map.class) {
								// Map<String, String> v = MyUtils
								// .json2Map((String) propValue);
								Map<String, String> v = (Map<String, String>) JSON
										.parse((String) propValue);
								PropertyUtils.setProperty(bean, targetProp, v);
							}
						}
					} catch (InvocationTargetException | NoSuchMethodException e) {
					    
					    if (logger.isErrorEnabled()) {
					        
					        logger.error("添加实体{}异常{}", bean, e);
					        
					    }
						        
						throw new RuntimeException("添加实体" + bean + "异常", e);
					}
				}
			}
		});
		return bean;
	}

	public void batchConverterIn(List<T> beans)
			throws EspStoreException {
		if(beans!=null){
			for (T item : beans) {
				converterIn(item);
			}
		}
	}
	
	public List<T> batchConverterOut(List<T> beans)throws EspStoreException {
		if(beans!=null){
			for (T item : beans) {
				converterOut(item);
			}
		}
		return beans;
	}

	/**
	 * Description 
	 * @param criteria
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#findOneByCriteria(com.nd.esp.repository.ds.jpa.Criteria) 
	 */ 
		
	@Override
	public T findOneByCriteria(Criteria<T> criteria) throws EspStoreException {
		return findOne(criteria);
	}

	/**
	 * Description 
	 * @param criteria
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#findAllByCriteria(com.nd.esp.repository.ds.jpa.Criteria) 
	 */ 
		
	@Override
	public List<T> findAllByCriteria(Criteria<T> criteria)
			throws EspStoreException {
		return findAll(criteria);
	}

	/**
	 * Description 
	 * @param criteria
	 * @param page
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#findByCriteria(com.nd.esp.repository.ds.jpa.Criteria, org.springframework.data.domain.Pageable) 
	 */ 
		
	@Override
	public Page<T> findByCriteria(Criteria<T> criteria, Pageable page)
			throws EspStoreException {
		return findAll(criteria, page);
	}
	
	

	/**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.EspRepository#getJdbcTemple() 
	 */ 
		
	@Override
	public JdbcTemplate getJdbcTemple() {
		return jdbcTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * Description 
	 * @param criteria
	 * @return 
	 * @see com.nd.esp.repository.EspRepository#countByCriteria(com.nd.esp.repository.ds.jpa.Criteria) 
	 */ 
		
	@Override
	public long countByCriteria(Criteria<T> criteria) {
		return super.count(criteria);
	}

	/**
	 * Description 
	 * @param entity
	 * @return 
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspRepository#countByExample(com.nd.esp.repository.EspEntity) 
	 */ 
		
	@Override
	public long countByExample(T entity) throws EspStoreException {
		return super.count(exampleSpecification(entity));
	}
	
	
//	@Transactional(propagation=Propagation.SUPPORTS)
//	// 更新资源categories字段的同时往resource_categories中插入数据
//	public void addCategoriesAndResourceCategories(String table){
//        String selectForCount = "SELECT COUNT(*) FROM data_migration";
//        long allCount = jdbcTemplate.queryForLong(selectForCount);
//        long pageSize = 500;
//        long page = (long) Math.ceil((double)allCount / pageSize);
//        System.out.println(allCount + "   ::    " + page);
//        String selectForAll = "SELECT * FROM data_migration LIMIT ?, ?";
//        for (int i = 0; i < page; i++) {
//            List<Map<String, Object>> questionDatas = jdbcTemplate.queryForList(selectForAll, 
//                    new Object[]{i * pageSize, pageSize});
//            System.out.println("当前页" + i);
////            if (i == page - 1) {
////                System.out.println(questionDatas.size());
////            }
//            for (int j = 0; j < questionDatas.size(); j++) {
//                String stage = (String)questionDatas.get(j).get("phases");
//                stage = (stage == null ? "" : stage);
//                String grade = (String)questionDatas.get(j).get("grade");
//                grade = (grade == null ? "" : grade);
//                String subject = (String)questionDatas.get(j).get("subjects");
//                subject = (subject == null ? "" : subject);
//                String edition = (String)questionDatas.get(j).get("edition");
//                edition = (edition == null ? "" : edition);
//                String subEdition = "";
//                if(!("").equals(edition)&& edition.length() > 3){
//                    //判断版本是否为子版本
//                    String s = edition.substring(edition.length() - 3);
//                    if(!s.equals("000")){
//                        subEdition = edition;
//                        edition = edition.substring(0, edition.length() - 3)+"000";
//                    }
//                }
//                String pattern = "\"{0}/{1}/{2}/{3}/{4}\"";
//                String path = MessageFormat.format(pattern, stage,grade,subject,edition,subEdition);
//                String identifier = (String)questionDatas.get(j).get("identifier");
//                String categories = (String)questionDatas.get(j).get("categories");
//                if (categories != null && !"".equals(categories)) {
//                    if (("[]").equals(categories)) {
//                        // categories的内容为："[]"；赋值之后类似于:["$ON040000//$SB0100/$E001000/$E001004"]
//                        categories = categories.replace("]", path) + "]";
//                    }else{
//                        // categories的内容为："[$ON040000//$SB0100/$E001000/$E001004]";复制之后类似于：
//                        // "[$ON040000//$SB0100/$E001000/$E001004, "$ON020000//$SB0100/$E001000/$E001001"]"
//                        categories = categories.replace("]", ",") + path + "]";
//                    }
//                    
//                }else{
//                    categories = "[" + path + "]";
//                }
//                
//                String updateString = "UPDATE " + table + " SET categories = ? WHERE identifier = ?";
//                jdbcTemplate.update(updateString , new Object[]{categories, identifier});
////                map.put("identifier", identifier);
////                map.put("categories", categories);
////                categoriesList.add(map);
//                path = "[" + path + "]";
//                // 为学段（小学、初中、高中等）在resource_categories中创建一条记录
//                if(!"".equals(stage)){
//                    insertResourceCategories(stage, path, identifier);
//                }
//                // 为年级（一年级、二年级等）在resource_categories中创建一条记录
//                if(!"".equals(grade)){
//                    insertResourceCategories(grade, path, identifier);
//                }
//                // 为学科（数学、语文、化学等）在resource_categories中创建一条记录
//                if(!"".equals(subject)){
//                    insertResourceCategories(subject, path, identifier);
//                }
//                // 为版本（人教版一年级起点、人教版B等）在resource_categories中创建一条记录
//                if(!"".equals(edition)){
//                    insertResourceCategories(edition, path, identifier);
//                }
//                // 为子版本（上、下册等）在resource_categories中创建一条记录
//                if(!"".equals(subEdition)){
//                    insertResourceCategories(subEdition, path, identifier);
//                }
//            }
//        }
//        
//        System.out.println(forConfirm);
//        System.out.println("执行成功");
//    }
//	@Transactional(propagation=Propagation.SUPPORTS)
//	//	更新资源categories字段
//    public void addCategories(String table){
//        String selectForCount = "SELECT COUNT(*) FROM data_migration";
//        long allCount = jdbcTemplate.queryForLong(selectForCount);
//        long pageSize = 500;
//        long page = (long) Math.ceil((double)allCount / pageSize);
//        System.out.println(allCount + "   ::    " + page);
//        String selectForAll = "SELECT * FROM data_migration LIMIT ?, ?";
//        for (int i = 0; i < page; i++) {
//            List<Map<String, Object>> questionDatas = jdbcTemplate.queryForList(selectForAll, 
//                    new Object[]{i * pageSize, pageSize});
//            System.out.println("当前页" + i);
//            
//            for (int j = 0; j < questionDatas.size(); j++) {
//                String stage = (String)questionDatas.get(j).get("phases");
//                stage = (stage == null ? "" : stage);
//                String grade = (String)questionDatas.get(j).get("grade");
//                grade = (grade == null ? "" : grade);
//                String subject = (String)questionDatas.get(j).get("subjects");
//                subject = (subject == null ? "" : subject);
//                String edition = (String)questionDatas.get(j).get("edition");
//                edition = (edition == null ? "" : edition);
//                String subEdition = "";
//                if(!("").equals(edition)&& edition.length() > 3){
//                    //判断版本是否为子版本
//                    String s = edition.substring(edition.length() - 3);
//                    if(!s.equals("000")){
//                        subEdition = edition;
//                        edition = edition.substring(0, edition.length() - 3)+"000";
//                    }
//                }
//                String pattern = "\"{0}/{1}/{2}/{3}/{4}\"";
//                String path = MessageFormat.format(pattern, stage,grade,subject,edition,subEdition);
//                String identifier = (String)questionDatas.get(j).get("identifier");
//                String categories = (String)questionDatas.get(j).get("categories");
//                if (categories != null && !"".equals(categories)) {
//                    if (("[]").equals(categories)) {
//                        // categories的内容为："[]"；赋值之后类似于:["$ON040000//$SB0100/$E001000/$E001004"]
//                        categories = categories.replace("]", path) + "]";
//                    }else{
//                        // categories的内容为："["$ON040000//$SB0100/$E001000/$E001004"]";复制之后类似于：
//                        // "["$ON040000//$SB0100/$E001000/$E001004", "$ON020000//$SB0100/$E001000/$E001001"]"
//                        categories = categories.replace("]", ",") + path + "]";
//                    }
//                    
//                }else{
//                    categories = "[" + path + "]";
//                }
//                String updateString = "UPDATE " + table + " SET categories = ? WHERE identifier = ?";
//                jdbcTemplate.update(updateString , new Object[]{categories, identifier});
//            }
//        }
//        System.out.println("执行成功");
//    }
//	
//	private int forConfirm = 0;
//    @Transactional(propagation=Propagation.SUPPORTS)
//    private void insertResourceCategories(String ndCode, String path, String identifier) {
//        String insertResourceCategories = "INSERT INTO resource_categories(identifier, description, record_status, "
//                + "title, resource, taxOnCode, taxOnName, taxOnPath, category_code, category_name, short_name, taxoncodeid) "
//                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        String selectCategoryData = "SELECT * FROM category_datas WHERE nd_code = ?";
//        List<Map<String, Object>> categoryData = jdbcTemplate.queryForList(selectCategoryData, new Object[]{ndCode});
//        
//        if(!categoryData.isEmpty()){
//            String resourceCategoriesIdentifier =  UUID.randomUUID().toString();
//            String description = null;
//            Integer recordStatus = 0;
//            String title = null;
//            String resource = identifier;
//            String taxOnCode = ndCode;
//            String taxOnName = (String)categoryData.get(0).get("title");
//            String taxOnPath = path;
//            String categoryCode = ndCode.substring(0, 2);
//            String categoryName = null;
//            String shortName = (String)categoryData.get(0).get("short_name");
//            String taxOnCodeId = (String)categoryData.get(0).get("identifier");
//            
//            forConfirm++;
//            jdbcTemplate.update(insertResourceCategories, new Object[]{resourceCategoriesIdentifier, description, recordStatus, title, 
//                    resource, taxOnCode, taxOnName, taxOnPath, categoryCode, categoryName, shortName, taxOnCodeId});
//        }
//    }
	
	@Transactional(propagation=Propagation.REQUIRED)
	@Override
	public Object callProcedure(String procedure) {
		Query query = entityManager.createNativeQuery("{call " + procedure + " }");
		query.executeUpdate();
		return null;
	}
	
	public TitanRepository getTitanRepository() {
		if(titanRepository == null){
			titanRepository = SpringContextUtil.getApplicationContext()
					.getBean("TitanRepositoryImpl",TitanRepository.class);
		}
		return titanRepository;
	}


}
