package nd.esp.service.lifecycle.educommon.models;

/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResClassificationModel implements Comparable<ResClassificationModel>{

	/**
	 * 标识
	 */
	private String identifier;
	
	/**
	 * 资源id
	 */
	private String resourceId;
	
	
	/**
	 * 分类路径
	 */
	private String taxonpath;
	/**
	 * 分类体系维度的编码
	 */
	private String taxonname;
	/**
	 * 分类体系维度数据的编码
	 */
	private String taxoncode;
	/**
	 * 分类维度数据的id
	 */
	private String taxoncodeId;
	private String shortName;
	private String categoryCode;
	private String categoryName;

	public ResClassificationModel(){

	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTaxonpath() {
		return taxonpath;
	}

	public void setTaxonpath(String taxonpath) {
		this.taxonpath = taxonpath;
	}

	public String getTaxonname() {
		return taxonname;
	}

	public void setTaxonname(String taxonname) {
		this.taxonname = taxonname;
	}

	public String getTaxoncode() {
		return taxoncode;
	}

	public void setTaxoncode(String taxoncode) {
		this.taxoncode = taxoncode;
	}

	public String getTaxoncodeId() {
		return taxoncodeId;
	}

	public void setTaxoncodeId(String taxoncodeId) {
		this.taxoncodeId = taxoncodeId;
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

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((taxoncode == null) ? 0 : taxoncode.hashCode());
		result = prime * result
				+ ((taxonpath == null) ? 0 : taxonpath.hashCode());
		return result;
	}
	
	/**
	 * 如果taxonpath与taxoncode两个值一样，则认为是相同
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResClassificationModel other = (ResClassificationModel) obj;
		if (taxoncode == null) {
			if (other.taxoncode != null)
				return false;
		} else if (!taxoncode.equals(other.taxoncode))
			return false;
		if (taxonpath == null) {
			if (other.taxonpath != null)
				return false;
		} else if (!taxonpath.equals(other.taxonpath))
			return false;
		return true;
	}
	


	@Override
	public int compareTo(ResClassificationModel o) {
		if(o == null){
			return -1;
		}
		return this.getTaxoncode().compareTo(o.getTaxoncode());
	}
}