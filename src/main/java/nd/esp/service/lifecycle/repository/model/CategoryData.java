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
@Table(name="category_datas")
public class CategoryData extends EspEntity {
	
	@Column(name="category")
 	private String category; 
	
	@Column(name="dimension_path")
 	private String dimensionPath; 
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
	@Column(name="order_num")
 	private Integer orderNum; 
	/**
	* 
	*/
	@Column(name="parent")
 	private String parent; 
	/**
	* 
	*/
	@Column(name="short_name")
 	private String shortName; 
	
	/**
	 * 预览图
	 */
	private String preview;

	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	public void setDimensionPath(String dimensionPath) {
		this.dimensionPath = dimensionPath;
	}
	
	public String getDimensionPath() {
		return this.dimensionPath;
	}
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
	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}
	
	public Integer getOrderNum() {
		return this.orderNum;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	public String getParent() {
		return this.parent;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getShortName() {
		return this.shortName;
	}

	@Override
	public IndexSourceType getIndexType() {
		return IndexSourceType.CategoryDataType;
	}

	public String getPreview() {
		return preview;
	}

	public void setPreview(String preview) {
		this.preview = preview;
	}
	
}