package nd.esp.service.lifecycle.repository.model;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.EspRepository;
import nd.esp.service.lifecycle.repository.IndexMapper;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.sdk.ResourceRelationRepository;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
  


// TODO: Auto-generated Javadoc
/**
 *  
 *
 * @author Rainy(yang.lin)
 * @version V1.0
 * @Description 
 * @date 2015年5月20日 上午10:27:21
 */ 
@Entity
@Table(name = "resource_relations")
@NamedQueries({
    @NamedQuery(name = "getResourceRelations", query = "SELECT rr.orderNum  AS orderNum FROM ResourceRelation rr WHERE sourceUuid = :sourceUuid AND rr.resType = :resType AND rr.resourceTargetType = :targetType AND rr.enable = 1"),
    @NamedQuery(name = "getResourceRelationsWithOrder", query = "SELECT rr.orderNum AS orderNum, rr.sortNum AS sortNum, rr.resourceTargetType AS resourceTargetType FROM ResourceRelation rr WHERE sourceUuid = :sourceUuid AND rr.resType = :resType AND rr.enable = 1 ORDER BY rr.sortNum DESC"),
		@NamedQuery(name = "batchGetRelationByResourceSourceOrTarget", query = "SELECT rr FROM ResourceRelation rr WHERE (rr.resType=:resType AND rr.sourceUuid IN (:rids)) or (rr.resourceTargetType=:resType AND rr.target IN (:rids))")
})
public class ResourceRelation extends EspEntity implements IndexMapper {
	
	/** The Constant PROP_TAGS. */
	public static final String PROP_TAGS = "tags";
	
	/** Logging. */
	private static Logger logger = LoggerFactory
			.getLogger(ResourceRelation.class);
	
	private static final long serialVersionUID = 1L;
	
	@Column(name = "label")
	private String label;
	
    /** The order num. */
	@Column(name = "order_num")
 	private Integer orderNum;
	
	/** The order num. */
	@Column(name = "sort_num")
	private Float sortNum; 
	
	/** The relation type. */
	@Column(name = "relation_type")
 	private String relationType; 
	
	/** The res type. */
	@Column(name = "res_type")
 	private String resType; 
	
	/** The resource target type. */
	@Column(name = "resource_target_type")
 	private String resourceTargetType; 
	
	/** The source uuid. */
	@Column(name="source_uuid")
 	private String sourceUuid; 
	
    /** The target. */
	@Column(name="target")
 	private String target; 
	
	/** The categorys. */
	@Transient
 	private List<String> categorys;
 	
	
	/** The enable. */
	@Column(name="enable")
	private Boolean enable; 
	
	/** The tags. */
	@Transient
 	private List<String> tags; 
	
	/** The dbtags. */
	@Column(name="tags")
	@DataConverter(target="tags", type=List.class)
 	private String dbtags; 
	
	/** * ******************* LIFE_CYCLE_ATTRIBUTES START *****************/

    /** The version. */
    protected String version;

    /** The status. */
    @Column(name = "estatus")
    protected String status;

    /** The publisher. */
    protected String publisher;

    /** The creator. */
    protected String creator;

    /**
    * 
    */
    @Column(name = "provider")
    protected String provider;
    
    /**
    * 
    */
    @Column(name = "provider_source")
    protected String providerSource;
    
    /** The create time. */
    @Column(name = "create_time")
    protected Timestamp createTime;

    /** The last update. */
    @Column(name = "last_update")
    protected Timestamp lastUpdate;
    
    
	/** The create time. */
	@Column(name = "resource_create_time")
	protected BigDecimal resourceCreateTime;
	
	/** The create time. */
	@Column(name = "target_create_time")
	protected BigDecimal targetCreateTime;

    /** **************** LIFE_CYCLE_ATTRIBUTES END * *******************/
	
	/**
	 * Description .
	 *
	 * @param identifier the new identifier
	 * @see com.nd.esp.repository.Education#setIdentifier(java.lang.String)
	 */ 
		
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * Description .
	 *
	 * @return the identifier
	 * @see com.nd.esp.repository.Education#getIdentifier()
	 */ 
		
	public String getIdentifier() {
		return this.identifier;
	}
	
	/**
	 * get the label
	 * 
	 * @return
	 * @since
	 */
	public String getLabel() {
        return label;
    }

	/**
	 * set the label
	 * 
	 * @param label
	 * @since
	 */
    public void setLabel(String label) {
        this.label = label;
    }
	
