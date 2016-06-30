package nd.esp.service.lifecycle.daos.userrestypemapping.v06.impl;

import nd.esp.service.lifecycle.daos.common.BaseDao;
import nd.esp.service.lifecycle.daos.userrestypemapping.v06.UserRestypeMappingDao;
import nd.esp.service.lifecycle.models.UserRestypeMappingModel;
import org.apache.commons.lang3.StringUtils;
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
public class UserRestypeMappingDaoImpl implements UserRestypeMappingDao {
	private static final Logger LOG = LoggerFactory.getLogger(UserRestypeMappingDaoImpl.class);

	@Autowired
	private BaseDao<UserRestypeMappingModel> baseDao;

	@Autowired
	@Qualifier("defaultJdbcTemplate")
	private JdbcTemplate jdbcTemplate;

	private static String  TABLE_POSTFIX = "user_restype_mapping";

	/**
	 * 新增用户请求类型映射关系
	 * @param
	 * @return
	 * @author lanyl
	 */
	public int insert(UserRestypeMappingModel userRestypeMappingModel){
		return this.baseDao.insert(userRestypeMappingModel, TABLE_POSTFIX);
	}


	/**
	 * 查询用户请求类型映射关系
	 * @param
	 * @return
	 * @author lanyl
	 */
	public UserRestypeMappingModel query(Integer id){
		return this.baseDao.queryOne(" and id = ?", new Object[]{id}, null, UserRestypeMappingModel.class, TABLE_POSTFIX);
	}

	/**
	 * 批量插入用户请求类型映射关系
	 * @param resTypeList
	 * @param userId
	 * @author lanyl
	 */
	public void batchSave(final  List<String> resTypeList, final  String userId){
		if(resTypeList != null && resTypeList.size() > 0){
			String sql = "insert into " + TABLE_POSTFIX + "(user_id, res_type,create_time) values (?,?,?) "
					+ "on duplicate key update create_time = ?";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					String resType = resTypeList.get(i);
					ps.setString(1, userId);
					ps.setString(2, resType);
					ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
					ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				}
				@Override
				public int getBatchSize() {
					return resTypeList.size();
				}

			});
		}
	}

	/**
	 * 批量删除用户请求类型映射关系
	 * @param resTypeList
	 * @param userId
	 * @author lanyl
	 */
	public void batchDelete(final List<String> resTypeList, final  String userId){
		if(resTypeList != null && resTypeList.size() > 0){
			String sql = "DELETE FROM " + TABLE_POSTFIX + "WHERE  user_id = ? AND res_type = ? ";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					String resType = resTypeList.get(i);
					ps.setString(1, userId);
					ps.setString(2, resType);
				}
				@Override
				public int getBatchSize() {
					return resTypeList.size();
				}
			});
		}
	}

	/**
	 * 删除用户请求类型映射关系
	 * @param userId
	 * @author lanyl
	 */
	public void delete(String userId){
		List<Object> args = new ArrayList<Object>();
		String sql = " and  user_id= ?";
		args.add(userId);
		this.baseDao.delete(sql, args.toArray(), TABLE_POSTFIX);
	}


	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userIdList
	 * @return
	 * @author lanyl
	 */
	public List<UserRestypeMappingModel> findUserRestypeMappingModelList(List<String> userIdList) {
		List<String> signList = new ArrayList<String>();
		for (int i = 0, size=userIdList.size(); i < size; i++) {signList.add("?");}
		String sign = StringUtils.join(signList,",");

		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" and user_id IN("+sign+") ");
		args.addAll(userIdList);
		if(!userIdList.isEmpty()){
			return baseDao.query(sql.toString(), args.toArray(), null, UserRestypeMappingModel.class, TABLE_POSTFIX);
		}else{
			return new ArrayList<UserRestypeMappingModel>();
		}
	}

	/**
	 * 查询用户请求类型映射关系信息列表
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	public List<String> findUserRestypeList(String userId) {
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT res_type FROM " + TABLE_POSTFIX + " WHERE  user_id = ? ");
		args.add(userId);
		if(StringUtils.isNotBlank(userId)){
			return this.jdbcTemplate.queryForList(sql.toString(), String.class, args.toArray());
		}else{
			return new ArrayList<String>();
		}
	}
}
