package nd.esp.service.lifecycle.support.vrlife;

import nd.esp.service.lifecycle.utils.StringUtils;
/**
 * VR人生 类型枚举
 * @author xiezy
 * @date 2016年8月2日
 */
public enum VrLifeType {
	SKELETON("skeleton"),
	ROLECONFIG("roleconfig"),
	SKELETONCONFIG("skeletonconfig"),
	ACTION("action")
	;
	
	/**
	 * 校验是否是合法的类型
	 * @author xiezy
	 * @date 2016年8月2日
	 * @param vrlifeType
	 * @return
	 */
	public static boolean validType(String vrlifeType){
		if (StringUtils.hasText(vrlifeType)) {
            for(VrLifeType type:VrLifeType.values()){
                if(type.getName().equals(vrlifeType)){
                    return true;
                }
            }
        }
		
		return false;
	}
	
	private VrLifeType(String name) {
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
