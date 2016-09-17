package nd.esp.service.lifecycle.daos.securitykey.v06;

import nd.esp.service.lifecycle.models.SecurityKeyModel;

/**
 * <p>Title: SecurityKeyDao  </p>
 * <p>Description: SecurityKeyDao </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月21日           </p>
 * @author lanyl
 */
public interface SecurityKeyDao {

    /**
     * 新增用户密钥信息
     * @param securityKeyModel
     * @return
     * @author lanyl
     */
    public int insert(SecurityKeyModel securityKeyModel);

	/**
	 * 更新用户密钥信息
	 * @param securityKeyModel
	 * @return
	 * @author lanyl
	 */
	public int update(SecurityKeyModel securityKeyModel);
    
    /**
     * 查询用户密钥信息
     * @param userId
     * @return
     * @author lanyl
     */
	public SecurityKeyModel findSecurityKeyInfo(String userId);

}
