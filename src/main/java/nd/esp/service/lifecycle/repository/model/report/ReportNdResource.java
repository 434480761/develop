package nd.esp.service.lifecycle.repository.model.report;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

@Entity
@Table(name="ndresource")
public class ReportNdResource extends EspEntity{
	private static final long serialVersionUID = 1L;
	
	@Column(name="estatus")
	private String status;
	
	@Column(name = "last_update")
	private BigDecimal lastUpdate;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name="primary_category")
	private String primaryCategory;
	
	@Column(name="enable")
	private boolean enable;
	
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

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
	
}
