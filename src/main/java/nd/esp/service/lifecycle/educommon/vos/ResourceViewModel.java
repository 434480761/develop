package nd.esp.service.lifecycle.educommon.vos;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import nd.esp.service.lifecycle.support.annotation.MapValid;
import nd.esp.service.lifecycle.vos.valid.BasicInfoDefault;
import nd.esp.service.lifecycle.vos.valid.CategoriesDefault;
import nd.esp.service.lifecycle.vos.valid.CoursewareObjectBasicInfo;
import nd.esp.service.lifecycle.vos.valid.InstructionalObjectiveDefault;
import nd.esp.service.lifecycle.vos.valid.LessPropertiesDefault;
import nd.esp.service.lifecycle.vos.valid.UpdateKnowledgeDefault;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;


/**
 * 教育资源模型。
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResourceViewModel {

	//lc默认值
	private static ResLifeCycleViewModel lc = new ResLifeCycleViewModel();
	static{
		lc.setVersion("v0.1");
		lc.setStatus("CREATED");
		lc.setEnable(true);
		lc.setCreator("{USER}");
		lc.setPublisher("UNKNOW");
		lc.setProvider("NetDragon Inc.");
		lc.setProviderSource("UNKNOW");
	}

	/**
	 * 对象的主键，主键类型采用UUID的形式进行存储
	 */
	private String identifier;
	/**
	 * 学习对象的标题名称
	 */
    @NotBlank(message="{resourceViewModel.title.notBlank.validmsg}",groups={BasicInfoDefault.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class})
    @Length(message="{resourceViewModel.title.maxlength.validmsg}",max=200,groups={BasicInfoDefault.class})
	private String title;
	/**
	 * 学习对象的文字描述，对于文字描述的长度约定为100个汉字
	 */
//    @Length(message="{resourceViewModel.description.maxlength.validmsg}",max=500,groups={BasicInfoDefault.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class})
	private String description;
	/**
	 * 学习对象的语言标识
	 */
    @NotBlank(message="{resourceViewModel.language.notBlank.validmsg}",groups={BasicInfoDefault.class, CoursewareObjectBasicInfo.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class,InstructionalObjectiveDefault.class})
    @Length(message="{resourceViewModel.language.maxlength.validmsg}",max=16,groups={BasicInfoDefault.class, CoursewareObjectBasicInfo.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class,InstructionalObjectiveDefault.class})
	private String language;
	/**
	 * 预览的路径
	 */
	@NotNull(message="{resourceViewModel.preview.notNull.validmsg}",groups={BasicInfoDefault.class, CoursewareObjectBasicInfo.class})
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
	 * 资源编码
	 */
	@Length(message="{resourceViewModel.ndresCode.maxlength.validmsg}",max=100,groups={BasicInfoDefault.class, CoursewareObjectBasicInfo.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class,InstructionalObjectiveDefault.class})
	@JsonInclude(Include.NON_NULL)
	private String ndresCode;
	
	/**
	 * 自定义扩展属性
	 */
	private Map<String,Object> customProperties;
	
	/**
	 * 维度数据
	 */
	@NotNull(message="{resourceViewModel.categories.notNull.validmsg}",groups={CategoriesDefault.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class,InstructionalObjectiveDefault.class})
	@JsonInclude(Include.NON_NULL)
	@MapValid(message="",groups={CategoriesDefault.class, LessPropertiesDefault.class, UpdateKnowledgeDefault.class,InstructionalObjectiveDefault.class})
	private Map<String,List<? extends ResClassificationViewModel>> categories;
	
	/**
	 * 生命周期
	 */
	@Valid
	@JsonInclude(Include.NON_NULL)
	private ResLifeCycleViewModel lifeCycle = lc;
	
	/**
	 * 资源的教育属性
	 */
	@JsonInclude(Include.NON_NULL)
	private ResEducationalViewModel educationInfo;
	
	/**
	 * 教育资源相关的技术属性，包括格式，大小，存储位置，技术需求描述，md5码等。
	 */
	@Valid
	@JsonInclude(Include.NON_NULL)
	private Map<String,? extends ResTechInfoViewModel> techInfo;
	/**
	 * 资源的覆盖范围，包括时间上，空间上，角色以及组织等
	 */
	@Valid
	@JsonInclude(Include.NON_NULL)
	private List<? extends ResCoverageViewModel> coverages;

	/**
	 * 描述资源和资源之间的关系
	 */
	@Valid
	@JsonInclude(Include.NON_NULL)
	private List<? extends ResRelationViewModel> relations;
	
	/**
	 * 资源的版权信息
	 */
	@JsonInclude(Include.NON_NULL)
	private ResRightViewModel copyright;

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

	public Map<String, String> getPreview() {
		return preview;
	}

	public void setPreview(Map<String, String> preview) {
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

	public Map<String, List<? extends ResClassificationViewModel>> getCategories() {
		return categories;
	}

	public void setCategories(
			Map<String,List<? extends ResClassificationViewModel>> categories) {
		this.categories = categories;
	}

	public ResLifeCycleViewModel getLifeCycle() {
		return lifeCycle;
	}

	public void setLifeCycle(ResLifeCycleViewModel lifeCycle) {
		this.lifeCycle = lifeCycle;
	}

	public ResEducationalViewModel getEducationInfo() {
		return educationInfo;
	}

	public void setEducationInfo(ResEducationalViewModel educationInfo) {
		this.educationInfo = educationInfo;
	}

	public Map<String, ? extends ResTechInfoViewModel> getTechInfo() {
		return techInfo;
	}

	public void setTechInfo(Map<String, ? extends ResTechInfoViewModel> techInfo) {
		this.techInfo = techInfo;
	}

	public List<? extends ResCoverageViewModel> getCoverages() {
		return coverages;
	}

	public void setCoverages(List<? extends ResCoverageViewModel> coverages) {
		this.coverages = coverages;
	}

	public List<? extends ResRelationViewModel> getRelations() {
		return relations;
	}

	public void setRelations(List<? extends ResRelationViewModel> relations) {
		this.relations = relations;
	}

	public ResRightViewModel getCopyright() {
		return copyright;
	}

	public void setCopyright(ResRightViewModel copyright) {
		this.copyright = copyright;
	}

	public Map<String,Object> getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(Map<String,Object> customProperties) {
		this.customProperties = customProperties;
	}

	public String getNdresCode() {
		return ndresCode;
	}

	public void setNdresCode(String ndresCode) {
		this.ndresCode = ndresCode;
	}
}