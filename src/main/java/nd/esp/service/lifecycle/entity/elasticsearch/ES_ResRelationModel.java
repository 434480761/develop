package nd.esp.service.lifecycle.entity.elasticsearch;

import java.util.List;

public class ES_ResRelationModel {
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
