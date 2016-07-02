package nd.esp.service.lifecycle.models;

import nd.esp.service.lifecycle.annotations.Column;

import java.sql.Timestamp;

/**
 * <p>Title: UserCoverageMappingModel</p>
 * <p>Description: UserCoverageMappingModel</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/29 </p>
 *
 * @author lanyl
 */
public class UserCoverageMappingModel {
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id")
	private String userId;

	@Column(name = "coverage")
	private String coverage;

	@Column(name = "create_time")
	private Timestamp createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCoverage() {
		return coverage;
	}

	public void setCoverage(String coverage) {
		this.coverage = coverage;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
}
