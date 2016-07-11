package nd.esp.service.lifecycle.utils.encrypt;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import nd.esp.service.lifecycle.controller.v06.TestResourceSecurityKeyController;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

/**
 * <p>Title: RSA工具类  </p>
 * <p>Description: RSAUtil </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月11日           </p>
 * @author lianggz
 */
public class RSAUtil {

    private static Logger logger = LoggerFactory.getLogger(RSAUtil.class);
    
    private static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";
    private static final String KEY_ALGORITHM = "RSA";  

    /**
     * 解密RSA内容	  
     * @param key
     * @param text
     * @return
     * @throws Exception
     * @author lianggz
     */
    public static String encoder(String text, String key)  {
        try {
            // 还原Public Key
            byte[] publicBytes = Base64.decodeBase64(key.getBytes());
            // 设置Public Key
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
            // 用还原的Public Key加密，并返回
            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(text.getBytes());
            //return Base64.encodeBase64String(encryptedBytes);\
            return new String(encryptedBytes, "ISO-8859-1");
        } 
        catch (Exception e) {
            logger.error("RSAUtil.encoder", e);
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
                    ,"key" + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }
    
    /*public static void main(String[] args) throws Exception{   
        String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2BvIiWx04MCaXZrfr0xnmG9/SiDAILYPgCjAg9XDlIXzXy/kgP8Ee87Mrit+cbJABcT3J0zAFTtphd1w8TblIVHuvP0KlTRX/YoeTLg6OJbK+5ACiktN+zcZZlF/2rwTtHec74cAHKICgf7666moXfjyoEgnlS5KKAZLbrlH02RgRHBInAdY+XEGHub5VSiezHr0oMj0rbp1WJKzcZg1p+l+d7YM3kgr9ty4QZI9e23zY1ji8mAnF0H+zyEVERW4ZRIAqhP1h62/8J2IC+McXn2INxc/igSWtTNcvsFIftTctuZY4Qr+iD92CsB660Lr/iqwrjR5BYsekqsR4VZlRQIDAQAB";
        String txt = encoder("beae6f4fb9a6de4d5c2a44c4cf32d6b37077cd9d", pubKey);
        String txt2 = encoder("beae6f4fb9a6de4d5c2a44c4cf32d6b37077cd9d", pubKey);
        System.out.println(txt);  
        System.out.println(txt2); 
    }*/
}
