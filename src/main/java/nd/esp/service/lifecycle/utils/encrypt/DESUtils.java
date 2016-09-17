package nd.esp.service.lifecycle.utils.encrypt;

import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * <p>Title: DES加密工具类   </p>
 * <p>Description: DES加密工具类 </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月11日           </p>
 * @author lianggz
 */
public class DESUtils {     
    
    private static Logger logger = LoggerFactory.getLogger(DESUtils.class);
    
    /**
     * 生成资源文件密钥
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

    /**
     * 对字符串加密
     * @param input
     * @param publicKey 要求publicKey至少长度为8个字符
     * @return
     * @throws Exception
     * @author lianggz
     */
    public static String encryptData(byte[] input, String publicKey) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec keySpec = new DESKeySpec(publicKey.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("DES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
        byte[] cipherData = cipher.doFinal(input);
        return new String(Base64.encodeBase64String(cipherData));
    }

    /**
     * 对字符串解密
     * @param input
     * @param publicKey 要求publicKey至少长度为8个字符
     * @return
     * @throws Exception
     * @author lianggz
     */
    public static String decryptData(String input, String publicKey) throws Exception {
        SecureRandom random = new SecureRandom();
        DESKeySpec keySpec = new DESKeySpec(publicKey.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("DES");

        cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
        byte [] plainData = cipher.doFinal(Base64.decodeBase64(input));
        return new String(plainData);
    }
}  