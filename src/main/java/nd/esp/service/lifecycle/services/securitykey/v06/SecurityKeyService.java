package nd.esp.service.lifecycle.services.securitykey.v06;

/**
 * <p>Title: SecurityKeyService   </p>
 * <p>Description: SecurityKeyService </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月21日           </p>
 * @author lanyl
 */
public interface SecurityKeyService {
    
    /**
     * 获取Rsa加密后的desKey
     * @param userId
     * @param publicKey
     * @return
     * @author lanyl
     */
    public String getRsaEncryptDesKey(String userId, String publicKey) ;

    /**
     * 获取desKey
     * @param userId
     * @return
     * @author lanyl
     */
    public String getDesKey(String userId) ;
}
