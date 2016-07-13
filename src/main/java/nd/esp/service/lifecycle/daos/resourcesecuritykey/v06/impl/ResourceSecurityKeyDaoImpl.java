package nd.esp.service.lifecycle.daos.resourcesecuritykey.v06.impl;

import java.util.ArrayList;
import java.util.List;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.resourcesecuritykey.v06.ResourceSecurityKeyDao;
import nd.esp.service.lifecycle.models.ResourceSecurityKeyModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>Title: ResourceSecurityKeyDaoImpl  </p>
 * <p>Description: ResourceSecurityKeyDaoImpl </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
@Service
public class ResourceSecurityKeyDaoImpl implements ResourceSecurityKeyDao {

	@Autowired
	private BaseDao<ResourceSecurityKeyModel> baseDao;

	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	private static String  TABLE_POSTFIX = "resource_security_key";
	
	/**
     * 新增资源文件密钥信息      
     * @param resourceSecurityKeyModel
     * @return
     * @author lianggz
     */
	public int insert(ResourceSecurityKeyModel resourceSecurityKeyModel){
        return this.baseDao.insert(resourceSecurityKeyModel, TABLE_POSTFIX);
    }
    
    /**
     * 查询资源文件密钥信息      
     * @param uuid
     * @return
     * @author lianggz
     */
    public ResourceSecurityKeyModel findSecurityKeyInfo(String uuid){
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" and identifier = ? limit 1");
        args.add(uuid);
        return  this.baseDao.queryOne(sql.toString(), args.toArray(), null, ResourceSecurityKeyModel.class, TABLE_POSTFIX);
    }
}