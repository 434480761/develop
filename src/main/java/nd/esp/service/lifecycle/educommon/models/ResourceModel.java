package nd.esp.service.lifecycle.educommon.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 教育资源模型。
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResourceModel {

	/**
	 * 对象的主键，主键类型采用UUID的形式进行存储
	 */
	private String identifier;
	/**
	 * 学习对象的标题名称
	 */
	private String title;
	/**
	 * 学习对象的文字描述，对于文字描述的长度约定为100个汉字
	 */
	private String description;
	/**
	 * 学习对象的语言标识
	 */
	private String language;
	/**
	 * 预览的路径
	 */
	private Map<String,String> preview;
	/**
	 * 社会化标签
	 */
	private List<String> tags;
	/**
	 * 关键字
	 */
	private List<String> keywords;
	
	/**
	 * 资源编号
	 */
	private String ndresCode;
	
	/**
	 * 统计数量
	 */
	private Double statisticsNum;
	
	/**
	 * 自定义扩展属性
	 */
	private String customProperties;
	
	private List<ResClassificationModel> categoryList = new ArrayList<ResClassificationModel>();
	
	/**
	 * 生命周期
	 */
	private ResLifeCycleModel lifeCycle;
	
	/**
	 * 资源的教育属性
	 */
	private ResEducationalModel educationInfo;
	
	/**
	 * 教育资源相关的技术属性，包括格式，大小，存储位置，技术需求描述，md5码等。
	 */
	private List<ResTechInfoModel> techInfoList = new ArrayList<ResTechInfoModel>();
	/**
	 * 资源的覆盖范围，包括时间上，空间上，角色以及组织等
	 */
	private List<ResCoverageModel> coverages;

	/**
	 * 描述资源和资源之间的关系
	 */
	private List<ResRelationModel> relations;
	
	/**
	 * 资源的版权信息
	 */
	private ResRightModel copyright;
	
	public ResourceModel(){

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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Map<String,String> getPreview() {
		return preview;
	}

	public void setPreview(Map<String,String> preview) {
		this.preview = preview;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public ResLifeCycleModel getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(ResLifeCycleModel lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public List<ResCoverageModel> getCoverages() {
		return coverages;
	}

	public void setCoverages(List<ResCoverageModel> coverages) {
		this.coverages = coverages;
	}

	public ResEducationalModel getEducationInfo() {
		return educationInfo;
	}

	public void setEducationInfo(ResEducationalModel educationInfo) {
		this.educationInfo = educationInfo;
	}

	public List<ResRelationModel> getRelations() {
		return relations;
	}

	public void setRelations(List<ResRelationModel> relations) {
		this.relations = relations;
	}

	public List<ResClassificationModel> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<ResClassificationModel> categoryList) {
		this.categoryList = categoryList;
	}

	public List<ResTechInfoModel> getTechInfoList() {
		return techInfoList;
	}

	public void setTechInfoList(List<ResTechInfoModel> techInfoList) {
		this.techInfoList = techInfoList;
	}

	public ResRightModel getCopyright() {
		return copyright;
	}

	public void setCopyright(ResRightModel copyright) {
		this.copyright = copyright;
	}

	public String getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(String customProperties) {
		this.customProperties = customProperties;
	}

	public String getNdresCode() {
		return ndresCode;
	}

	public void setNdresCode(String ndresCode) {
		this.ndresCode = ndresCode;
	}

	public Double getStatisticsNum() {
		return statisticsNum;
	}

	public void setStatisticsNum(Double statisticsNum) {
		this.statisticsNum = statisticsNum;
	}


}