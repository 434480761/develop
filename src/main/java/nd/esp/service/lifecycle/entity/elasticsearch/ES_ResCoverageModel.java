package nd.esp.service.lifecycle.entity.elasticsearch;

public class ES_ResCoverageModel {
	/**
	 * Org，Role，User，Time，Space，Group；必填值，举例：“Org”
	 * 描述资源的覆盖范围的类型，主要是机构，组织，角色，用户，空间描述和时间上的描述
	 */
	private String targetType;
	/**
	 * 目标类型确定后，目标的内容需要进行填写，Org，Role，User，Group需要记录Id。空间和时间上的描述主要是字符串描述
	 */
	private String target;
	/**
	 * 目标对象的中文标识或者描述信息
	 */
	private String targetTitle;
	/**
	 * VIEW，PLAY，SHAREING，REPORTING,COPY，NONE；必填值，举例：“SHAREING” 策略信息
	 */
	private String strategy;
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
