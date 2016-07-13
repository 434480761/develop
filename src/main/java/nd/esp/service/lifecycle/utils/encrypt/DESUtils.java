package nd.esp.service.lifecycle.utils.encrypt;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * <p>Title: DES加密工具类   </p>
 * <p>Description: DES加密工具类 </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月11日           </p>
 * @author lianggz
 */
public class DESUtils {     
    
    private static Logger logger = LoggerFactory.getLogger(RSAUtil.class);
    
    /**
     * 生成资源文件密钥      
     * @param uuid
     * @return
     * @author lianggz
     */
    public static String getSecurityKey(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("DES");
            SecretKey secretKey = keyGen.generateKey();
            byte[] bytes = secretKey.getEncoded();
            return Base64.encodeBase64String(bytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("DESUtils.getSecurityKey", e);
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,"server_key" + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
        
    }
}  