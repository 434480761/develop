package nd.esp.service.lifecycle.models;

import java.util.List;


/**
 * 分类模型：包含分类的中文名称，默认名称【根据语言可以设置多过名称】，shortname，mark，描述，schema上传
 * @author linsm
 * @version 0.3
 * @created 20-4月-2015 15:26:21
 */
public class CategoryModel {

	/**
	 * 描述内容
	 */
	private String description;
	/**
	 * 唯一标识符
	 * 
	 * 标准UUID，本值空间由 IETF RFC 4122:2005定义
	 */
	private String identifier;
	/**
	 * 分类名称，业务要求：必须，不能为空，切明确为中文。
	 */
	private String title;
	/**
	 * 分类目的
	 */
	private String purpose;
	/**
	 * 分类依据和来源
	 */
	private String source;
	/**
	 * 国家标准分类体系的编码，如果此分类没有明确的国家编码，此字段为空。可选值
	 */
	private String gbCode;
	/**
	 * 分类的英文标识。这个字段主要是对分类维度的英文标识，不能为空，切必须。并且唯一
	 */
	private String shortName;
	/**
	 * 通过两位字符标识分类体系，如果是只有一位大写数字，只有一位字母的时候，首位用”_“下划线代替。
	 * 
	 * 不能为空，切唯一
	 */
	private String ndCode;
	/**
	 * 业务模型中描述与分类数据之间的关系
	 */
	private List<CategoryDataModel> datas;
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
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
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
	public String getNdCode() {
		return ndCode;
	}
	public void setNdCode(String ndCode) {
		this.ndCode = ndCode;
	}
	public List<CategoryDataModel> getDatas() {
		return datas;
	}
	public void setDatas(List<CategoryDataModel> datas) {
		this.datas = datas;
	}


}