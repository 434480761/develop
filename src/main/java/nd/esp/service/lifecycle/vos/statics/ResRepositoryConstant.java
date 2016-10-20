package nd.esp.service.lifecycle.vos.statics;
/**
 * 物理存储空间常量
 * <p>Create Time: 2015年7月17日           </p>
 * @author xiezy
 */
public class ResRepositoryConstant {
    
    /**
     * target_type：存储空间的类型
     */
    public final static String TARGET_TYPE_ORG = "Org";
    public final static String TARGET_TYPE_GROUP = "Group";
    public final static String TARGET_TYPE_DEBUG = "Debug";
    public final static String TARGET_TYPE_RSD = "RSD";
    public final static String TARGET_TYPE_APP = "App";
    
    /**
     * status:私有库的状态信息
     */
    public final static String STATUS_APPLY = "APPLY";
    public final static String STATUS_RUNNING = "RUNNING";
    public final static String STATUS_REMOVE = "REMOVE";
    
    /**
     * 判断存储空间的类型是否在可选范围内    
     * <p>Create Time: 2015年7月16日   </p>
     * <p>Create author: xiezy   </p>
     * @param targetType
     * @return
     */
    public static boolean isRepositoryTargetType(String targetType) {
        if(targetType.equals(ResRepositoryConstant.TARGET_TYPE_ORG) ||
                targetType.equals(ResRepositoryConstant.TARGET_TYPE_GROUP) ||
                targetType.equals(ResRepositoryConstant.TARGET_TYPE_APP)){
                 return true;
             }
        return false;
    }
    
    /**
     * 判断私有库的状态信是否在可选范围内	
     * <p>Create Time: 2015年7月17日   </p>
     * <p>Create author: xiezy   </p>
     * @param statuc
     * @return
     */
    public static boolean isRepositoryStatus(String status) {
        if(status.equals(ResRepositoryConstant.STATUS_APPLY)        ||
                status.equals(ResRepositoryConstant.STATUS_RUNNING)      ||
                status.equals(ResRepositoryConstant.STATUS_REMOVE)){
                 return true;
             }
        return false;
    }
}
