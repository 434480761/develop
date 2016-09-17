package nd.esp.service.lifecycle.vos.coveragesharing.v06;

import org.hibernate.validator.constraints.NotBlank;

/**
 * 库分享 viewModel
 * @author xiezy
 * @date 2016年8月24日
 */
public class CoverageSharingViewModel {
	/**
	 * id
	 */
	private String identifier;
	/**
	 * 资源覆盖范围-分享来源
	 */
	@NotBlank(message="{coverageSharingViewModel.sourceCoverage.notBlank.validmsg}")
	private String sourceCoverage;
	/**
	 * 资源覆盖范围-分享对象
	 */
	@NotBlank(message="{coverageSharingViewModel.targetCoverage.notBlank.validmsg}")
	private String targetCoverage;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getSourceCoverage() {
		return sourceCoverage;
	}
	public void setSourceCoverage(String sourceCoverage) {
		this.sourceCoverage = sourceCoverage;
	}
	public String getTargetCoverage() {
		return targetCoverage;
	}
	public void setTargetCoverage(String targetCoverage) {
		this.targetCoverage = targetCoverage;
	}
}
