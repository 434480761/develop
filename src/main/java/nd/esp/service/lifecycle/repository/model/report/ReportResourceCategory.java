package nd.esp.service.lifecycle.repository.model.report;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
@Entity
@Table(name="resource_categories")
public class ReportResourceCategory extends EspEntity{
	private static final long serialVersionUID = 1L;
	
	private String resource;
	
	private String taxOnCode;
	
	@Column(name="category_name")
	private String categoryName;
	
	@Column(name = "last_update")
	private BigDecimal lastUpdate;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name="operation_flag")
	private String operationFlag;
	
	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getTaxOnCode() {
		return taxOnCode;
	}

	public void setTaxOnCode(String taxOnCode) {
		this.taxOnCode = taxOnCode;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public BigDecimal getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(BigDecimal lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getOperationFlag() {
		return operationFlag;
	}


	public void setOperationFlag(String operationFlag) {
		this.operationFlag = operationFlag;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

}
