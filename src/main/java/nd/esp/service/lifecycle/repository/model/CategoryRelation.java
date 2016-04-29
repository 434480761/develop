package nd.esp.service.lifecycle.repository.model;


import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.DataConverter;
import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
/**
 * 类描述:bean
 * 创建人:
 * 创建时间:2015-05-13 18:52:3
 * @version
 */
  
@Entity
@Table(name="category_relations")
public class CategoryRelation extends EspEntity {
	
	/**
	* 
	*/
	@Column(name="enable")
 	private Boolean enable; 

	/**
	* 
	*/
	@Column(name="level_parent")
 	private String levelParent; 
	/**
	* 
	*/
	@Column(name="order_num")
 	private Float orderNum; 
	/**
	* 
	*/
	@Column(name="pattern")
 	private String pattern; 
	/**
	* 
	*/
	@Column(name="pattern_path")
 	private String patternPath; 
	/**
	* 
	*/
	@Column(name="relation_type")
 	private String relationType; 
	/**
	* 
	*/
	@Column(name="source")
 	private String source; 

	/**
	* 
	*/
	@Column(name="target")
 	private String target; 
	
	@Column(name="tags")
	@DataConverter(target="tags",type=List.class)
	private String dbTags; 
	
	@Transient
	private List<String> tags; 
	
	

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return this.identifier;
	}
	public void setLevelParent(String levelParent) {
		this.levelParent = levelParent;
	}
	
	public String getLevelParent() {
		return this.levelParent;
	}
	public void setOrderNum(Float orderNum) {
		this.orderNum = orderNum;
	}
	
	public Float getOrderNum() {
		return this.orderNum;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
	public String getPattern() {
		return this.pattern;
	}
	public void setPatternPath(String patternPath) {
		this.patternPath = patternPath;
	}
	
	public String getPatternPath() {
		return this.patternPath;
	}
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	
	public String getRelationType() {
		return this.relationType;
	}
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getSource() {
		return this.source;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getTarget() {
		return this.target;
	}

	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.CategoryRelationType;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public String getDbTags() {
		return dbTags;
	}

	public void setDbTags(String dbTags) {
		this.dbTags = dbTags;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
}