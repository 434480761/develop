package nd.esp.service.lifecycle.models;

import java.util.List;


/**
 * 维度数据
 * @author linsm
 * @version 0.3
 * @created 20-4月-2015 15:26:21
 */
public class CategoryDataModel {

	/**
	 * ND定义的分类维度的编码，必填，并且需要进行唯一性校验
	 */
	private String ndCode;
	/**
	 * 说明  可选
	 */
	private String description;
	/**
	 * 主键标识
	 */
	private String identifier;
	/**
	 * 分类维度级别名称  中文名称
	 */
	private String title;
	/**
	 * 上级节点
	 */
	private CategoryDataModel parent;
	/**
	 * 所属的分类维度
	 */
	private CategoryModel category;
	/**
	 * 同级别下的顺序  必填
	 */
	private int orderNum;
	/**
	 * 维度数据之间的关系，暂时不做启用操作
	 */
	private List<CategoryRelationModel> relation;
	/**
	 * 维度分类的路径说明，路径之间用反斜杠分割，非用户输入，业务代码自动生成分类路径
	 */
	private String dimensionPath;
	/**
	 * 国家定义的分类维度的编码，此字段为可选。
	 */
	private String gbCode;
	/**
	 * 学科的英文名称
	 */
	private String shortName;
	
	/**
	 * 预览图
	 */
	private String preview;
	
	public String getNdCode() {
		return ndCode;
	}
	public void setNdCode(String ndCode) {
		this.ndCode = ndCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public CategoryDataModel getParent() {
		return parent;
	}
	public void setParent(CategoryDataModel parent) {
		this.parent = parent;
	}
	public CategoryModel getCategory() {
		return category;
	}
	public void setCategory(CategoryModel category) {
		this.category = category;
	}
	public int getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}
	public List<CategoryRelationModel> getRelation() {
		return relation;
	}
	public void setRelation(List<CategoryRelationModel> relation) {
		this.relation = relation;
	}
	public String getDimensionPath() {
		return dimensionPath;
	}
	public void setDimensionPath(String dimensionPath) {
		this.dimensionPath = dimensionPath;
	}
	public String getGbCode() {
		return gbCode;
	}
	public void setGbCode(String gbCode) {
		this.gbCode = gbCode;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getPreview() {
		return preview;
	}
	public void setPreview(String preview) {
		this.preview = preview;
	}



}