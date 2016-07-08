package nd.esp.service.lifecycle.utils.encrypt;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Administrator on 2016/7/6 0006.
 */
public class EncryptUtils {
    private static final String DES_ECB_PKCS5_PADDING = "DES/ECB/PKCS5Padding";

    public String encryptByRSA(String input, String publicKey) {
        String output = "";
        byte[] keyData = Base64.decodeBase64(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyData);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/None/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            output = String.valueOf(cipher.doFinal(input.getBytes("UTF-8")));
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return  output;
    }

    public void decryptByDES(InputStream input, OutputStream output, String strSecretKey) {

        CipherInputStream is = null;
        FileOutputStream os = null;
        try {
            Cipher desCipher = Cipher.getInstance(DES_ECB_PKCS5_PADDING);
            SecretKeySpec secretKey = null;
            byte keyData[] = null;
            // decode
            keyData = Base64.decodeBase64(strSecretKey);

            secretKey = new SecretKeySpec(keyData, "DES");

            desCipher.init(Cipher.DECRYPT_MODE, secretKey);

            is = new CipherInputStream(input, desCipher);

            copy(is, output);
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }  finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void copy(InputStream is, OutputStream os) throws IOException {
        int i;
        byte[] b = new byte[1024];
        while ((i = is.read(b)) != -1) {
            os.write(b, 0, i);
        }
    }
}
