package nd.esp.service.lifecycle.repository.model.categorysync;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

@Entity
@Table(name = "category_sync")
public class CategorySync extends EspEntity {

	private static final long serialVersionUID = 7133171030599547863L;

	@Column(name = "sync_category")
	private String syncCategory;
	@Column(name = "category_type")
	private Integer categoryType;
	@Column(name = "operation_type")
	private Integer operationType;

	@Transient
	private Timestamp operationTime;
	@Column(name = "operation_time")
	private BigDecimal dboperationTime;
	
	@Column(name = "handle")
	private Integer handle;

	public String getSyncCategory() {
		return syncCategory;
	}

	public void setSyncCategory(String syncCategory) {
		this.syncCategory = syncCategory;
	}

	public Integer getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(Integer categoryType) {
		this.categoryType = categoryType;
	}

	public Integer getOperationType() {
		return operationType;
	}

	public void setOperationType(Integer operationType) {
		this.operationType = operationType;
	}

	public Timestamp getOperationTime() {
		if (this.dboperationTime != null) {
			this.operationTime = new Timestamp(dboperationTime.longValue());
		}
		return operationTime;
	}

	public void setOperationTime(Timestamp operationTime) {
		this.operationTime = operationTime;
		if (this.operationTime != null) {
			this.dboperationTime = new BigDecimal(operationTime.getTime());
		}
	}

	public BigDecimal getDboperationTime() {
		return dboperationTime;
	}

	public void setDboperationTime(BigDecimal dboperationTime) {
		this.dboperationTime = dboperationTime;
	}

	public Integer getHandle() {
		return handle;
	}

	public void setHandle(Integer handle) {
		this.handle = handle;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}
