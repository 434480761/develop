package nd.esp.service.lifecycle.utils.encrypt;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>Title: DigestUtil  </p>
 * <p>Description: DigestUtil </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
public class DigestUtil {
    
    private DigestUtil(){}
    
    /**
     * 生成资源文件密钥      
     * @param uuid
     * @return
     * @author lianggz
     */
    public static String getSecurityKey(String uuid){
        // 步骤一：获取时间戳
        String currentTimeMillis = System.currentTimeMillis() + "";
        // 步骤二：对UUID + public key进行MD5加密
        String secUKey = DigestUtil.md5Hex16(uuid);
        // 步骤三：进行sha1加密
        String devSecretKey = DigestUtil.sha1Hex(secUKey + currentTimeMillis);
        return devSecretKey;
    }
    
    /**
     * MD5加密	    
     * @param str
     * @return
     * @author lianggz
     */
    public static String md5(String str){
        if(StringUtils.isEmpty(str)){return str;}
        return DigestUtils.md5Hex(str);
    }
    
    /**
     * MD5加密【16位】        
     * @param str
     * @return
     * @author lianggz
     */
    public static String md5Hex16(String str){
        if(StringUtils.isEmpty(str)){return str;}
        return DigestUtils.md5Hex(str).substring(8, 24);
    }
    
    /**
     * sha1加密        
     * @param str
     * @return
     * @author lianggz
     */
    public static String sha1Hex(String str){
        if(StringUtils.isEmpty(str)){return str;}
        return DigestUtils.sha1Hex(str); 
    }
}