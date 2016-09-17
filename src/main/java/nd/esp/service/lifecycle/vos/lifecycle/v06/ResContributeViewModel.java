package nd.esp.service.lifecycle.vos.lifecycle.v06;

import java.util.Date;


/**
 * 生命周期的管理过程中，对资源有所操作的所有的相关记录 什么人在什么时间，以什么角色对资源做了什么操作
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:50
 */
public class ResContributeViewModel {

    private String identifier;
    private String roleName;
    private String roleId;
    private String targetName;
    private String targetType;
    /**
     * 操作时间
     */
    private Date contributeTime;
    private String targetId;
    private String message;
    /**
     * 生命周期的状态值
     */
    private String lifecycleStatus;
    /**
     * 生命周期每个时期的进度
     */
    private float process;

    public ResContributeViewModel(){

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
}