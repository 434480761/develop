package nd.esp.service.lifecycle.repository.model.report;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
@Entity
@Table(name="categorys")
//title
public class ReportCategory extends EspEntity {
	private static final long serialVersionUID = 1L;
	
	@Column(name="short_name")
	private String shortName;
	
	@Column(name = "last_update")
	private BigDecimal lastUpdate;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name="operation_flag")
	private String operationFlag;
	
	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
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
