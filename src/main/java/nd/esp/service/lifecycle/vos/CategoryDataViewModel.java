package nd.esp.service.lifecycle.vos;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 *  维度数据viewModel
 * <br>Created 2015年4月20日 下午7:44:21
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class CategoryDataViewModel {
	/**
	 * uuid
	 */
	private String identifier;
	/**
	 * 分类维度的标识名称
	 */
	@NotBlank(message="{categoryDataViewModel.title.notBlank.validmsg}") @Length(max=20,message="{categoryDataViewModel.title.maxlength.validmsg}")
	private String title;
	/**
	 * 英文标识名称
	 */
	@NotBlank(message="{categoryDataViewModel.shortName.notBlank.validmsg}") @Length(max=30,message="{categoryDataViewModel.shortName.maxlength.validmsg}")
	private String shortName;
	/**
	 * ND编码标识
	 */
	@NotBlank(message="{categoryDataViewModel.ndCode.notBlank.validmsg}")
	private String ndCode;
	/**
	 * 同一分类维度下的父级节点，如果此数据为定级节点，默认值为ROOT
	 */
	private String parent;
	/**
	 * 对此分类维度数据进行描述
	 */
	@Length(max=100,message="{categoryDataViewModel.description.maxlength.validmsg}")
	private String description;
	/**
	 * 同一维度下，同一级别下，子分类的顺序
	 */
	private int orderNum;
	/**
	 * 国家标准编码
	 */
	@NotBlank(message="{categoryDataViewModel.gbCode.notBlank.validmsg}")
	@Length(max=30,message="{categoryDataViewModel.gbCode.maxlength.validmsg}")
	private String gbCode;
	/**
	 * 分类维度的标识
	 */
	@NotBlank(message="{categoryDataViewModel.category.notBlank.validmsg}")
	private String category;
	/**
	 * 当前分类维度下的分类路径
	 */
	private String dimensionPath;
	
	/**
	 * 预览图
	 */
	private String preview;
	
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
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getNdCode() {
		return ndCode;
	}
	public void setNdCode(String ndCode) {
		this.ndCode = ndCode;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getOrderNum() {
		return orderNum;
	}
	public void setOrderNum(int orderNum) {
		this.orderNum = orderNum;
	}
	public String getGbCode() {
		return gbCode;
	}
	public void setGbCode(String gbCode) {
		this.gbCode = gbCode;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getDimensionPath() {
		return dimensionPath;
	}
	public void setDimensionPath(String dimensionPath) {
		this.dimensionPath = dimensionPath;
	}
	public String getPreview() {
		return preview;
	}
	public void setPreview(String preview) {
		this.preview = preview;
	}
	
	
	

}
