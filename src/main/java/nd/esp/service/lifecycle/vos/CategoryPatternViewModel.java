package nd.esp.service.lifecycle.vos;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 分类模式viewModel
 * 
 * <br>Created 2015年4月20日 下午8:10:52
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class CategoryPatternViewModel {
	/**
	 * uuid
	 */
	private String identifier;
	/**
	 * 应用模式的名称
	 */
	@NotBlank(message="{categoryPatternViewModel.title.notBlank.validmsg}") @Length(max=50,message="{categoryPatternViewModel.title.maxlength.validmsg}")
	private String title;
	/**
	 * 模式的英文标识
	 */
	@NotBlank(message="{categoryPatternViewModel.patternName.notBlank.validmsg}") @Length(max=30,message="{categoryPatternViewModel.patternName.maxlength.validmsg}")
	private String patternName;
	/**
	 * 目的描述
	 */
	@NotBlank(message="{categoryPatternViewModel.purpose.notBlank.validmsg}") @Length(max=100,message="{categoryPatternViewModel.purpose.maxlength.validmsg}")
	private String purpose;
	/**
	 * 对此分类维度进行描述，主要对标识，编码规则进行描述
	 */
	@NotBlank(message="{categoryPatternViewModel.description.notBlank.validmsg}") @Length(max=200,message="{categoryPatternViewModel.description.maxlength.validmsg}")
	private String description;
	/**
	 * 应用场景描述
	 */
	private String scope;
	
	/**
	 * 维度路径,比如：$O$S$E
	 */
	private String patternPath;
	
	public String getPatternPath() {
        return patternPath;
    }
    public void setPatternPath(String patternPath) {
        this.patternPath = patternPath;
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
	public String getPatternName() {
		return patternName;
	}
	public void setPatternName(String patternName) {
		this.patternName = patternName;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
}
