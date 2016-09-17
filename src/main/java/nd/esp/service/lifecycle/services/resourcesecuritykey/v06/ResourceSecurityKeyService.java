package nd.esp.service.lifecycle.services.resourcesecuritykey.v06;

/**
 * <p>Title: ResourceSecurityKeyService   </p>
 * <p>Description: ResourceSecurityKeyService </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
public interface ResourceSecurityKeyService {
    
    /**
     * 查询或者新增密钥信息 	  
     * @param uuid
     * @param publicKey
     * @return
     * @author lianggz
     */
    public String findOrInsert(String uuid, String publicKey) ;
}
