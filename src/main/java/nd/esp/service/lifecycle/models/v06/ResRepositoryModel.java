package nd.esp.service.lifecycle.models.v06;

import java.util.Date;
/**
 * 资源物理存储空间的Model
 * <p>Create Time: 2015年7月16日           </p>
 * @author xiezy
 */
public class ResRepositoryModel {
    /**
     * 私有资源库的id
     */
    private String identifier;
    /**
     * 私有资源库的名称
     */
    private String repositoryName;
    /**
     * 存储空间的类型，Org代表组织机构
     */
    private String targetType;
    /**
     * 目标的标识
     */
    private String target;
    /**
     * 存储空间的管理者
     */
    private String repositoryAdmin;
    /**
     * 私有库是否可用
     */
    private Boolean enable;
    /**
     * 私有库的状态信息
     */
    private String status;
    /**
     * 私有库的创建时间
     */
    private Date createTime;
    /**
     *  存储路径 
     */
    private String repositoryPath;
    
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getRepositoryName() {
        return repositoryName;
    }
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    public String getTargetType() {
        return targetType;
    }
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getRepositoryAdmin() {
        return repositoryAdmin;
    }
    public void setRepositoryAdmin(String repositoryAdmin) {
        this.repositoryAdmin = repositoryAdmin;
    }
    public Boolean getEnable() {
        return enable;
    }
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public String getRepositoryPath() {
        return repositoryPath;
    }
    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }
}
