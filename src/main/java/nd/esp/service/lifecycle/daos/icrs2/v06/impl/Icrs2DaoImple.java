package nd.esp.service.lifecycle.daos.icrs2.v06.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.daos.icrs2.v06.Icrs2Dao;
import nd.esp.service.lifecycle.models.icrs2.v06.HourDataModel;
import nd.esp.service.lifecycle.models.icrs2.v06.TeacherOutputResource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class Icrs2DaoImple implements Icrs2Dao {

	private static final Logger LOG = LoggerFactory
			.getLogger(Icrs2DaoImple.class);

	@Qualifier(value = "defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private CategoryDataRepository categoryDataRepository;

	/**
	 * 实现Icrs2Dao的querySchoolTeacherResource接口方法，依教师、年级、学科从数据库中进行查询，
	 * 查询统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型。
	 * 
	 * @author xm
	 * @date 2016年9月14
	 * @param schoolId
	 *            ,resType,fromDate,toDate,grade,subject,order, limit
	 * @return List<Map<String, Object>>
	 */
	@Override
	public List<TeacherOutputResource> querySchoolTeacherResource(
			String schoolId, String resType, String fromDate, String toDate,
			String grade, String subject, String order, String limit) {

		// 拼接sql语句
		StringBuffer sqlStringBuffer = new StringBuffer(
				"select teacher_id as teacherId,teacher_name as teacherName,grade_code as gradeCode,subject_code as subjectCode,count(*) as data ");
		sqlStringBuffer.append(" from icrs_resource where school_id=:schoolId");

		if (StringUtils.hasText(resType)) {
			sqlStringBuffer.append(" and res_type=:resType");
		}
		if (StringUtils.hasText(fromDate)) {
			sqlStringBuffer.append(" and create_date>=:fromDate");
		}
		if (StringUtils.hasText(toDate)) {
			sqlStringBuffer.append(" and create_date<=:toDate");
		}
		if (StringUtils.hasText(grade)) {
			sqlStringBuffer.append(" and grade_code=:gradeCode");
		}
		if (StringUtils.hasText(subject)) {
			sqlStringBuffer.append(" and subject_code=:subjectCode");
		}

		// group by分组依据老师的id，subject_id和grade_id进行分组
		sqlStringBuffer.append(" group by teacher_id,grade_code,subject_code");

		// 依数量排序方式，desc 降序（默认） / asc 升序
		if (!StringUtils.hasText(order)) {
			sqlStringBuffer.append(" order by count(*) desc");
		} else {
			sqlStringBuffer.append(" order by count(*) " + order);
		}

		Integer result[] = ParamCheckUtil.checkLimit(limit);
		sqlStringBuffer.append(" limit ").append(result[0]).append(",")
				.append(result[1]);

		// SQL参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("schoolId", schoolId);
		if (StringUtils.hasText(resType)) {
			params.put("resType", resType);
		}
		if (StringUtils.hasText(fromDate)) {
			params.put("fromDate", fromDate);
		}
		if (StringUtils.hasText(toDate)) {
			params.put("toDate", toDate);
		}
		if (StringUtils.hasText(grade)) {
			params.put("gradeCode", grade);
		}
		if (StringUtils.hasText(subject)) {
			params.put("subjectCode", subject);
		}

		// sql查询并把查询结果给list
		String querySql = sqlStringBuffer.toString();
		LOG.info("sql语句为" + querySql);

		final List<TeacherOutputResource> resultList = new ArrayList<TeacherOutputResource>();
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(
				jdbcTemplate);
		namedJdbcTemplate.query(querySql, params, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				TeacherOutputResource tor = new TeacherOutputResource();

				tor.setTeacherId(rs.getString("teacherId"));
				tor.setTeacherName(rs.getString("teacherName"));
				tor.setGradeCode(rs.getString("gradeCode"));
				tor.setSubjectCode(rs.getString("subjectCode"));

				if (StringUtils.hasText(rs.getString("gradeCode"))) {
					String gradeName = accordingCodeFindCodeName(rs
							.getString("gradeCode"));// 通过subject_code获得这个值
					tor.setGrade(gradeName);
				}
				if (StringUtils.hasText(rs.getString("subjectCode"))) {
					String subjectName = accordingCodeFindCodeName(rs
							.getString("subjectCode"));
					tor.setSubject(subjectName);
				}

				tor.setData(rs.getInt("data"));

				resultList.add(tor);
				return null;
			}
		});

		return resultList;
	}
	
	@Override
	public long countSchoolTeacherResource(String schoolId, String resType,
			String fromDate, String toDate, String grade, String subject) {
		// 拼接sql语句
		StringBuffer sqlStringBuffer = new StringBuffer(
				"select count(*) as total from ");
		sqlStringBuffer.append("(select teacher_id as teacherId,teacher_name as teacherName,grade_code as gradeCode,subject_code as subjectCode");
		sqlStringBuffer.append(" from icrs_resource where school_id=:schoolId");

		if (StringUtils.hasText(resType)) {
			sqlStringBuffer.append(" and res_type=:resType");
		}
		if (StringUtils.hasText(fromDate)) {
			sqlStringBuffer.append(" and create_date>=:fromDate");
		}
		if (StringUtils.hasText(toDate)) {
			sqlStringBuffer.append(" and create_date<=:toDate");
		}
		if (StringUtils.hasText(grade)) {
			sqlStringBuffer.append(" and grade_code=:gradeCode");
		}
		if (StringUtils.hasText(subject)) {
			sqlStringBuffer.append(" and subject_code=:subjectCode");
		}

		// group by分组依据老师的id，subject_id和grade_id进行分组
		sqlStringBuffer.append(" group by teacher_id,grade_code,subject_code) temp");
		
		// SQL参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("schoolId", schoolId);
		if (StringUtils.hasText(resType)) {
			params.put("resType", resType);
		}
		if (StringUtils.hasText(fromDate)) {
			params.put("fromDate", fromDate);
		}
		if (StringUtils.hasText(toDate)) {
			params.put("toDate", toDate);
		}
		if (StringUtils.hasText(grade)) {
			params.put("gradeCode", grade);
		}
		if (StringUtils.hasText(subject)) {
			params.put("subjectCode", subject);
		}

		// sql查询并把查询结果给list
		String querySql = sqlStringBuffer.toString();
		LOG.info("sql语句为" + querySql);
		
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(
				jdbcTemplate);
		@SuppressWarnings("deprecation")
		long total = namedJdbcTemplate.queryForLong(querySql, params);

		return total;
	}

	/**
	 * 实现Icrs2Dao的queryResourcePerHour接口方法，查询本校资源一天内各时段的产出数量，
	 * 查询统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型
	 * 
	 * @author xm
	 * @date 2016年9月14
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return List<Map<String, Object>>
	 */
	@Override
	public List<HourDataModel> queryResourcePerHour(String schoolId,
			String resType, String queryDate) {

		// sql拼接
		StringBuffer sqlStringBuffer = new StringBuffer(
				"select create_hour as hour,count(*) as data from icrs_resource ");
		
		sqlStringBuffer.append(" where school_id=:schoolId");
		
		if (StringUtils.hasText(resType)) {
			sqlStringBuffer.append(" and res_type=:resType");
		}
		if (StringUtils.hasText(queryDate)) {
			sqlStringBuffer.append(" and create_date=:queryDate");
		}
		sqlStringBuffer.append(" group by create_hour order by create_hour");
		
		// SQL参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("schoolId", schoolId);
		if (StringUtils.hasText(resType)) {
			params.put("resType", resType);
		}
		if (StringUtils.hasText(queryDate)) {
			params.put("queryDate", queryDate);
		}
		
		String querySql = sqlStringBuffer.toString();
		LOG.info("sql语句为" + querySql);
		
		final List<HourDataModel> resultList = new ArrayList<HourDataModel>();
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(
				jdbcTemplate);
		namedJdbcTemplate.query(querySql,params, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				HourDataModel hdm = new HourDataModel();
				hdm.setHour(rs.getString("hour"));
				hdm.setData(rs.getInt("data"));
				resultList.add(hdm);
				return null;
			}
		});

		return resultList;
	}

	/**
	 * 根据code查找title，例如subject_code=$SB02000,查找subject为音乐
	 * 
	 * @author xm
	 * @version
	 * @date 2016年9月15日 下午6:01:17
	 * @method accordingCodeFindCodeName
	 * @see
	 * @param sourceString
	 * @return String
	 * @throws
	 */
	private String accordingCodeFindCodeName(String sourceString) {
		String name = null;
		CategoryData cData = new CategoryData(); // 根据grade来查找中文名称
		cData.setNdCode(sourceString);
		try {
			cData = categoryDataRepository.getByExample(cData);
			if (cData == null) {
				return "";
			}
			name = cData.getTitle();
		} catch (EspStoreException e) {

			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
					e.getMessage());
		}

		return name;
	}
}
