package nd.esp.service.lifecycle.educommon.support;
/**
 * 统计平台枚举
 * @author xiezy
 * @date 2016年7月27日
 */
public enum StatisticsPlatform {
	TOTAL("TOTAL"),
	NDPPT("101PPT")
	;
	
	private StatisticsPlatform(String name) {
		this.name = name;
	}
	
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
