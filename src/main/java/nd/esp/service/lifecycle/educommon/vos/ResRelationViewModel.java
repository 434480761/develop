package nd.esp.service.lifecycle.educommon.vos;

import java.util.List;

import nd.esp.service.lifecycle.vos.valid.RelationsDefault;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResRelationViewModel {
	/**
	 * 源资源的标识id
	 */
	@NotBlank(message="{resourceViewModel.relations.source.notBlank.validmsg}",groups={RelationsDefault.class})
	private String source;
	
	/**
	 * 源资源的资源类型
	 */
	@NotBlank(message="{resourceViewModel.relations.sourceType.notBlank.validmsg}",groups={RelationsDefault.class})
	private String sourceType;
	

	
	/**
	 * 资源关系类型，关系类型，默认值是ASSOCIATE，可以为空
	 */
	@Length(message="{resourceViewModel.relations.relationType.maxlength.validmsg}",max=100, groups={RelationsDefault.class})
	private String relationType = "ASSOCIATE";
	
	/**
     * 关系标识
     */
    @Length(message="{resourceViewModel.relations.label.maxlength.validmsg}",max=100, groups={RelationsDefault.class})
    private String label;
	
	/**
	 * 资源关系的标签
	 */
	private List<String> tags;
	
	/**
     * 排序
     */
    private Integer orderNum;
	
	/**
	 * 资源关系的可用性
	 */
	private boolean enable = true;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
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

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }


}