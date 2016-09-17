package nd.esp.service.lifecycle.entity.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.daos.elasticsearch.EsResourceOperationImpl;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ES_ResourceModel {
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
	private Map<String, String> preview;// es change to String 与mysql 一致
	/**
	 * 社会化标签
	 */
	private List<String> tags;// es change to String 与mysql 一致
	/**
	 * 关键字
	 */
	private List<String> keywords;// es change to String 与mysql 一致

	/**
	 * 资源编号
	 */
	private String ndresCode;

	/**
	 * 自定义扩展属性
	 */
	private String customProperties;

	private List<ES_ResClassificationModel> categoryList = new ArrayList<ES_ResClassificationModel>();

	/**
	 * 生命周期
	 */
	private ES_ResLifeCycleModel lifeCycle;

	/**
	 * 资源的教育属性
	 */
	private ES_ResEducationalModel educationInfo;

	/**
	 * 教育资源相关的技术属性，包括格式，大小，存储位置，技术需求描述，md5码等。
	 */
	private List<ES_ResTechInfoModel> techInfoList = new ArrayList<ES_ResTechInfoModel>();
	/**
	 * 资源的覆盖范围，包括时间上，空间上，角色以及组织等
	 */
	private List<ES_ResCoverageModel> coverages;

	/**
	 * 描述资源和资源之间的关系
	 */
	// private List<ES_ResRelationModel> relations;

	/**
	 * 资源的版权信息
	 */
	private ES_ResRightModel copyright;

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

	public String getPreview() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper.writeValueAsString(preview);
	}

	public void setPreview(Map<String, String> preview) {
		this.preview = preview;
	}

	public String getTags() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper.writeValueAsString(tags);
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getKeywords() throws JsonProcessingException {
		return EsResourceOperationImpl.ObjectMapper
				.writeValueAsString(keywords);
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getNdresCode() {
		return ndresCode;
	}

	public void setNdresCode(String ndresCode) {
		this.ndresCode = ndresCode;
	}

	public String getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(String customProperties) {
		this.customProperties = customProperties;
	}

	public List<ES_ResClassificationModel> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<ES_ResClassificationModel> categoryList) {
		this.categoryList = categoryList;
	}

	public ES_ResLifeCycleModel getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(ES_ResLifeCycleModel lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public ES_ResEducationalModel getEducationInfo() {
		return educationInfo;
	}

	public void setEducationInfo(ES_ResEducationalModel educationInfo) {
		this.educationInfo = educationInfo;
	}

	public List<ES_ResTechInfoModel> getTechInfoList() {
		return techInfoList;
	}

	public void setTechInfoList(List<ES_ResTechInfoModel> techInfoList) {
		this.techInfoList = techInfoList;
	}

	public List<ES_ResCoverageModel> getCoverages() {
		return coverages;
	}

	public void setCoverages(List<ES_ResCoverageModel> coverages) {
		this.coverages = coverages;
	}

	// public List<ES_ResRelationModel> getRelations() {
	// return relations;
	// }
	//
	// public void setRelations(List<ES_ResRelationModel> relations) {
	// this.relations = relations;
	// }

	public ES_ResRightModel getCopyright() {
		return copyright;
	}

	public void setCopyright(ES_ResRightModel copyright) {
		this.copyright = copyright;
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		Map<String, String> preview = null;
		preview = EsResourceOperationImpl.ObjectMapper.readValue("null",
				Map.class);
		System.out.println(preview);
	}

}
