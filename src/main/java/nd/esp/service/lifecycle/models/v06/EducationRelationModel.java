package nd.esp.service.lifecycle.models.v06;

import java.math.BigDecimal;
import java.util.List;

public class EducationRelationModel {
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
    private String label;
    /**
     * 关系标签
     */
    private List<String> tags;
    /**
     * 关系的顺序值
     */
    private Integer orderNum;
    
    /**
     * 目标资源的类型
     */
    private String resourceTargetType;
    
    /**
     * 源资源的类型
     */
    private String resType;
	
	private BigDecimal targetCT;
    
    /**
     * 资源关系生命周期
     */
    private EducationRelationLifeCycleModel lifeCycle;
    
    public EducationRelationLifeCycleModel getLifeCycle() {
        return lifeCycle;
    }
    public void setLifeCycle(EducationRelationLifeCycleModel lifeCycle) {
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
    public String getResType() {
        return resType;
    }
    public void setResType(String resType) {
        this.resType = resType;
    }
	public BigDecimal getTargetCT() {
		return targetCT;
	}
	public void setTargetCT(BigDecimal targetCT) {
		this.targetCT = targetCT;
	}

    
    
}
