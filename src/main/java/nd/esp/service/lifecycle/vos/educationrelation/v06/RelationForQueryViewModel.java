package nd.esp.service.lifecycle.vos.educationrelation.v06;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 关系目标检索的viewModel
 * <p>Create Time: 2015年5月18日           </p>
 * @author xiezy
 */
public class RelationForQueryViewModel {
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
    private int orderNum;
    /**
     * 资源关系是否可用
     */
    private boolean enable;
    /**
     * 资源关系的标签
     */
    private List<String> relationTags;
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
     * 目标资源的预览地址
     */
    private Map<String,String> preview;
    /**
     * 目标资源的标签
     */
    private List<String> tags;
    /**
     * 目标资源的关键字
     */
    private List<String> keywords;
    /**
     * 源资源id
     */
    @JsonInclude(Include.NON_NULL)
    private String sid;
    
    /**
     * 目标资源类型
     * add by xuzy 2015-6-11
     */
    @JsonIgnore
    private String targetType;
    
    @JsonIgnore
    private String creator;
    
    @JsonIgnore
    private String status;
    
    @JsonIgnore
    private Date createTime;
    
    @JsonIgnore
    private Date lastUpdate;
    
    public String getRelationId() {
        return relationId;
    }
    public void setRelationId(String relationId) {
        this.relationId = relationId;
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
    public int getOrderNum() {
        return orderNum;
    }
    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }
    public boolean isEnable() {
        return enable;
    }
    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    public List<String> getRelationTags() {
        return relationTags;
    }
    public void setRelationTags(List<String> relationTags) {
        this.relationTags = relationTags;
    }
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
    public Map<String, String> getPreview() {
        return preview;
    }
    public void setPreview(Map<String, String> preview) {
        this.preview = preview;
    }
    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    public List<String> getKeywords() {
        return keywords;
    }
    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    public String getSid() {
        return sid;
    }
    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getTargetType() {
        return targetType;
    }
    public void setTargetType(String targetType) {
        this.targetType = targetType;
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
