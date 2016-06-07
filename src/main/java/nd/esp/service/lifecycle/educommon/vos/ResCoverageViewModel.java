package nd.esp.service.lifecycle.educommon.vos;

import nd.esp.service.lifecycle.vos.valid.CoveragesDefault;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;


/**
 * @author johnny
 * @version 1.0
 * @created 08-7月-2015 10:18:49
 */
public class ResCoverageViewModel {

	/**
	 * Org，Role，User，Time，Space，Group；必填值，举例：“Org”
	 * 描述资源的覆盖范围的类型，主要是机构，组织，角色，用户，空间描述和时间上的描述
	 */
	@NotBlank(message="{resourceViewModel.coverages.targetType.notBlank.validmsg}",groups={CoveragesDefault.class})
//	@Reg(message="{resourceViewModel.coverages.targetType.reg.validmsg}",pattern="^(Org|Role|User|Time|Space|Group)$",isNullValid=false,groups={CoveragesDefault.class})
	private String targetType;
	/**
	 * 目标类型确定后，目标的内容需要进行填写，Org，Role，User，Group需要记录Id。空间和时间上的描述主要是字符串描述
	 */
	@NotBlank(message="{resourceViewModel.coverages.target.notBlank.validmsg}",groups={CoveragesDefault.class})
	@Length(message="{resourceViewModel.coverages.target.maxlength.validmsg}",max=100, groups={CoveragesDefault.class})
	private String target;
	/**
	 * 目标对象的中文标识或者描述信息
	 */
	@NotBlank(message="{resourceViewModel.coverages.targetTitle.notBlank.validmsg}",groups={CoveragesDefault.class})
	private String targetTitle;
	/**
	 * VIEW，PLAY，SHAREING，REPORTING,COPY，NONE；必填值，举例：“SHAREING”  策略信息
	 */
	@NotBlank(message="{resourceViewModel.coverages.strategy.notBlank.validmsg}",groups={CoveragesDefault.class})
	private String strategy;

	public ResCoverageViewModel(){

	}


	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public void setTargetTitle(String targetTitle) {
		this.targetTitle = targetTitle;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

}