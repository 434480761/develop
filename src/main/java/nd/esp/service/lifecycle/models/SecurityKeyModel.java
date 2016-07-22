package nd.esp.service.lifecycle.models;

import nd.esp.service.lifecycle.annotations.Column;

import java.sql.Timestamp;

/**
 * <p>Title: SecurityKeyModel</p>
 * <p>Description: SecurityKeyModel</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/21 </p>
 *
 * @author lanyl
 */
public class SecurityKeyModel {

	@Column(name = "identifier")
	private String identifier;

	@Column(name = "security_key")
	private String securityKey;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "update_time")
	protected Timestamp updateTime;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getSecurityKey() {
		return securityKey;
	}

	public void setSecurityKey(String securityKey) {
		this.securityKey = securityKey;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
