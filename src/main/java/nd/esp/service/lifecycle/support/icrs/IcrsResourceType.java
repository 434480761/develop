package nd.esp.service.lifecycle.support.icrs;

import nd.esp.service.lifecycle.utils.StringUtils;

public enum IcrsResourceType {
	COURSEWARE("courseware","coursewares"),
	MULTIMEDIA("multimedia","assets"),
	BASIC_QUESTION("basic_question","questions"),
	FUNNY_QUESTION("funny_question","coursewareobjects")
	;
	
	/**
	 * 校验是否是合法的类型
	 * @author xiezy
	 * @date 2016年8月2日
	 * @param vrlifeType
	 * @return
	 */
	public static boolean validType(String icrsType){
		if (StringUtils.hasText(icrsType)) {
            for(IcrsResourceType type: IcrsResourceType.values()){
                if(type.getName().equals(icrsType)){
                    return true;
                }
            }
        }
		
		return false;
	}
	
	/**
	 * 返回对应的NDR资源类型
	 * @author xiezy
	 * @date 2016年9月19日
	 * @param icrsType
	 * @return
	 */
	public static String getCorrespondingType(String icrsType){
		if(StringUtils.hasText(icrsType)){
			for(IcrsResourceType type: IcrsResourceType.values()){
				if(type.getName().equals(icrsType)){
					return type.getCorresponding();
				}
			}
		}
		
		return null;
	}
	
	private IcrsResourceType(String name,String corresponding) {
		this.name = name;
		this.corresponding = corresponding;
	}
	
	private String name;
	private String corresponding;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getCorresponding() {
		return corresponding;
	}

	public void setCorresponding(String corresponding) {
		this.corresponding = corresponding;
	}
}
