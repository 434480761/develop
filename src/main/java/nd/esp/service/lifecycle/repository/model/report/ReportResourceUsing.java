package nd.esp.service.lifecycle.repository.model.report;

import java.math.BigDecimal;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
@Entity
@Table(name="resource_using")
public class ReportResourceUsing extends EspEntity{
	private static final long serialVersionUID = 1L;

	@Column(name="resource_id")
	private String resourceId;
	
	@Column(name="user_id")
	private String userId;
	
	@Column(name="real_name")
	private String realName;
	
	@Column(name="org_id")
	private String orgId;
	
	@Column(name="org_name")
	private String orgName;
	
	@Column(name="biz_sys")
	private String bizSys;
	
	@Column(name = "last_update")
	private BigDecimal lastUpdate;
	
	@Column(name = "create_time")
	private Timestamp createTime;
	
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getBizSys() {
		return bizSys;
	}

	public void setBizSys(String bizSys) {
		this.bizSys = bizSys;
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

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

}
