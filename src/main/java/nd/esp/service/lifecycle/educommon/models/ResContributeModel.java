package nd.esp.service.lifecycle.educommon.models;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import nd.esp.service.lifecycle.support.annotation.Reg;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * 生命周期的管理过程中，对资源有所操作的所有的相关记录 什么人在什么时间，以什么角色对资源做了什么操作
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResContributeModel {

	private String identifier;
	@JsonIgnore
	private String title;
	private String roleName;
	private String roleId;
	private ResLifeCycleModel lifeCycle;
	@NotBlank(message="{resContributeModel.target_name.notBlank.validmsg}")
    @Length(message="{resContributeModel.target_name.maxLength.validmsg}",max=250)
	private String targetName;
	@NotBlank(message="{resContributeModel.target_type.notBlank.validmsg}")
    @Length(message="{resContributeModel.target_type.maxLength.validmsg}",max=250)
	private String targetType;
	/**
	 * 操作时间
	 */
	private Date contributeTime;
	@NotBlank(message="{resContributeModel.target_id.notBlank.validmsg}")
    @Length(message="{resContributeModel.target_id.maxLength.validmsg}",max=250)
    private String targetId;
	@NotBlank(message="{resContributeModel.message.notBlank.validmsg}")
    private String message;
	/**
	 * 生命周期的状态值
	 */
	@NotBlank(message="{resContributeModel.status.notBlank.validmsg}")
    @Reg(message = "{resContributeModel.status.reg.validmsg}", pattern = "^(CREATING|CREATED|EDITING|EDITED|TRANSCODE_WAITING|TRANSCODING|TRANSCODED|TRANSCODE_ERROR|AUDIT_WAITING|AUDITING|AUDITED|PUBLISH_WAITING|PUBLISHING|PUBLISHED|ONLINE|OFFLINE|AUDIT_REJECT|REMOVED|CREATE|INIT|TRANSCODE|AUDIT|REJECT)$", isNullValid = false)
    private String lifecycleStatus;
	/**
	 * 生命周期每个时期的进度
	 */
	@NotNull(message="{resContributeModel.process.notNull.validmsg}")
    private float process;
	
	private List<String> resources;

	public ResContributeModel(){

	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public ResLifeCycleModel getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(ResLifeCycleModel lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public Date getContributeTime() {
		return contributeTime;
	}

	public void setContributeTime(Date contributeTime) {
		this.contributeTime = contributeTime;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getLifecycleStatus() {
		return lifecycleStatus;
	}

	public void setLifecycleStatus(String lifecycleStatus) {
		this.lifecycleStatus = lifecycleStatus;
	}

	public float getProcess() {
		return process;
	}

	public void setProcess(float process) {
		this.process = process;
	}

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}