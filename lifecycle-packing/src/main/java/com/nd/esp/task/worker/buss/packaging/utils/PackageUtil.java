/* =============================================================
 * Created: [2015年5月20日] by Administrator
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package com.nd.esp.task.worker.buss.packaging.utils;

import java.util.HashMap;
import java.util.Map;

import com.nd.esp.task.worker.buss.packaging.utils.gson.ObjectUtils;


/**
 * <h2>打包工具相关的工具</h2>
 * <p>
 * </p>
 *
 * @author liuwx
 * @since
 * @create 2015年5月20日 下午5:10:21
 */
public class PackageUtil {
    
    
    //public final static String OFFLINE_ICPLAYER = "offline_icplayer";

    public final static String TARGET_DEFALUT = "default";
    public final static String TARGET_STUDENT = "student";
    public final static String OFFLINE = "offline";
    public final static String PLUS_ICPLAYER = "_icplayer";
    
    public static final String TARGET_COMBINED = "combined";
    
    /**
     * 获取storeinfo中的打包的键
     * 
     * @param icplayer
     * @return
     * @since
     */
    public static String getStoreInfoKey(String target, boolean icplayer) {
        if(StringUtils.isEmpty(target) || target.equals(PackageUtil.TARGET_DEFALUT)) {
            target = PackageUtil.OFFLINE;
        }
        String key = icplayer ? target+PackageUtil.PLUS_ICPLAYER : target;
        return key;
    }
    
    
    /**
     * cs返回的信息,转成storeinfo需要的
     * 
     * @param response
     * @return
     * @since 
     */
    public static Map<String,String>convertTostoreInfoMap( Map<String, Object>response ){
        
        Map<String,String> map=new HashMap<String, String>();
        if(CollectionUtils.isNotEmpty(response)){
            Object obj=response.get("path");
            String path=obj==null?"":obj.toString();
            if(StringUtils.hasLength(path)){
                map.put("location", path);
                Map<String,Object>inode=ObjectUtils.fromJson(ObjectUtils.toJson(response.get("inode")), Map.class);
                map.put("format", String.valueOf(inode.get("mime")));
                map.put("md5", String.valueOf(inode.get("md5")));
                map.put("size", String.valueOf(inode.get("size")));
            }
        }
        return map;
    }

}
