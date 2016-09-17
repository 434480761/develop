package nd.esp.service.lifecycle.entity.elasticsearch;

public class ES_ResClassificationModel {
	private String identifier;

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

}
