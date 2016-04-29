package nd.esp.service.lifecycle.repository.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.solr.client.solrj.beans.Field;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 类描述:bean
 * 创建人:
 * 创建时间:2015-05-13 18:52:3
 * @version
 */
  
@Entity
@Table(name="categorys")
public class Category extends EspEntity {
	
	
	
	/** @Fields serialVersionUID: */
	  	
	private static final long serialVersionUID = 1L;

	/**
	* 
	*/

	@Column(name="gb_code")
 	private String gbCode; 
	
	/**
	* 
	*/
	@Column(name="nd_code")
 	private String ndCode; 
	/**
	* 
	*/
	@Column(name="purpose")
 	private String purpose; 
	/**
	* 
	*/
	@Column(name="short_name")
 	private String shortName; 
	/**
	* 
	*/
	@Column(name="source")
 	private String source; 
	

	
	public void setGbCode(String gbCode) {
		this.gbCode = gbCode;
	}
	
	public String getGbCode() {
		return this.gbCode;
	}
	
	public void setNdCode(String ndCode) {
		this.ndCode = ndCode;
	}
	
	public String getNdCode() {
		return this.ndCode;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	
	public String getPurpose() {
		return this.purpose;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getShortName() {
		return this.shortName;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return this.source;
	}

	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.CategoryType;
	}
}