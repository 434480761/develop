/**   
 * @Title: EspEntity.java 
 * @Package: com.nd.esp.repository 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年5月26日 下午1:55:19 
 * @version 1.3.1 
 */

package nd.esp.service.lifecycle.repository;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.exception.EspStoreException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

/**
 * @Description
 * @author Rainy(yang.lin)
 * @date 2015年5月26日 下午1:55:19
 * @version V1.0
 */
@MappedSuperclass
public abstract class EspEntity implements Serializable, IndexMapper {

	/** @Fields serialVersionUID: */

	public static final String PROP_IDENTIFIER = "identifier";

	@Transient
	private Map<String, Object> searchables = Maps.newHashMap();

	@Id
	protected String identifier;

	protected String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	protected String title;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	/** The type. */
	@Transient
	@JsonIgnore
	protected int indexType = 1000;

	/** The type. */
	@Transient
	@JsonIgnore
	protected int indexSubType = 1000;

	// 记录状态
	// 1、表示删除
	// @Field("record_status")
	//@Column(name = "record_status", columnDefinition = "int default 0")
//	protected int recordStatus = 0;
//
//	public int getRecordStatus() {
//		return recordStatus;
//	}
//
//	public void setRecordStatus(int recordStatus) {
//		this.recordStatus = recordStatus;
//	}

	public EspEntity() {
		if(getIndexType() !=null){
			this.indexType = getIndexType().getType();
			this.indexSubType = getIndexType().getSubtype();
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the identifier.
	 *
	 * @param identifier
	 *            the new identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public Map<String, Object> getAdditionSearchFields()
			throws EspStoreException {
		return searchables;
	}
}
