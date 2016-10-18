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
@Table(name="category_patterns")
public class CategoryPattern extends EspEntity {
	
	
	
	/** @Fields serialVersionUID: */
	  	
	@Column(name="pattern_name")
 	private String patternName; 
	/**
	* 
	*/
	@Column(name="purpose")
 	private String purpose; 
	/**
	* 
	*/
	@Column(name="scope")
 	private String scope; 
	/**
	* 国际化编码
	*/
	@Column(name="gb_code")
 	private String gbCode; 

	@Column(name="pattern_path")
	private String patternPath;
	
	@Column
	private String segment;
	
	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}
	
	public String getPatternName() {
		return this.patternName;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	
	public String getPurpose() {
		return this.purpose;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public String getScope() {
		return this.scope;
	}

	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.CategoryPatternType;
	}

	public String getPatternPath() {
		return patternPath;
	}

	public void setPatternPath(String patternPath) {
		this.patternPath = patternPath;
	}

	public String getSegment() {
		return segment;
	}

	public void setSegment(String segment) {
		this.segment = segment;
	}
	
	public String getGbCode() {
		return gbCode;
	}

	public void setGbCode(String gbCode) {
		this.gbCode = gbCode;
	}
	
	
}