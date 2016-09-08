package nd.esp.service.lifecycle.vos.educationrelation.v06;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nd.esp.service.lifecycle.vos.valid.CreateEducationRelationDefault;
import nd.esp.service.lifecycle.vos.valid.UpdateEducationRelationDefault;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class EducationRelationViewModel {
    /**
     * 资源关系id
     */
    private String identifier;
    /**
     * 源资源id
     */
    private String source;
    /**
     * 源资源标题
     */
    private String sourceTitle;
    /**
     * 目标资源id
     */
    @NotBlank(message = "{educationRelationModel.target.notBlank.validmsg}", groups = { CreateEducationRelationDefault.class })
    private String target;
    /**
     * 目标资源标题
     */
    private String targetTitle;
    /**
     * 关系类型
     */
    private String relationType;
    /**
     * 关系标识
     */
    @Length(message="{educationRelationModel.label.maxlength.validmsg}",max=100, groups={ UpdateEducationRelationDefault.class, CreateEducationRelationDefault.class })
    private String label;
    /**
     * 关系标签
     */
    private List<String> tags;
    /**
     * 关系的顺序值
     */
    //@NotNull(message = "{educationRelationModel.orderNum.notNull.validmsg}", groups = { UpdateEducationRelationDefault.class, CreateEducationRelationDefault.class })
    private Integer orderNum;
    
    /**
     * 目标资源的类型
     */
    @NotBlank(message="{educationRelationModel.resourceTargetType.notBlank.validmsg}", groups = { CreateEducationRelationDefault.class })
    @JsonInclude(Include.NON_NULL)
    private String resourceTargetType;
    
    /**
     * 资源关系生命周期
     */
    @Valid
    @JsonInclude(Include.NON_NULL)
    private EducationRelationLifeCycleViewModel lifeCycle;
    
    public EducationRelationLifeCycleViewModel getLifeCycle() {
        return lifeCycle;
    }
    public void setLifeCycle(EducationRelationLifeCycleViewModel lifeCycle) {
        this.lifeCycle = lifeCycle;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getSourceTitle() {
        return sourceTitle;
    }
    public void setSourceTitle(String sourceTitle) {
        this.sourceTitle = sourceTitle;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getTargetTitle() {
        return targetTitle;
    }
    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }
    public String getRelationType() {
        return relationType;
    }
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public Integer getOrderNum() {
        return orderNum;
    }
    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
    public String getResourceTargetType() {
        return resourceTargetType;
    }
    public void setResourceTargetType(String resourceTargetType) {
        this.resourceTargetType = resourceTargetType;
    }

}
