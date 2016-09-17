package nd.esp.service.lifecycle.educommon.models;

import java.util.List;
/**
 * 描述资源和资源之间的关系模型
 * @author xuzy
 *
 */
public class ResRelationModel {
	/**
	 * 主键id
	 */
	private String identifier;
	/**
	 * 源资源的标识id
	 */
	private String source;
	
	/**
	 * 源资源的资源类型
	 */
	private String sourceType;
	
	/**
	 * 目标资源的标识id
	 */
	private String target;
	
	/**
	 * 目标资源的资源类型
	 */
	private String targetType;
	
	/**
	 * 资源关系类型，关系类型，默认值是ASSOCIATE，可以为空
	 */
	private String relationType = "ASSOCIATE";
	
	/**
     * 关系类型
     */
    private String label;
	
	/**
	 * 资源关系的标签
	 */
	private List<String> tags;
	
	/**
	 * 资源关系的可用性
	 */
	private boolean enable;
	
	/**
	 * 排序
	 */
	private Integer orderNum;

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

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((relationType == null) ? 0 : relationType.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResRelationModel other = (ResRelationModel) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (relationType == null) {
            if (other.relationType != null)
                return false;
        } else if (!relationType.equals(other.relationType))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
	
}
