package nd.esp.service.lifecycle.daos.usercoveragemapping.v06.impl;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.usercoveragemapping.v06.UserCoverageMappingDao;
import nd.esp.service.lifecycle.models.UserCoverageMappingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
@Service
public class UserCoverageMappingDaoImpl implements UserCoverageMappingDao {
	private static final Logger LOG = LoggerFactory.getLogger(UserCoverageMappingDaoImpl.class);

	@Autowired
	private BaseDao<UserCoverageMappingModel> baseDao;

	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	private static String  TABLE_POSTFIX = "user_coverage_mapping";

	/**
	 * 新增用户覆盖类型映射关系
	 * @param
	 * @return
	 * @author lanyl
	 */
	public int insert(UserCoverageMappingModel userCoverageMappingModel){
		return this.baseDao.insert(userCoverageMappingModel, TABLE_POSTFIX);
	}


	/**
	 * 查询用户覆盖类型映射关系
	 * @param
	 * @return
	 * @author lanyl
	 */
	public UserCoverageMappingModel query(Integer id){
		return this.baseDao.queryOne(" and id = ?", new Object[]{id}, null, UserCoverageMappingModel.class, TABLE_POSTFIX);
	}

	/**
	 * 批量插入用户覆盖类型映射关系
	 * @param coverageList
	 * @param userId
	 * @author lanyl
	 */
	public void batchSave(final  List<String> coverageList, final  String userId){
		if(coverageList != null && coverageList.size() > 0){
			String sql = "insert into " + TABLE_POSTFIX + "(user_id, coverage,create_time) values (?,?,?) "
					+ "on duplicate key update create_time = ?";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					String coverage = coverageList.get(i);
					ps.setString(1, userId);
					ps.setString(2, coverage);
					ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				}
				@Override
				public int getBatchSize() {
					return coverageList.size();
				}

			});
		}
	}


	/**
	 * 批量删除用户覆盖类型映射关系
	 * @param coverageList
	 * @param userId
	 * @author lanyl
	 */
	public void batchDelete(final List<String> coverageList, final String userId){
		if(coverageList != null && coverageList.size() > 0){
			String sql = "DELETE FROM " + TABLE_POSTFIX + " WHERE user_id = ? AND  coverage = ?";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					String coverage = coverageList.get(i);
					ps.setString(1, userId);
					ps.setString(2, coverage);
				}
				@Override
				public int getBatchSize() {
					return coverageList.size();
				}

			});
		}
	}

	/**
	 * 删除用户覆盖类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	public void delete( String userId){
		List<Object> args = new ArrayList<Object>();
		String sql = " and  user_id= ?";
		args.add(userId);
		this.baseDao.delete(sql, args.toArray(), TABLE_POSTFIX);
	}
}
