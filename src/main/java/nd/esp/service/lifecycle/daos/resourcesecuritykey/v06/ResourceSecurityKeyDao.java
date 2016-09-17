package nd.esp.service.lifecycle.daos.resourcesecuritykey.v06;

import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;

/**
 * <p>Title: ResourceSecurityKeyDao  </p>
 * <p>Description: ResourceSecurityKeyDao </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
public interface ResourceSecurityKeyDao {

    /**
     * 新增资源文件密钥信息      
     * @param resourceSecurityKeyModel
     * @return
     * @author lianggz
     */
    public int insert(ResourceSecurityKeyModel resourceSecurityKeyModel);
    
    /**
     * 查询资源文件密钥信息      
     * @param uuid
     * @return
     * @author lianggz
     */
	public ResourceSecurityKeyModel findSecurityKeyInfo(String uuid);

}
