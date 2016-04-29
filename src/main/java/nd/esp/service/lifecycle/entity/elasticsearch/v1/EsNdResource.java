package nd.esp.service.lifecycle.entity.elasticsearch.v1;

/**
 * 教育资源基本属性（包含LC，EDU，CR）
 * 
 * @author linsm
 *
 */
public class EsNdResource {

	private String identifier;

	private String description;

	private String title;

	private Long dbcreateTime;

	private Long dblastUpdate;
	private String dbpreview;

	private String dbtags;
	private String dbkeywords;

	private String language;

	private Boolean enable = true;

	private String customProperties;

	private String code;

	private String providerSource;

	/** * ******************* LIFE_CYCLE_ATTRIBUTES START *****************/

	private String version;

	private String status;

	private String publisher;

	private String creator;

	private String provider;

	/** **************** LIFE_CYCLE_ATTRIBUTES END * *******************/

	/** * ******************* CR_ATTRIBUTES START *****************/

	private String crRight;

	private String crDescription;

	private String author;

	/** * ******************* CR_ATTRIBUTES END *****************/

	private Integer interactivity;

	private Integer interactivityLevel;

	private String endUserType;

	private Integer semanticDensity;

	private String ageRange;

	private String context;

	private String difficulty;

	private String learningTime;

	private String dbEduDescription;

	private String eduLanguage;

	/*** ******************教育属性 end **************** **/

	private String primaryCategory;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getDbcreateTime() {
		return dbcreateTime;
	}

	public void setDbcreateTime(Long dbcreateTime) {
		this.dbcreateTime = dbcreateTime;
	}

	public Long getDblastUpdate() {
		return dblastUpdate;
	}

	public void setDblastUpdate(Long dblastUpdate) {
		this.dblastUpdate = dblastUpdate;
	}

	public String getDbpreview() {
		return dbpreview;
	}

	public void setDbpreview(String dbpreview) {
		this.dbpreview = dbpreview;
	}

	public String getDbtags() {
		return dbtags;
	}

	public void setDbtags(String dbtags) {
		this.dbtags = dbtags;
	}

	public String getDbkeywords() {
		return dbkeywords;
	}

	public void setDbkeywords(String dbkeywords) {
		this.dbkeywords = dbkeywords;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public String getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(String customProperties) {
		this.customProperties = customProperties;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getProviderSource() {
		return providerSource;
	}

	public void setProviderSource(String providerSource) {
		this.providerSource = providerSource;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getCrRight() {
		return crRight;
	}

	public void setCrRight(String crRight) {
		this.crRight = crRight;
	}

	public String getCrDescription() {
		return crDescription;
	}

	public void setCrDescription(String crDescription) {
		this.crDescription = crDescription;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Integer getInteractivity() {
		return interactivity;
	}

	public void setInteractivity(Integer interactivity) {
		this.interactivity = interactivity;
	}

	public Integer getInteractivityLevel() {
		return interactivityLevel;
	}

	public void setInteractivityLevel(Integer interactivityLevel) {
		this.interactivityLevel = interactivityLevel;
	}

	public String getEndUserType() {
		return endUserType;
	}

	public void setEndUserType(String endUserType) {
		this.endUserType = endUserType;
	}

	public Integer getSemanticDensity() {
		return semanticDensity;
	}

	public void setSemanticDensity(Integer semanticDensity) {
		this.semanticDensity = semanticDensity;
	}

	public String getAgeRange() {
		return ageRange;
	}

	public void setAgeRange(String ageRange) {
		this.ageRange = ageRange;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public String getLearningTime() {
		return learningTime;
	}

	public void setLearningTime(String learningTime) {
		this.learningTime = learningTime;
	}

	public String getDbEduDescription() {
		return dbEduDescription;
	}

	public void setDbEduDescription(String dbEduDescription) {
		this.dbEduDescription = dbEduDescription;
	}

	public String getEduLanguage() {
		return eduLanguage;
	}

	public void setEduLanguage(String eduLanguage) {
		this.eduLanguage = eduLanguage;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

}
