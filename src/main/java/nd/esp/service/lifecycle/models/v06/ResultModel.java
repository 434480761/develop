package nd.esp.service.lifecycle.models.v06;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ResultModel {
    /**
     * 目标资源的id
     */
    private String identifier;
    /**
     * 目标资源的名称
     */
    private String title;
    /**
     * 目标资源的描述
     */
    private String description;
    /**
     * 目标资源的标签
     */
    private String tags;
    /**
     * 目标资源的关键字
     */
    private String keywords;
    /**
     * 资源关系的id
     */
    private String relationId;
    /**
     * 资源关系类型
     */
    private String relationType;
    /**
     * 资源关系标识
     */
    private String label;
    /**
     * 资源关系顺序
     */
    private Integer orderNum;
    /**
     * 资源关系是否可用
     */
    private boolean enable;
    /**
     * 资源关系的标签
     */
    private String relationTags;
    /**
     * 目标资源的预览地址
     */
    private String preview;
    /**
     * 目标资源类型
     * add by xuzy 2015-6-11
     */
    @JsonIgnore
    private String targetType;
    /**
     * 源资源id
     */
    @JsonInclude(Include.NON_NULL)
    private String sid;
    
    @JsonIgnore
    private String creator;
    
    @JsonIgnore
    private String status;
    
    @JsonIgnore
    private Date createTime;
    
    @JsonIgnore
    private Date lastUpdate;
    
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    public String getRelation_id() {
        return relationId;
    }
    public void setRelation_id(String relationId) {
        this.relationId = relationId;
    }
    public String getRelation_type() {
        return relationType;
    }
    public void setRelation_type(String relationType) {
        this.relationType = relationType;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public Integer getOrder_num() {
        return orderNum;
    }
    public void setOrder_num(Integer orderNum) {
        this.orderNum = orderNum;
    }
    public boolean isEnable() {
        return enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    public String getRelation_tags() {
        return relationTags;
    }
    public void setRelation_tags(String relationTags) {
        this.relationTags = relationTags;
    }
    public String getTarget_type() {
        return targetType;
    }
    public void setTarget_type(String targetType) {
        this.targetType = targetType;
    }
    public String getPreview() {
        return preview;
    }
    public void setPreview(String preview) {
        this.preview = preview;
    }
    public String getSource_uuid() {
        return sid;
    }
    public void setSource_uuid(String sid) {
        this.sid = sid;
    }
    public String getCreator() {
        return creator;
    }
    public void setCreator(String creator) {
        this.creator = creator;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Date getCreate_time() {
        return createTime;
    }
    public void setCreate_time(Date createTime) {
        this.createTime = createTime;
    }
    public Date getLast_update() {
        return lastUpdate;
    }
    public void setLast_update(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
