package nd.esp.service.lifecycle.services.resourcesecuritykey.impl;

import nd.esp.service.lifecycle.daos.resourcesecuritykey.ResourceSecurityKeyDao;
import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;
import nd.esp.service.lifecycle.services.resourcesecuritykey.ResourceSecurityKeyService;
import nd.esp.service.lifecycle.utils.encrypt.DigestUtil;
import nd.esp.service.lifecycle.utils.encrypt.RSAUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>Title: ResourceSecurityKeyServiceImpl   </p>
 * <p>Description: ResourceSecurityKeyServiceImpl </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
@Service(value="ResourceSecurityKeyServiceImpl")
public class ResourceSecurityKeyServiceImpl implements ResourceSecurityKeyService {

	@Autowired
	private ResourceSecurityKeyDao resourceSecurityKeyDao;

	/**
     * 查询或者新增密钥信息     
     * @param uuid
     * @param publicKey
     * @return
     * @author lianggz
	 * @throws Exception 
     */
    public String findOrInsert(String uuid, String publicKey) {
        ResourceSecurityKeyModel resourceSecurityKeyModel = this.resourceSecurityKeyDao.findSecurityKeyInfo(uuid);
        String securityKey = "";
        String desKey = "";
        // 存在密钥信息，直接获取密钥  
        if(resourceSecurityKeyModel != null){
            securityKey = resourceSecurityKeyModel.getSecurityKey();
            desKey = RSAUtil.encoder(securityKey, publicKey);
        }
        // 不存在密钥信息，则重新生成后保存。
        else{
            securityKey = DigestUtil.getSecurityKey(uuid);
            desKey = RSAUtil.encoder(securityKey, publicKey);
            resourceSecurityKeyModel = new ResourceSecurityKeyModel();
            resourceSecurityKeyModel.setIdentifier(uuid);
            resourceSecurityKeyModel.setSecurityKey(securityKey);
            this.resourceSecurityKeyDao.insert(resourceSecurityKeyModel);
        }
        return desKey;
    }
}
