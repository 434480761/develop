package nd.esp.service.lifecycle.support.enums;

import nd.esp.service.lifecycle.utils.StringUtils;

/**
 * @title 生命周期资源枚举类
 * @desc
 * @atuh lwx
 * @createtime on 2015年8月17日 下午9:38:31
 */
public enum LifecycleStatus {
    
    CREATING("CREATING","正在创建"),
    CREATED("CREATED","创建完成"),
    EDITING("EDITING","正在编辑"),
    EDITED("EDITED","编辑完成"),
    TRANSCODE_WAITING("TRANSCODE_WAITING","等待转码"),
    TRANSCODING("TRANSCODING","转码中"),
    TRANSCODED("TRANSCODED","转码完成"),
    TRANSCODE_ERROR("TRANSCODE_ERROR","转码错误"),
    AUDIT_WAITING("AUDIT_WAITING","等待审核"),
    AUDITING("AUDITING","审核中"),
    AUDITED("AUDITED","审核完成"),
    PUBLISH_WAITING("PUBLISH_WAITING","等待发布"),
    PUBLISHING("PUBLISHING","发布中"),
    PUBLISHED("PUBLISHED","发布完成"),
    ONLINE("ONLINE","资源上线"),
    OFFLINE("OFFLINE","资源下线"),
    AUDIT_REJECT("AUDIT_REJECT","审核驳回"),
    REMOVED("REMOVED","资源已删除");
    
    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    String code;
    String message;
    
    LifecycleStatus(String code,String message){
        this.code=code;
        this.message=message;
        
    }
    
    /**	
     * @desc:判断对象是否需要转码  
     * @createtime: 2015年8月17日 
     * @author: liuwx 
     * @param status
     * @return
     */
    public static boolean isNeedTranscode(String status){
       return TRANSCODE_WAITING.getCode().equals(status);
        
    }

    /**
     * @desc:  特殊转码状态
     * @createtime: 2015年9月18日
     * @author: liuwx
     * @return
     */
    public static LifecycleStatus[] getSpecialConverseStatus(){

    	return  new LifecycleStatus[]{CREATED,AUDITING,AUDITED,ONLINE,OFFLINE,AUDIT_REJECT};
    }
    
    /**
     * 判断是否是合法的LC状态码
     * @author xiezy
     * @date 2016年7月11日
     * @param status
     * @return
     */
    public static boolean isLegalStatus(String status){
    	if(StringUtils.hasText(status)){
    		for(LifecycleStatus ls : LifecycleStatus.values()){
    			if(ls.getCode().equals(status)){
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
}
