
/**   
 * @Title: ResourceCategory.java 
 * @Package: com.nd.esp.repository.model 
 * @Description: TODO
 * @author Rainy(yang.lin)  
 * @date 2015年7月7日 下午12:00:17 
 * @version 1.3.1 
 */


package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

/** 
 * @Description 
 * @author Rainy(yang.lin)  
 * @date 2015年7月7日 下午12:00:17 
 * @version V1.0
 */
@Entity
@Table(name = "resource_categories")
@NoIndexBean
@NamedQueries({
    @NamedQuery(name="deleteResourceCategoryByResource",query="delete from ResourceCategory ti where primaryCategory=:rts AND resource=:resourceId"),
    @NamedQuery(name="commonQueryGetCategories", query="SELECT rc FROM ResourceCategory rc WHERE primaryCategory IN (:rts) AND resource IN  (:sids)"),
    @NamedQuery(name="batchGetCategories", query="SELECT ti FROM ResourceCategory ti WHERE primaryCategory=:rts AND resource IN  (:resIds)")
})
public class ResourceCategory extends EspEntity{
	@Column(name="resource")
	private String resource;
	
	private String taxonpath;
	
	private String taxoncode;
	
	private String taxonname;
	
	private String taxoncodeid;
	
	@Column( name = "short_name")
	private String shortName;
	
	@Column( name = "category_code")
	private String categoryCode;
	
	@Column( name = "category_name")
	private String categoryName;
	
    @Column(name="primary_category")
    private String primaryCategory;

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}
    /**
	 * Description 
	 * @return 
	 * @see com.nd.esp.repository.IndexMapper#getIndexType() 
	 */ 
		
	@Override
	public IndexSourceType getIndexType() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTaxonpath() {
		return taxonpath;
	}

	public void setTaxonpath(String taxonpath) {
		this.taxonpath = taxonpath;
	}

	public String getTaxoncode() {
		return taxoncode;
	}

	public void setTaxoncode(String taxoncode) {
		this.taxoncode = taxoncode;
	}

	public String getTaxonname() {
		return taxonname;
	}

	public void setTaxonname(String taxonname) {
		this.taxonname = taxonname;
	}

	public String getTaxoncodeid() {
		return taxoncodeid;
	}

	public void setTaxoncodeid(String taxoncodeid) {
		this.taxoncodeid = taxoncodeid;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
}
