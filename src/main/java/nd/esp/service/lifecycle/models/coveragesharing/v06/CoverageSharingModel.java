package nd.esp.service.lifecycle.models.coveragesharing.v06;
/**
 * 库分享 Model
 * @author xiezy
 * @date 2016年8月24日
 */
public class CoverageSharingModel {
	/**
	 * id
	 */
	private String identifier;
	/**
	 * 资源覆盖范围-分享来源
	 */
	private String sourceCoverage;
	/**
	 * 资源覆盖范围-分享对象
	 */
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
