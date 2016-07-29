package nd.esp.service.lifecycle.services.securitykey.v06.impl;

import nd.esp.service.lifecycle.daos.securitykey.v06.SecurityKeyDao;
import nd.esp.service.lifecycle.models.SecurityKeyModel;
import nd.esp.service.lifecycle.services.securitykey.v06.SecurityKeyService;
import nd.esp.service.lifecycle.utils.TimeUtils;
import nd.esp.service.lifecycle.utils.encrypt.DESUtils;
import nd.esp.service.lifecycle.utils.encrypt.RSAUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * <p>Title: SecurityKeyServiceImpl   </p>
 * <p>Description: SecurityKeyServiceImpl </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月21日           </p>
 * @author lanyl
 */
@Service(value="SecurityKeyServiceImpl")
public class SecurityKeyServiceImpl implements SecurityKeyService {

	@Autowired
	private SecurityKeyDao securityKeyDao;

	/**
     * 获取Rsa加密后的desKey
     * @param userId
     * @param publicKey
     * @return
     * @author lanyl
	 * @throws Exception 
     */
    public String getRsaEncryptDesKey(String userId, String publicKey) {
        SecurityKeyModel securityKeyModel = this.securityKeyDao.findSecurityKeyInfo(userId);
        String securityKey = "";
        String desKey = "";
        // 存在密钥信息
        if(securityKeyModel != null ){
            //判断deskey是否超过24小时
            if(TimeUtils.getTimeIntervalWith24Hour(securityKeyModel.getUpdateTime())){
                securityKey = DESUtils.getSecurityKey();
                //更新密钥
                securityKeyModel.setSecurityKey(securityKey);
                this.securityKeyDao.update(securityKeyModel);
            }else {
                //deskey没有超过24小时，直接获取密钥
                securityKey = securityKeyModel.getSecurityKey();
            }
            desKey = RSAUtil.encoder(securityKey, publicKey);
        } else{
            // 不存在密钥信息，则重新生成后保存。
            securityKey = DESUtils.getSecurityKey();
            desKey = RSAUtil.encoder(securityKey, publicKey);
            securityKeyModel = new SecurityKeyModel();
            securityKeyModel.setIdentifier(UUID.randomUUID().toString());
            securityKeyModel.setUserId(userId);
            securityKeyModel.setSecurityKey(securityKey);
            this.securityKeyDao.insert(securityKeyModel);
        }
        return desKey;
    }

    /**
     * 获取desKey
     * @param userId
     * @return
     * @author lanyl
     * @throws Exception
     */
    public String getDesKey(String userId) {
        SecurityKeyModel securityKeyModel = this.securityKeyDao.findSecurityKeyInfo(userId);
        String securityKey = "";
        // 存在密钥信息
        if(securityKeyModel != null ){
            //判断deskey是否超过24小时
            if(TimeUtils.getTimeIntervalWith24Hour(securityKeyModel.getUpdateTime())){
                securityKey = DESUtils.getSecurityKey();
                //更新密钥
                securityKeyModel.setSecurityKey(securityKey);
                securityKeyModel.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                this.securityKeyDao.update(securityKeyModel);
            }else {
                //deskey没有超过24小时，直接获取密钥
                securityKey = securityKeyModel.getSecurityKey();
            }
        } else{
            // 不存在密钥信息，则重新生成后保存。
            securityKey = DESUtils.getSecurityKey();
            securityKeyModel = new SecurityKeyModel();
            securityKeyModel.setIdentifier(UUID.randomUUID().toString());
            securityKeyModel.setUserId(userId);
            securityKeyModel.setSecurityKey(securityKey);
            this.securityKeyDao.insert(securityKeyModel);
        }
        return securityKey;
    }
}
