package nd.esp.service.lifecycle.vos;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

/**
 * 分类维度viewModel
 * <br>Created 2015年4月20日 下午7:16:45
 * @version  0.3
 * @author   linsm		
 *
 * @see 	 
 * 
 * Copyright(c) 2009-2014, TQ Digital Entertainment, All Rights Reserved
 *
 */
public class CategoryViewModel {
	/**
	 * uuid
	 */
	private String identifier;
	/**
	 * 分类维度的标识名称
	 */
	@NotBlank(message="{categoryViewModel.title.notBlank.validmsg}") @Length(max=10,message="{categoryViewModel.title.maxlength.validmsg}")
	private String title;
	/**
	 * 英文标识名称
	 */
	@NotBlank(message="{categoryViewModel.shortName.notBlank.validmsg}") @Length(max=30,message="{categoryViewModel.shortName.maxlength.validmsg}")
	private String shortName;
	/**
	 * ND编码标识
	 */
	@NotBlank(message="{categoryViewModel.ndCode.notBlank.validmsg}")
	private String ndCode;
	/**
	 * 创建此分类的主要目的
	 */
	@NotBlank(message="{categoryViewModel.purpose.notBlank.validmsg}") @Length(min=10,max=100,message="{categoryViewModel.purpose.length.validmsg}")
	private String purpose;
	/**
	 * 对此分类维度进行描述，主要对标识，编码规则进行描述
	 */
	@Length(max=200,message="{categoryViewModel.description.maxlength.validmsg}")
	private String description;
	/**
	 * 分类的依据和来源
	 */
	private String source;
	/**
	 * 国家标准编码
	 */
	@NotBlank(message="{categoryViewModel.gbCode.notBlank.validmsg}")
	@Length(max=30,message="{categoryViewModel.gbCode.maxlength.validmsg}")
	private String gbCode;
	
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
	
	

}
