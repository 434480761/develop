/* =============================================================
 * Created: [2015年10月19日] by Administrator
 * =============================================================
 *
 * Copyright 2014-2015 NetDragon Websoft Inc. All Rights Reserved
 *
 * =============================================================
 */

package nd.esp.service.lifecycle.utils.videoConvertStrategy;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.utils.StringUtils;

import com.nd.gaea.rest.exceptions.extendExceptions.WafSimpleException;


/**
 * @author linsm
 * @since
 */
@SuppressWarnings("deprecation")
public enum SupportVideoType {
//    cue, raw, cif, nrg, dat, sfd, bin, toc, rm, rmvb, mov, wmv, asf, flv, ;
//    flv, mp4, mov, rmvb, rm, avi, wmv, f4v, ;
    flv, mp4, mov, rmvb, rm, avi, wmv, f4v, asf,  mpg,  mkv, threegp("3gp"), m4v, vob, ts, ogv, swf,   //视频
    mp3, wma, m4a, aac, wav, flac, ogg ;//音频
    
    // 辅助根据string来获取到对应对象
    public static Map<String, SupportVideoType> StringToType = new HashMap<String, SupportVideoType>();
    static {
        for (SupportVideoType type : SupportVideoType.values()) {
            StringToType.put(type.toString().toLowerCase(), type);//都转成小写的
        }
    }
    
    String value;
    
    /**
     * 有参构造函数，用于保存值与enum名不一致（java 中不允许以数字开头）
     */
    private SupportVideoType(String value) {
        this.value = value;
    }
    
    /**
     * 无参构造，兼容
     */
    private SupportVideoType() {
        this.value = super.toString();
    }
    /**
     * FIXME 可以考虑是否忽略大小写
     * 
     * @param fileType
     * @return
     * @since
     */
    public static SupportVideoType fromString(String fileType) {
        if(StringUtils.isEmpty(fileType)){
            //FIXME change to lc exception
            throw new WafSimpleException("not support type:" + fileType);
        }
       SupportVideoType type = StringToType.get(fileType.trim().toLowerCase());
       if(type == null){
           //FIXME change to lc exception
           throw new WafSimpleException("not support type:" + fileType);
       }
        return type;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * just for test 
     * 
     * @author linsm
     * @param args
     * @since
     */
    public static void main(String[] args) {
        for(SupportVideoType supportVideoType:SupportVideoType.values()){
            System.out.println(supportVideoType.toString());
        }
        
        System.out.println(StringToType);
    }

}
