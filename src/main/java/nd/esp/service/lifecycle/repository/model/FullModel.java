package nd.esp.service.lifecycle.repository.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 提供给通用查询时,接收查询结果使用
 * <p>Create Time: 2015年11月11日           </p>
 * @author xiezy
 */
@Entity
public class FullModel {
    //**********************通用属性**********************\\
    @Id
    private String identifier;
    
    private String mIdentifier;
    private String title;
    private String description;
    private String language;
    private String preview;
    private String tags;
    private String keywords;
    private String customProperties;
    private String code;
    private String relationId;
    private Double statistics_num;
    
    public String getRelationId() {
		return relationId;
	}
	public void setRelationId(String relationId) {
		this.relationId = relationId;
	}
	//**********************LC(生命周期)**********************\\
    private String lifeCycle_version;
    private String lifeCycle_status;
    private String lifeCycle_enable;
    private String lifeCycle_creator;
    private String lifeCycle_publisher;
    private String lifeCycle_provider;
    private String lifeCycle_providerSource;
    private String lifeCycle_providerMode;
    private String lifeCycle_createTime;
    private String lifeCycle_lastUpdate;
    
    //**********************EDU(教育属性)**********************\\
    private String educationInfo_interactivity;
    private String educationInfo_interactivityLevel;
    private String educationInfo_endUserType;
    private String educationInfo_semanticDensity;
    private String educationInfo_context;
    private String educationInfo_ageRange;
    private String educationInfo_difficulty;
    private String educationInfo_learningTime;
    private String educationInfo_description;
    private String educationInfo_language;
    
    //**********************CR(版权属性)**********************\\
    private String copyRight_right;
    private String copyRight_crDescription;
    private String copyRight_author;
    private String copyRight_hasRight;
    private String copyRight_rightStartDate;
    private String copyRight_rightEndDate;
    
