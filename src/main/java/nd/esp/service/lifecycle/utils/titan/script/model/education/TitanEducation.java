package nd.esp.service.lifecycle.utils.titan.script.model.education;

import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanCompositeKey;
import nd.esp.service.lifecycle.utils.titan.script.annotation.TitanField;
import nd.esp.service.lifecycle.utils.titan.script.model.TitanModel;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Administrator on 2016/8/24.
 */
public class TitanEducation extends TitanModel {
    @TitanCompositeKey
    @TitanField(name = "identifier")
    private String identifier;

    @TitanField(name = "m_identifier")
    private String mIdentifier;

    @TitanCompositeKey
    @TitanField(name = "primary_category")
    private String primaryCategory;

    @TitanField(name = "description")
    private String description;

    @TitanField(name = "title")
    private String title;

    @TitanField(name = "lc_create_time")
    private BigDecimal dbcreateTime;

    @TitanField(name = "lc_last_update")
    private BigDecimal dblastUpdate;

    @TitanField(name = "preview")
    private String dbpreview;

    @TitanField(name = "tags")
    protected String dbtags;

    @TitanField(name = "keywords")
    protected String dbkeywords;

    @TitanField(name = "language")
    protected String language;

    @TitanField(name = "lc_enable")
    private Boolean enable = true;

    @TitanField(name = "custom_properties")
    private String customProperties;

    @TitanField(name = "lc_provider_source")
    protected String providerSource;

    @TitanField(name = "lc_version")
    protected String version;

    @TitanField(name = "lc_status")
    protected String status;

    @TitanField(name = "lc_creator")
    protected String creator;

    @TitanField(name = "lc_provider")
    protected String provider;

    @TitanField(name = "cr_right")
    protected String crRight;

    @TitanField(name = "cr_description")
    protected String crDescription;

    @TitanField(name = "cr_author")
    protected String author;

    @TitanField(name = "edu_interactivity")
    private Integer interactivity;

    @TitanField(name = "edu_interactivity_level")
    private Integer interactivityLevel;

    @TitanField(name = "edu_end_user_type")
    private String endUserType;

    @TitanField(name = "edu_semantic_density")
    private Integer semanticDensity;

    @TitanField(name = "edu_age_range")
    private String ageRange;

    @TitanField(name = "edu_context")
    private String context;

    @TitanField(name = "edu_difficulty")
    private String difficulty;

    @TitanField(name = "edu_learning_time")
    private String learningTime;

    @TitanField(name = "edu_description")
    private String dbEduDescription;

    @TitanField(name = "edu_language")
    private String eduLanguage;

    @TitanField(name = "lc_publisher")
    protected String publisher;

    @TitanField(name = "ndres_code")
    private String ndresCode;

    @TitanField(name = "cr_has_right")
    protected Boolean hasRight;

    @TitanField(name = "cr_right_start_date")
    protected BigDecimal rightStartDate;

    @TitanField(name = "cr_right_end_date")
    protected BigDecimal rightEndDate;

    @TitanField(name = "lc_provider_mode")
    protected String providerMode;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getNdresCode() {
        return ndresCode;
    }

    public void setNdresCode(String ndresCode) {
        this.ndresCode = ndresCode;
    }

    public Boolean getHasRight() {
        return hasRight;
    }

    public void setHasRight(Boolean hasRight) {
        this.hasRight = hasRight;
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

    public String getProviderMode() {
        return providerMode;
    }

    public void setProviderMode(String providerMode) {
        this.providerMode = providerMode;
    }
}
