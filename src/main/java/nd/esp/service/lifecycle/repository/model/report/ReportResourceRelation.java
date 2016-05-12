package nd.esp.service.lifecycle.repository.model.report;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
@Entity
@Table(name="resource_relations")
public class ReportResourceRelation extends EspEntity {
	private static final long serialVersionUID = 1L;

	@Column(name="relation_type")
	private String relationType;
	
	@Column(name="res_type")
	private String resType;
	
	@Column(name="resource_target_type")
	private String resourceTargetType;
	
	@Column(name="source_uuid")
	private String sourceUuid;
	
	private String target;
	
	@Column(name="resource_create_time")
	private BigDecimal resourceCreateTime;
	
	@Column(name="target_create_time")
	private BigDecimal targetCreateTime;
	
	@Column(name = "last_update")
	private Timestamp lastUpdate;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	@Column(name="operation_flag")
	private String operationFlag;
	
	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public String getResourceTargetType() {
		return resourceTargetType;
	}

	public void setResourceTargetType(String resourceTargetType) {
		this.resourceTargetType = resourceTargetType;
	}

	public String getSourceUuid() {
		return sourceUuid;
	}

	public void setSourceUuid(String sourceUuid) {
		this.sourceUuid = sourceUuid;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public BigDecimal getResourceCreateTime() {
		return resourceCreateTime;
	}

	public void setResourceCreateTime(BigDecimal resourceCreateTime) {
		this.resourceCreateTime = resourceCreateTime;
	}

	public BigDecimal getTargetCreateTime() {
		return targetCreateTime;
	}

	public void setTargetCreateTime(BigDecimal targetCreateTime) {
		this.targetCreateTime = targetCreateTime;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
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
