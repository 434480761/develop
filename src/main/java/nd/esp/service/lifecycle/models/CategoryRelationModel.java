package nd.esp.service.lifecycle.models;

import java.util.List;


/**
 * 维度数据关系
 * @author linsm
 * @version 0.3
 * @created 20-4月-2015 15:26:22
 */
public class CategoryRelationModel extends RelationModel<CategoryDataModel> {

	
	/**
	 * 分类的编码路径
	 */
	private String patternPath;
	/**
	 * 同级别的父级节点。在模式下，同级别会存在父子关系。
	 */
	private String levelParent;
	/**
	 * 引用的模式
	 */
	private CategoryPatternModel pattern;
	/**
	 * 同级别下的子节点。
	 */
	private List<CategoryRelationModel> levelItems;
	
	public String getPatternPath() {
		return patternPath;
	}
	public void setPatternPath(String patternPath) {
		this.patternPath = patternPath;
	}
	public String getLevelParent() {
		return levelParent;
	}
	public void setLevelParent(String levelParent) {
		this.levelParent = levelParent;
	}
	public CategoryPatternModel getPattern() {
		return pattern;
	}
	public void setPattern(CategoryPatternModel pattern) {
		this.pattern = pattern;
	}
	public List<CategoryRelationModel> getLevelItems() {
		return levelItems;
	}
	public void setLevelItems(List<CategoryRelationModel> levelItems) {
		this.levelItems = levelItems;
	}


}