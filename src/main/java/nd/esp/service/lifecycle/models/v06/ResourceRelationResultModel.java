package nd.esp.service.lifecycle.models.v06;

import java.sql.Timestamp;

public class ResourceRelationResultModel {
    private String identifier;
    
    private String source;
    
    private String sourceTitle;
    
    private String target;
    
    private String targetTitle;
    
    private String relationType;
    
    private String label;
    
    private String resType;
    
    private String resourceTargetType;
    
    private String tags;
    
    private Integer orderNum;
    
    private Float sortNum;
    
    private String status;
    
    private Boolean enable;
    
    private String creator;
    
    private Timestamp createTime;
    
    private Timestamp lastUpdate;

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

    public String getResType() {
        return resType;
    }

    public void setResType(String resType) {
        this.resType = resType;
    }

    public String getResourceTargetType() {
        return resourceTargetType;
    }

    public void setResourceTargetType(String resourceTargetType) {
        this.resourceTargetType = resourceTargetType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Float getSortNum() {
        return sortNum;
    }

    public void setSortNum(Float sortNum) {
        this.sortNum = sortNum;
    }

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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
