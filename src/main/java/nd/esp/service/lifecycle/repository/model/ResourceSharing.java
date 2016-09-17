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
 * 资源分享 仓储Model
 * @author xiezy
 * @date 2016年8月24日
 */
@Entity
@Table(name="resources_sharing")
public class ResourceSharing extends EspEntity {

	private static final long serialVersionUID = 3751260153974321825L;

	@Column(name = "res_type")
	private String resourceType;
	
	@Column(name = "resource")
	private String resource;
	
	@Column(name = "protect_passwd")
	private String protectPasswd;
	
	@Column(name = "sharer_id")
	private String sharerId;
	
	@Column(name = "sharer_name")
	private String sharerName;
	
	@Transient
	private Timestamp sharingTime;
	
	@Column(name = "sharing_time")
	private BigDecimal dbsharingTime;
	
	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getProtectPasswd() {
		return protectPasswd;
	}

	public void setProtectPasswd(String protectPasswd) {
		this.protectPasswd = protectPasswd;
	}

	public String getSharerId() {
		return sharerId;
	}

	public void setSharerId(String sharerId) {
		this.sharerId = sharerId;
	}

	public String getSharerName() {
		return sharerName;
	}

	public void setSharerName(String sharerName) {
		this.sharerName = sharerName;
	}

	public Timestamp getSharingTime() {
		if (this.dbsharingTime != null) {
			this.sharingTime = new Timestamp(dbsharingTime.longValue());
		}
		return sharingTime;
	}

	public void setSharingTime(Timestamp sharingTime) {
		this.sharingTime = sharingTime;
		if (this.sharingTime != null) {
			this.dbsharingTime = new BigDecimal(sharingTime.getTime());
		}
	}

	public BigDecimal getDbsharingTime() {
		return dbsharingTime;
	}

	public void setDbsharingTime(BigDecimal dbsharingTime) {
		this.dbsharingTime = dbsharingTime;
	}
	
	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}