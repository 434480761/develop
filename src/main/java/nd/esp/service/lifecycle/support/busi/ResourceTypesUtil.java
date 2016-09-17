/* =============================================================
 * Created: [2015年5月20日] by Administrator
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.support.busi;

import nd.esp.service.lifecycle.controllers.ResourceController;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;

/**
 * <h2>资源类型工具类</h2>
 * <p>
 * 资源类型相关的校验可以通过此工具类来完成,尽量避免直接调用{@link ResourceTypes}
 * </p>
 *
 * @author liuwx
 * @since
 * @see ResourceController
 * @create 2015年5月20日 下午4:00:01
 */
public class ResourceTypesUtil {
    /**	
     * @desc:判断资源类型是否是转码的类型
     * @createtime: 2015年8月21日 
     * @author: liuwx 
     * @param resType
     * @return
     */
    public static boolean belongtoTranscodeType(String resType){
        
        return ResourceNdCode.coursewares.toString().equals(resType)||ResourceNdCode.lessonplans.toString().equals(resType);
    }
}
