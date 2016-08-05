package nd.esp.service.lifecycle.repository;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;

@Entity
@Table(name = "ndresource")
@Inheritance(strategy=InheritanceType.JOINED)
public class Education extends EspEntity implements Serializable,
		IndexMapper {

	/** @Fields serialVersionUID: */

	private static final long serialVersionUID = 1L;
	public static final String PROP_CREATETIME = "createtime";
	public static final String PROP_LASTUPDATE = "lastUpdate";
	public static final String PROP_CATEGORYS = "categorys";
	public static final String PROP_RELATIONS = "relations";
	public static final String PROP_TAGS = "tags";
	public static final String PROP_KEYWORDS = "keywords";
	
	@Column(name="m_identifier")
	protected String mIdentifier;
	
	/** The create time. */
	@Transient
	protected Timestamp createTime;
	
	@Column(name = "create_time")
	protected BigDecimal dbcreateTime;
	
	/** The last update. */
	
	@Transient
	protected Timestamp lastUpdate;
	
	@Column(name = "last_update")
	protected BigDecimal dblastUpdate;

	@Transient
	private Map<String, String> preview = new HashMap<String, String>();

	@DataConverter(target="preview", type=Map.class)
	@Column(name = "preview")
	private String dbpreview;

//	@DataConverter(target = "relations", type = List.class)
//	@Column(name = "relations")
//	protected String dbrelations;

	@Transient
	protected List<String> relations;

	/** The tags. */
	@Transient
	protected List<String> tags;

	/** The dbtags. */
	@DataConverter(target = "tags", type = List.class)
	@Column(name = "tags")
	protected String dbtags;

	/** The keywords. */
	@Transient
	protected List<String> keywords;

	/** The dbkeywords. */
	@DataConverter(target = "keywords", type = List.class)
	@Column(name = "keywords")
	protected String dbkeywords;

	/** The language. */
	@Column(name = "elanguage")
	protected String language;

	@Column(name = "enable")
	private Boolean enable = true;
	
	@Column(name = "custom_properties")
	private String customProperties;
	
	@Column(name="code")
	private String ndresCode;
	
	/** * ******************* LIFE_CYCLE_ATTRIBUTES START *****************/

	/**
	* 资源来源
	*/
	@Column(name = "provider_source")
	protected String providerSource;
	
	/**
	 * 资源来源方式
	 */
	//@Column(name = "provider_mode")
	@Transient
	protected String providerMode;
	
	/** The version. */
	protected String version;

	/** The status. */
	@Column(name = "estatus")
	protected String status;

	/** The publisher. */
	protected String publisher;

	/** The creator. */
	protected String creator;

	/**
	* 
	*/
	@Column(name = "provider")
	protected String provider;

	/** **************** LIFE_CYCLE_ATTRIBUTES END * *******************/

	/** * ******************* CR_ATTRIBUTES START *****************/

	@Column(name = "cr_right")
	protected String crRight;

	@Column(name = "cr_description")
	protected String crDescription;

	protected String author;
	
//	@Column(name="right_start_date")
	@Transient
	protected BigDecimal rightStartDate;
	
//	@Column(name="right_end_date")
	@Transient
	protected BigDecimal rightEndDate;
	
//	@Column(name="has_right")
	@Transient
	protected boolean hasRight;
	
	/** * ******************* CR_ATTRIBUTES END *****************/

	/*** ******************教育属性 start **************** **/
	private Integer interactivity;

	@Column(name = "interactivity_level")
	private Integer interactivityLevel;

	@Column(name = "end_user_type")
	private String endUserType;

	@Column(name = "semantic_density")
	private Integer semanticDensity;

	@Column(name = "age_range")
	private String ageRange;

	private String context;

	@Column(name = "difficulty")
	private String difficulty;

	@Column(name = "learning_time")
	private String learningTime;

	@Transient
	private Map<String, String> eduDescription;

	@DataConverter(target = "eduDescription", type = Map.class)
	@Column(name = "edu_description")
	private String dbEduDescription;

	@Column(name = "edu_language")
	private String eduLanguage;

	/*** ******************教育属性 end **************** **/
	
	@Column(name="primary_category")
	private String primaryCategory;

	public String getmIdentifier() {
		return mIdentifier;
	}

	public void setmIdentifier(String mIdentifier) {
		this.mIdentifier = mIdentifier;
	}

	public String getPrimaryCategory() {
		return primaryCategory;
	}

	public void setPrimaryCategory(String primaryCategory) {
		this.primaryCategory = primaryCategory;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProvider() {
		return this.provider;
	}

	public String getProviderSource() {
		return providerSource;
	}

	public void setProviderSource(String providerSource) {
		this.providerSource = providerSource;
	}

	/**
	 * Gets the language.
	 * 
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the language.
	 * 
	 * @param language
	 *            the new language
	 */
	public void setLanguage(String language) {
		this.language = language;
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


//	public String getDbrelations() {
//		return dbrelations;
//	}
//
//	public void setDbrelations(String dbrelations) {
//		this.dbrelations = dbrelations;
//	}

	public List<String> getRelations() {
		return relations;
	}

	public void setRelations(List<String> relations) {
		this.relations = relations;
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

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getDbtags() {
		return dbtags;
	}

	public void setDbtags(String dbtags) {
		this.dbtags = dbtags;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public String getDbkeywords() {
		return dbkeywords;
	}

	public void setDbkeywords(String dbkeywords) {
		this.dbkeywords = dbkeywords;
	}

	public Boolean getEnable() {
		return enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	@Override
	public Map<String, Object> getAdditionSearchFields()
			throws EspStoreException {
		if (this.getCreateTime() != null) {
			super.getAdditionSearchFields().put("sorttime",
					this.getCreateTime().getTime());
		}
		return super.getAdditionSearchFields();
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

	public String getCustomProperties() {
		return customProperties;
	}

	public void setCustomProperties(String customProperties) {
		this.customProperties = customProperties;
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

	public Map<String, String> getEduDescription() {
		return eduDescription;
	}

	public void setEduDescription(Map<String, String> eduDescription) {
		this.eduDescription = eduDescription;
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

	@Override
	public IndexSourceType getIndexType() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getPreview() {
		return preview;
	}

	public void setPreview(Map<String, String> preview) {
		this.preview = preview;
	}

	public String getDbpreview() {
		return dbpreview;
	}

	public void setDbpreview(String dbpreview) {
		this.dbpreview = dbpreview;
	}
	
	
	public Timestamp getCreateTime() {
		if(this.dbcreateTime != null){
			this.createTime = new Timestamp(dbcreateTime.longValue());
		} 
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
		if(this.createTime != null){
			this.dbcreateTime = new BigDecimal(createTime.getTime());
		}
	}

	public Timestamp getLastUpdate() {
		if(this.dblastUpdate != null){
			this.lastUpdate = new Timestamp(dblastUpdate.longValue());
		} 
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
		if(this.lastUpdate != null){
			this.dblastUpdate = new BigDecimal(lastUpdate.getTime());
		}
	}

	public BigDecimal getDbcreateTime() {
		return dbcreateTime;
	}

	public void setDbcreateTime(BigDecimal dbcreateTime) {
		this.dbcreateTime = dbcreateTime;
	}

	public BigDecimal getDblastUpdate() {
		return dblastUpdate;
	}

	public void setDblastUpdate(BigDecimal dblastUpdate) {
		this.dblastUpdate = dblastUpdate;
	}


	public String getNdresCode() {
		return ndresCode;
	}

	public void setNdresCode(String ndresCode) {
		this.ndresCode = ndresCode;
	}

	public String getProviderMode() {
		return providerMode;
	}

	public void setProviderMode(String providerMode) {
		this.providerMode = providerMode;
	}

	public BigDecimal getRightStartDate() {
		return rightStartDate;
	}

	public void setRightStartDate(BigDecimal rightStartDate) {
		this.rightStartDate = rightStartDate;
	}

	public BigDecimal getRightEndDate() {
		return rightEndDate;
	}

	public void setRightEndDate(BigDecimal rightEndDate) {
		this.rightEndDate = rightEndDate;
	}

	public boolean isHasRight() {
		return hasRight;
	}

	public void setHasRight(boolean hasRight) {
		this.hasRight = hasRight;
	}
	
}
