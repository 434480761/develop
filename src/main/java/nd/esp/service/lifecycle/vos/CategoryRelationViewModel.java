package nd.esp.service.lifecycle.vos;

import java.util.List;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 维度数据关系viewModel
 * <br>Created 2015年4月21日 下午10:27:42
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class CategoryRelationViewModel {
	/**
	 * uuid
	 */
	private String identifier;
	/**
	 * 关系源对象标识
	 */
	@NotBlank(message="{categoryRelationViewModel.source.notBlank.validmsg}")
	private String source;
	/**
	 * 关系指向的目标对象标识
	 */
	@NotBlank(message="{categoryRelationViewModel.target.notBlank.validmsg}")
	private String target;
	/**
	 * 关系类型
	 */
	@NotBlank(message="{categoryRelationViewModel.relationType.notBlank.validmsg}")
	private String relationType;
	/**
	 * 关系标签
	 */
	private List<String> tags;
	/**
	 * 关系的顺序值，来源于维度中的顺序值
	 */
	private float orderNum;
	/**
	 * 是否可用
	 */
	private boolean enable;
	/**
	 * 分类的编码路径
	 */
	private String patternPath;
	/**
	 * 分类维度应用模式的uuid
	 */
	private String pattern;
	/**
	 * 维度模式中，同级别数据的父节点ndCode
	 */
	private String levelParent;
	
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
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getRelationType() {
		return relationType;
	}
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public float getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(float orderNum) {
		this.orderNum = orderNum;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public String getPatternPath() {
		return patternPath;
	}
	public void setPatternPath(String patternPath) {
		this.patternPath = patternPath;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getLevelParent() {
		return levelParent;
	}
	public void setLevelParent(String levelParent) {
		this.levelParent = levelParent;
	}

}
