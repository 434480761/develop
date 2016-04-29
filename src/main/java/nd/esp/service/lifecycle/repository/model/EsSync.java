package nd.esp.service.lifecycle.repository.model;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.index.NoIndexBean;

@NoIndexBean
@Entity
@Table(name = "es_sync")
public class EsSync extends EspEntity {

	private static final long serialVersionUID = 958059725763295454L;

	// 资源uuid
	@Column(name = "resource")
	private String resource;

	// 资源主分类
	@Column(name = "primary_category")
	private String primaryCategory;
	// 0表示删除，1表示更新
	@Column(name = "sync_type")
	private Boolean syncType;
	// 0表示已处理，1表示待处理
	@Column(name = "enable")
	private Boolean enable;

	// 同步记录创建时间
	@Column(name = "create_time")
	private BigDecimal createTime;
	// 同步记录最后更新时间
	@Column(name = "last_update")
	private BigDecimal lastUpdate;
	// 已经尝试同步的次数
	@Column(name = "try_times")
	private Integer tryTimes;

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	public Boolean getSyncType() {
		return syncType;
	}

	public void setSyncType(Boolean syncType) {
		this.syncType = syncType;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public BigDecimal getCreateTime() {
		return createTime;
	}

	public void setCreateTime(BigDecimal createTime) {
		this.createTime = createTime;
	}

	public BigDecimal getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(BigDecimal lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Integer getTryTimes() {
		return tryTimes;
	}

	public void setTryTimes(Integer tryTimes) {
		this.tryTimes = tryTimes;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}

}
