package nd.esp.service.lifecycle.daos.thirdpartybsys.v06.impl;

import java.util.List;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.thirdpartybsys.v06.ThirdPartyBsysDao;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * <p>Title: ThirdPartyBsysDaoImpl   </p>
 * <p>Description: ThirdPartyBsysDaoImpl </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年6月30日           </p>
 * @author lianggz
 */
@Service
public class ThirdPartyBsysDaoImpl implements ThirdPartyBsysDao {
    
	@Autowired
	private BaseDao<ThirdPartyBsysModel> baseDao;

	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	private static String TABLE_POSTFIX = "third_party_bsys";

	/**
     * 查询第三方列表
     * @return
     * @author lianggz
     */
    @Override
    public List<ThirdPartyBsysModel> findThirdPartyBsysList() {
        //List<Object> args = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        return baseDao.query(sql.toString(), null, ThirdPartyBsysModel.class, TABLE_POSTFIX);
        
    }

	
}