	/**
	 * Sets the order num.
	 *
	 * @param orderNum the new order num
	 */
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}
	
	/**
	 * Gets the order num.
	 *
	 * @return the order num
	 */
	public Integer getOrderNum() {
		return this.orderNum;
	}
	
	/**
	 * Sets the relation type.
	 *
	 * @param relationType the new relation type
	 */
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	
	/**
	 * Gets the relation type.
	 *
	 * @return the relation type
	 */
	public String getRelationType() {
		return this.relationType;
	}
	
	/**
	 * Sets the res type.
	 *
	 * @param resType the new res type
	 */
	public void setResType(String resType) {
		this.resType = resType;
	}
	
	/**
	 * Gets the res type.
	 *
	 * @return the res type
	 */
	public String getResType() {
		return this.resType;
	}
	
	/**
	 * Sets the resource target type.
	 *
	 * @param resourceTargetType the new resource target type
	 */
	public void setResourceTargetType(String resourceTargetType) {
		this.resourceTargetType = resourceTargetType;
	}
	
	/**
	 * Gets the resource target type.
	 *
	 * @return the resource target type
	 */
	public String getResourceTargetType() {
		return this.resourceTargetType;
	}
	

	/**
	 * Description .
	 *
	 * @return the categorys
	 * @see com.nd.esp.repository.NdResource#getCategorys()
	 */ 
		
	public List<String> getCategorys() {
		return categorys;
	}

	/**
	 * Description .
	 *
	 * @param categorys the new categorys
	 * @see com.nd.esp.repository.NdResource#setCategorys(java.util.List)
	 */ 
		
	public void setCategorys(List<String> categorys) {
		this.categorys = categorys;
	}
	
	/**
	 * Gets the composite pk.
	 *
	 * @return the composite pk
	 */
	public String getCompositePk(){
		return getSourceUuid()+"-"+getTarget()+"-"+getRelationType();
	}

	/**
	 * Description .
	 *
	 * @return the publisher
	 * @see com.nd.esp.repository.NdResource#getPublisher()
	 */ 
		
	public String getPublisher() {
		return publisher;
	}

	/**
	 * Description .
	 *
	 * @param publisher the new publisher
	 * @see com.nd.esp.repository.NdResource#setPublisher(java.lang.String)
	 */ 
		
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	/**
	 * Description .
	 *
	 * @return the creator
	 * @see com.nd.esp.repository.NdResource#getCreator()
	 */ 
		
	public String getCreator() {
		return creator;
	}

	/**
	 * Description .
	 *
	 * @param creator the new creator
	 * @see com.nd.esp.repository.NdResource#setCreator(java.lang.String)
	 */ 
		
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.ResourceRelationType;
	}

	/**
	 * Description 
	 * @return
	 * @throws EspStoreException 
	 * @see com.nd.esp.repository.EspEntity#getAdditionSearchFields() 
	 */ 
		
	@Override
	public Map<String, Object> getAdditionSearchFields() throws EspStoreException {
		final Map<String,Object> other = super.getAdditionSearchFields();
		try {
			ResourceRelationRepository relationRepository = (ResourceRelationRepository) ServicesManager.get(IndexSourceType.ResourceRelationType.getName());
			EspRepository<?> targetBeanService = ServicesManager.get(this.getResourceTargetType());
			final IndexMapper ndbean = targetBeanService.get(this.getTarget());
			if(ndbean == null ){
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("新增关系错误,id为:{}的{}资源不存在的", this.getTarget(), this.getResourceTargetType());
			        logger.error("关系数据为{}:{},{}:{}", this.getResType(), this.getSourceUuid(), this.resourceTargetType, this.getTarget());
			        
                }
			    
				relationRepository.del(this.identifier);
				//return other;
				throw new EspStoreException("新增关系错误，id为"+this.getTarget()+"的资源不存在的"); 
			}
			ReflectionUtils.doWithFields(ndbean.getClass(), new FieldCallback() {

				@Override
				public void doWith(java.lang.reflect.Field field)
						throws IllegalArgumentException, IllegalAccessException {
					org.apache.solr.client.solrj.beans.Field solrField = field
							.getAnnotation(org.apache.solr.client.solrj.beans.Field.class);

					if (solrField != null &&!solrField.value().equals("identifier") &&!solrField.value().equals("index_type_int") &&!solrField.value().equals("index_subtype_int")) {
						field.setAccessible(true);
						Object value = null;
						try {
							value = field.get(ndbean);
							field.setAccessible(false);
							if (value == null)
								return;
							other.put(solrField.value(), value);
						} catch (IllegalArgumentException | IllegalAccessException e) {
						    
						    if (logger.isWarnEnabled()) {
                                
						        logger.warn("获取属性值异常:{}",e);
						        
                            }
							        
						}
					}
				}

			});
			other.putAll(ndbean.getAdditionSearchFields());
			return other;
		} catch (Exception e) {
			throw new EspStoreException(e.getMessage());
		}
	}

	public Map<String,Object> addAdditionSearchFields(Map<String,Object> fields)throws EspStoreException {
		final Map<String,Object> other = super.getAdditionSearchFields();
		other.putAll(fields);
		return other;
	}
	/**
	 * Gets the enable.
	 *
	 * @return the enable
	 */
	public Boolean getEnable() {
		return enable;
	}

	/**
	 * Sets the enable.
	 *
	 * @param enable the new enable
	 */
	public void setEnable(Boolean enable) {
		this.enable = enable;
	}


	public Float getSortNum() {
		return sortNum;
	}

	public void setSortNum(Float sortNum) {
		this.sortNum = sortNum;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getDbtags() {
		return dbtags;
	}

	public void setDbtags(String dbtags) {
		this.dbtags = dbtags;
	}
	
	
	public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
	
    public String getProviderSource() {
        return providerSource;
    }

    public void setProviderSource(String providerSource) {
        this.providerSource = providerSource;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

	public BigDecimal getResourceCreateTime() {
		return resourceCreateTime;
	}

	public void setResourceCreateTime(BigDecimal resourceCreateTime) {
		this.resourceCreateTime = resourceCreateTime;
	}

	public BigDecimal getTargetCreateTime() {
		return targetCreateTime;
	}

	public void setTargetCreateTime(BigDecimal targetCreateTime) {
		this.targetCreateTime = targetCreateTime;
	}

	public String getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid) {
		this.sourceUuid = sourceUuid;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
}