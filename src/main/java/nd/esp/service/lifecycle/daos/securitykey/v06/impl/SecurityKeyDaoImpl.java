package nd.esp.service.lifecycle.daos.securitykey.v06.impl;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.securitykey.v06.SecurityKeyDao;
import nd.esp.service.lifecycle.models.SecurityKeyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: SecurityKeyDaoImpl  </p>
 * <p>Description: SecurityKeyDaoImpl </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月21日           </p>
 * @author lanyl
 */
@Service
public class SecurityKeyDaoImpl implements SecurityKeyDao {

	@Autowired
	private BaseDao<SecurityKeyModel> baseDao;

	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	private static String  TABLE_POSTFIX = "security_key";
	
	/**
     * 新增用户密钥信息
     * @param securityKeyModel
     * @return
     * @author lanyl
     */
	public int insert(SecurityKeyModel securityKeyModel){
        return this.baseDao.insert(securityKeyModel, TABLE_POSTFIX);
    }

	/**
	 * 更新用户密钥信息
	 * @param securityKeyModel
	 * @return
	 * @author lanyl
	 */
	public int update(SecurityKeyModel securityKeyModel){
		return this.baseDao.update(" and user_id = ?", new Object[]{securityKeyModel.getUserId()}, securityKeyModel, TABLE_POSTFIX);
	}

    
    /**
     * 查询用户密钥信息
     * @param userId
     * @return
     * @author lanyl
     */
    public SecurityKeyModel findSecurityKeyInfo(String userId){
        List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" and user_id = ? limit 1");
        args.add(userId);
        return  this.baseDao.queryOne(sql.toString(), args.toArray(), null, SecurityKeyModel.class, TABLE_POSTFIX);
    }
}