package nd.esp.service.lifecycle.models.chapter.v06;

/**
 * 06章节Model
 * <p>Create Time: 2015年8月4日           </p>
 * @author xiezy
 */
public class ChapterModel {
    /**
     * 章节的id
     */
    private String identifier;
    /**
     * 章节标题
     */
    private String title;
    /**
     * 章节描述
     */
    private String description;
    /**
     * 父级节点
     */
    private String parent;
    /**
     * 所属教材的id
     */
    private String teachingMaterial;
    /**
     * 目标参照章节的id,如果不填写，默认增加在同级节点的最末一个节点
     */
    private String target;
    /**
     * 方向，默认值是next
     */
    private String direction;
    
    
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
    public String getParent() {
        return parent;
    }
    public void setParent(String parent) {
        this.parent = parent;
    }
    public String getTeachingMaterial() {
        return teachingMaterial;
    }
    public void setTeachingMaterial(String teachingMaterial) {
        this.teachingMaterial = teachingMaterial;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }
}
