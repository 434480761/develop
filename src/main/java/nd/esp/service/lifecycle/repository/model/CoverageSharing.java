package nd.esp.service.lifecycle.repository.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * 库分享 仓储Model
 * @author xiezy
 * @date 2016年8月24日
 */
@Entity
@Table(name="coverages_sharing")
public class CoverageSharing extends EspEntity {

	private static final long serialVersionUID = -4227054282966422737L;
	
	@Column(name = "source_coverage")
	private String sourceCoverage;
	
	@Column(name = "target_coverage")
	private String targetCoverage;
	
	@Transient
	private Timestamp createTime;
	
	@Column(name = "create_time")
	private BigDecimal dbcreateTime;
	
	@Column(name = "creator")
	private String creator;
	
	public Timestamp getCreateTime() {
		if(this.dbcreateTime != null){
			this.createTime = new Timestamp(dbcreateTime.longValue());
		} 
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
		if(this.createTime != null){
			this.dbcreateTime = new BigDecimal(createTime.getTime());
		}
	}
	
	public BigDecimal getDbcreateTime() {
		return dbcreateTime;
	}

	public void setDbcreateTime(BigDecimal dbcreateTime) {
		this.dbcreateTime = dbcreateTime;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public String getSourceCoverage() {
		return sourceCoverage;
	}

	public void setSourceCoverage(String sourceCoverage) {
		this.sourceCoverage = sourceCoverage;
	}

	public String getTargetCoverage() {
		return targetCoverage;
	}

	public void setTargetCoverage(String targetCoverage) {
		this.targetCoverage = targetCoverage;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}