    //**********************Get and Set**********************\\
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
    public String getPreview() {
        return preview;
    }
    public void setPreview(String preview) {
        this.preview = preview;
    }
    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
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
    public String getLifeCycle_version() {
        return lifeCycle_version;
    }
    public void setLifeCycle_version(String lifeCycle_version) {
        this.lifeCycle_version = lifeCycle_version;
    }
    public String getLifeCycle_status() {
        return lifeCycle_status;
    }
    public void setLifeCycle_status(String lifeCycle_status) {
        this.lifeCycle_status = lifeCycle_status;
    }
    public String getLifeCycle_enable() {
        return lifeCycle_enable;
    }
    public void setLifeCycle_enable(String lifeCycle_enable) {
        this.lifeCycle_enable = lifeCycle_enable;
    }
    public String getLifeCycle_creator() {
        return lifeCycle_creator;
    }
    public void setLifeCycle_creator(String lifeCycle_creator) {
        this.lifeCycle_creator = lifeCycle_creator;
    }
    public String getLifeCycle_publisher() {
        return lifeCycle_publisher;
    }
    public void setLifeCycle_publisher(String lifeCycle_publisher) {
        this.lifeCycle_publisher = lifeCycle_publisher;
    }
    public String getLifeCycle_provider() {
        return lifeCycle_provider;
    }
    public void setLifeCycle_provider(String lifeCycle_provider) {
        this.lifeCycle_provider = lifeCycle_provider;
    }
    public String getLifeCycle_providerSource() {
        return lifeCycle_providerSource;
    }
    public void setLifeCycle_providerSource(String lifeCycle_providerSource) {
        this.lifeCycle_providerSource = lifeCycle_providerSource;
    }
    public String getLifeCycle_createTime() {
        return lifeCycle_createTime;
    }
    public void setLifeCycle_createTime(String lifeCycle_createTime) {
        this.lifeCycle_createTime = lifeCycle_createTime;
    }
    public String getLifeCycle_lastUpdate() {
        return lifeCycle_lastUpdate;
    }
    public void setLifeCycle_lastUpdate(String lifeCycle_lastUpdate) {
        this.lifeCycle_lastUpdate = lifeCycle_lastUpdate;
    }
    public String getEducationInfo_interactivity() {
        return educationInfo_interactivity;
    }
    public void setEducationInfo_interactivity(String educationInfo_interactivity) {
        this.educationInfo_interactivity = educationInfo_interactivity;
    }
    public String getEducationInfo_interactivityLevel() {
        return educationInfo_interactivityLevel;
    }
    public void setEducationInfo_interactivityLevel(String educationInfo_interactivityLevel) {
        this.educationInfo_interactivityLevel = educationInfo_interactivityLevel;
    }
    public String getEducationInfo_endUserType() {
        return educationInfo_endUserType;
    }
    public void setEducationInfo_endUserType(String educationInfo_endUserType) {
        this.educationInfo_endUserType = educationInfo_endUserType;
    }
    public String getEducationInfo_semanticDensity() {
        return educationInfo_semanticDensity;
    }
    public void setEducationInfo_semanticDensity(String educationInfo_semanticDensity) {
        this.educationInfo_semanticDensity = educationInfo_semanticDensity;
    }
    public String getEducationInfo_context() {
        return educationInfo_context;
    }
    public void setEducationInfo_context(String educationInfo_context) {
        this.educationInfo_context = educationInfo_context;
    }
    public String getEducationInfo_ageRange() {
        return educationInfo_ageRange;
    }
    public void setEducationInfo_ageRange(String educationInfo_ageRange) {
        this.educationInfo_ageRange = educationInfo_ageRange;
    }
    public String getEducationInfo_difficulty() {
        return educationInfo_difficulty;
    }
    public void setEducationInfo_difficulty(String educationInfo_difficulty) {
        this.educationInfo_difficulty = educationInfo_difficulty;
    }
    public String getEducationInfo_learningTime() {
        return educationInfo_learningTime;
    }
    public void setEducationInfo_learningTime(String educationInfo_learningTime) {
        this.educationInfo_learningTime = educationInfo_learningTime;
    }
    public String getEducationInfo_description() {
        return educationInfo_description;
    }
    public void setEducationInfo_description(String educationInfo_description) {
        this.educationInfo_description = educationInfo_description;
    }
    public String getEducationInfo_language() {
        return educationInfo_language;
    }
    public void setEducationInfo_language(String educationInfo_language) {
        this.educationInfo_language = educationInfo_language;
    }
    public String getCopyRight_right() {
        return copyRight_right;
    }
    public void setCopyRight_right(String copyRight_right) {
        this.copyRight_right = copyRight_right;
    }
    public String getCopyRight_crDescription() {
        return copyRight_crDescription;
    }
    public void setCopyRight_crDescription(String copyRight_crDescription) {
        this.copyRight_crDescription = copyRight_crDescription;
    }
    public String getCopyRight_author() {
        return copyRight_author;
    }
    public void setCopyRight_author(String copyRight_author) {
        this.copyRight_author = copyRight_author;
    }
	public Double getStatistics_num() {
		return statistics_num;
	}
	public void setStatistics_num(Double statistics_num) {
		this.statistics_num = statistics_num;
	}
	public String getmIdentifier() {
		return mIdentifier;
	}
	public void setmIdentifier(String mIdentifier) {
		this.mIdentifier = mIdentifier;
	}
	public String getLifeCycle_providerMode() {
		return lifeCycle_providerMode;
	}
	public void setLifeCycle_providerMode(String lifeCycle_providerMode) {
		this.lifeCycle_providerMode = lifeCycle_providerMode;
	}
	public String getCopyRight_hasRight() {
		return copyRight_hasRight;
	}
	public void setCopyRight_hasRight(String copyRight_hasRight) {
		this.copyRight_hasRight = copyRight_hasRight;
	}
	public String getCopyRight_rightStartDate() {
		return copyRight_rightStartDate;
	}
	public void setCopyRight_rightStartDate(String copyRight_rightStartDate) {
		this.copyRight_rightStartDate = copyRight_rightStartDate;
	}
	public String getCopyRight_rightEndDate() {
		return copyRight_rightEndDate;
	}
	public void setCopyRight_rightEndDate(String copyRight_rightEndDate) {
		this.copyRight_rightEndDate = copyRight_rightEndDate;
	}
}
