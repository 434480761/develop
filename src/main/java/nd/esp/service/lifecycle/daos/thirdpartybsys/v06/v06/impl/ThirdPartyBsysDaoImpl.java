package nd.esp.service.lifecycle.daos.thirdpartybsys.v06.v06.impl;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.thirdpartybsys.v06.v06.ThirdPartyBsysDao;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: ThirdPartyBsysDaoImpl</p>
 * <p>Description: ThirdPartyBsysDaoImpl</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
@Service
public class ThirdPartyBsysDaoImpl implements ThirdPartyBsysDao {

	@Autowired
	private BaseDao<ThirdPartyBsysModel> baseDao;

	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	private static String  TABLE_POSTFIX = "third_party_bsys";

	/**
	 * 查询第三方服务是否存在
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	public ThirdPartyBsysModel findThirdPartyBsys(String userId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" and user_id = ? limit 1");
		args.add(userId);
		return   this.baseDao.queryOne(sql.toString(), args.toArray(), null, ThirdPartyBsysModel.class, TABLE_POSTFIX);
	}
}
