package nd.esp.service.lifecycle.models;

import java.util.List;


/**
 * 关系模型
 * @author linsm
 * @version 0.3
 * @created 20-4月-2015 15:26:26
 */
public class RelationModel<T> {

	/**
	 * 主键
	 */
	private String identifier;
	/**
	 * 源
	 */
	private T source;
	/**
	 * 目标
	 */
	private T target;
	/**
	 * 关系类型
	 */
	private String relationType;  
	/**
	 * 资源和资源的关系存在一种标签分类。同一资源下的关系户，按照特定的标签进行分类以及分组。
	 */
	private List<String> tags;
	/**
	 * 关系的顺序
	 */
	private float orderNum;
	/**
	 * 是否可用
	 */
	private boolean enable;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public T getSource() {
		return source;
	}
	public void setSource(T source) {
		this.source = source;
	}
	public T getTarget() {
		return target;
	}
	public void setTarget(T target) {
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
	

	
}