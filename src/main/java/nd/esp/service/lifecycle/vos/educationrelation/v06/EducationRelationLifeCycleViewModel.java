package nd.esp.service.lifecycle.vos.educationrelation.v06;

import java.util.Date;

import javax.validation.constraints.NotNull;

import nd.esp.service.lifecycle.support.annotation.Reg;
import nd.esp.service.lifecycle.vos.valid.CreateEducationRelationDefault;
import nd.esp.service.lifecycle.vos.valid.UpdateEducationRelationDefault;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 资源关系生命周期（V06增加）
 * 
 * @author caocr
 *
 */
public class EducationRelationLifeCycleViewModel {
    @NotBlank(message="{educationRelationModel.lifeCycle.status.notBlank.validmsg}", groups = { UpdateEducationRelationDefault.class, CreateEducationRelationDefault.class})
    @Reg(message = "{educationRelationModel.lifeCycle.status.reg.validmsg}", pattern = "^(AUDIT_WAITING|AUDITING|AUDITED|AUDIT_REJECT|REMOVED)$", isNullValid = false, groups = { UpdateEducationRelationDefault.class, CreateEducationRelationDefault.class})
    private String status;
    
    @NotNull(message="{educationRelationModel.lifeCycle.enable.notNull.validmsg}", groups = { CreateEducationRelationDefault.class })
    private Boolean enable;
    
    @NotBlank(message="{educationRelationModel.lifeCycle.creator.notBlank.validmsg}", groups = { CreateEducationRelationDefault.class })
    private String creator;
    
    private Date createTime;
    
    private Date lastUpdate;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getEnable() {
        return enable;
    }
    
    public void setEnable(Boolean enable) {
        this.enable = enable;
    }
    
    public String getCreator() {
        return creator;
    }
    
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
