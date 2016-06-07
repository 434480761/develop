package nd.esp.service.lifecycle.vos.chapters.v06;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * 06章节viewModel
 * <p>Create Time: 2015年8月3日           </p>
 * @author xiezy
 */
@JsonInclude(Include.NON_NULL)
public class ChapterViewModel {
    /**
     * 章节的id
     */
    private String identifier;
    /**
     * 章节标题
     */
    @NotBlank(message="{chapterViewModel.title.notBlank.validmsg}")
    private String title;
    /**
     * 章节描述
     */
    @NotBlank(message="{chapterViewModel.description.notBlank.validmsg}")
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
    
    /**
     * 标签
     */
    private List<String> tags;
    
    /**
     * 创建时间只用于查询
     */
    @JsonIgnore
    private BigDecimal dbcreateTime;
    
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
	public BigDecimal getDbcreateTime() {
		return dbcreateTime;
	}
	public void setDbcreateTime(BigDecimal dbcreateTime) {
		this.dbcreateTime = dbcreateTime;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
