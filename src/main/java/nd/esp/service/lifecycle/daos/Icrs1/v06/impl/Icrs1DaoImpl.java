package nd.esp.service.lifecycle.daos.Icrs1.v06.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.daos.Icrs1.v06.Icrs1Dao;
import nd.esp.service.lifecycle.models.icrs1.v06.DailyDataModel;
import nd.esp.service.lifecycle.models.icrs1.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs1.v06.TextbookModel;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class Icrs1DaoImpl implements Icrs1Dao {

	private static final Logger LOG = LoggerFactory
			.getLogger(Icrs1DaoImpl.class);

	@Qualifier(value = "defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;

	@Override
	public List<ResourceTotalModel> getResourceTotal(String schoolId,
			String fromDate, String toDate) {
		String querySql =  "select res_type as resType ,count(*) as resTotal  from icrs_resource  where school_id=:schoolId";
		
		final List<ResourceTotalModel> totalList = new ArrayList<ResourceTotalModel>();
		Map<String, Object> params = new HashMap<String, Object>();
		
		if (StringUtils.hasText(fromDate) && StringUtils.hasText(toDate)) {
			querySql += " and create_date between :fromDate  and :toDate";
			params.put("fromDate", fromDate);
			params.put("toDate", toDate);
		} 
		querySql += " group by res_type";
		params.put("schoolId", schoolId);
		
		LOG.info("查询的SQL语句：" + querySql.toString());
		LOG.info("查询的SQL参数:" + ObjectUtils.toJson(params));
		
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(
				defaultJdbcTemplate);
		namedJdbcTemplate.query(querySql, params, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				ResourceTotalModel rtm = new ResourceTotalModel();
				rtm.setResType(rs.getString("resType"));
				rtm.setResTotal(rs.getInt("resTotal"));
				totalList.add(rtm);
				return null;
			}
		});
		return totalList;
	}

	@Override
	public List<DailyDataModel> getResourceStatisticsByDay(String schoolId,
			String resType, String fromDate, String toDate) {
		String sql = "select DATE(create_date) as date,count(create_date) as data from icrs_resource where  school_id=:schoolId and";
		final List<DailyDataModel> dailyList = new ArrayList<DailyDataModel>();
		Map<String, Object> params = new HashMap<String, Object>();
		if (resType == null) {
			sql=sql+" create_date between :fromDate  and :toDate  group by date";
			
		} else {
			sql=sql+" res_type=:resType and create_date between :fromDate  and :toDate  group by date";
			params.put("resType", resType);
		}
		params.put("schoolId", schoolId);
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);
		LOG.info("查询的SQL语句：" + sql.toString());
		LOG.info("查询的SQL参数:" + ObjectUtils.toJson(params));
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(
				defaultJdbcTemplate);
		namedJdbcTemplate.query(sql, params, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				DailyDataModel ddm = new DailyDataModel();
				ddm.setDate(rs.getString("date"));
				ddm.setData(rs.getInt("data"));
				dailyList.add(ddm);
				return null;
			}
		});
		return dailyList;
	}

	@Override
	public List<TextbookModel> getTeacherResource(String schoolId, String teacherId,
			String resType) {
		String querySql = "SELECT ndr.identifier AS uuid,ndr.title AS title FROM ndresource AS ndr INNER JOIN icrs_resource AS icrs ON "
				+ "ndr.identifier=icrs.teachmaterial_uuid WHERE ndr.primary_category='teachingmaterials' AND icrs.teacher_id=:teacherId AND icrs.school_id=:schoolId AND ndr.enable=1";
		final List<TextbookModel> resourceList = new ArrayList<TextbookModel>();
		Map<String, Object> params = new HashMap<String, Object>();
		if (StringUtils.hasText(resType)) {		
			querySql=querySql+" AND  icrs.res_type=:resType";
			params.put("resType", resType);			
		} 
		params.put("schoolId", schoolId);
		params.put("teacherId", teacherId);
		LOG.info("查询的SQL语句：" + querySql.toString());
		LOG.info("查询的SQL参数:" + ObjectUtils.toJson(params));
		NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(
				defaultJdbcTemplate);
		namedJdbcTemplate.query(querySql, params, new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				TextbookModel tbm = new TextbookModel();
				tbm.setUuid(rs.getString("uuid"));
				tbm.setTitle(rs.getString("title"));
				resourceList.add(tbm);
				return null;
			}
		});
		return resourceList;
	}
}
