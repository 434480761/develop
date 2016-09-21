package nd.esp.service.lifecycle.repository.model.icrs;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import nd.esp.service.lifecycle.repository.EspEntity;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;

/**
 * ICRS同步错误记录表 仓储Model
 * @author xiezy
 * @date 2016年9月12日
 */
@Entity
@Table(name = "icrs_sync_error")
public class IcrsSyncErrorRecord extends EspEntity {
	private static final long serialVersionUID = -7688701576435426255L;

	@Column(name = "res_uuid")
	private String resUuid;
	@Column(name = "res_type")
	private String resType;
	@Column(name = "create_time")
	private BigDecimal createTime;
	@Column(name = "target")
	private String target;
	@Column(name = "error_message")
	private String errorMessage;

	public String getResUuid() {
		return resUuid;
	}

	public void setResUuid(String resUuid) {
		this.resUuid = resUuid;
	}

	public String getResType() {
		return resType;
	}

	public void setResType(String resType) {
		this.resType = resType;
	}

	public BigDecimal getCreateTime() {
		return createTime;
	}

	public void setCreateTime(BigDecimal createTime) {
		this.createTime = createTime;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public IndexSourceType getIndexType() {
		return null;
	}
}
