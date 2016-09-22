package nd.esp.service.lifecycle.support.terminal;

import nd.esp.service.lifecycle.utils.StringUtils;
/**
 * 终端枚举
 * @author xiezy
 * @date 2016年9月20日
 */
public enum TerminalTypeEnum {
	PC("pc"),
	IOS("ios"),
	ANDROID("android"),
	WEB("web"),
	PACKAGE("package")
	;
	
	/**
	 * 获取终端类型
	 * @author xiezy
	 * @date 2016年9月20日
	 * @param terminal
	 * @return
	 */
	public static String getTerminalType(String terminal){
		if (StringUtils.hasText(terminal)) {
            for(TerminalTypeEnum type: TerminalTypeEnum.values()){
                if(type.getName().equals(terminal)){
                	
                    return terminal;
                }
            }
        }
		
		return null;
	}
	
	private TerminalTypeEnum(String name) {
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
