package nd.esp.service.lifecycle.support.al;
/**
 * 地区语言枚举
 * @author xiezy
 * @date 2016年10月17日
 */
public enum AreaAndLanguage {
	ZH_CN("zh-CN","简体中文(中国)"),
	ZH_TW("zh-TW","繁体中文(台湾地区)"),
	ZH_HK("zh-HK","繁体中文(香港)"),
	en_HK("en-HK","英语(香港)"),
	en_US("en-US","英语(美国)"),
	en_GB("en-GB","英语(英国)"),
	;
	
	private AreaAndLanguage(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	private String code;
	private String description;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
