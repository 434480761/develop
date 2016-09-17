package nd.esp.service.lifecycle.daos.icrs2.v06.impl;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.poi.ss.formula.functions.T;
import org.joda.time.Hours;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.mysql.fabric.xmlrpc.base.Data;

import nd.esp.service.lifecycle.daos.icrs2.v06.Icrs2Dao;
import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.Asset;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.Icrs;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

@Repository
public class Icrs2DaoImple implements Icrs2Dao{

	
	
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager em;
	@Autowired
	private CategoryDataRepository categoryDataRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	 
	/**
	 * 实现Icrs2Dao的querySchoolTeacherResource接口方法，依教师、年级、学科从数据库中进行查询，
	 * 查询统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型。
	 * @author xm
	 * @date 2016年9月14
	 * @param schoolId,resType,fromDate,toDate,grade,subject,order, limit
	 * @return List<Map<String, Object>>
	 */
	
	@Override
	public List<Map<String, Object>> querySchoolTeacherResource(String schoolId,String resType,String fromDate,String toDate,String grade,String subject,
            String order,String limit) {
				
		//拼接sql语句
		StringBuffer sqlStringBuffer = new StringBuffer("select teacher_id as teacherId,teacher_name as teacherName,grade_code as gradeCode,subject_code as subjectCode,count(*) as data from icrs_resource  where 1 ");
		if (StringUtils.hasText(schoolId)) {
			sqlStringBuffer.append(" and school_id = "+"'"+schoolId+"'");
		}
		if (StringUtils.hasText(resType)) {
			sqlStringBuffer.append(" and res_type="+"'"+resType+"'");
		}
		if (StringUtils.hasText(fromDate)) {
			sqlStringBuffer.append(" and create_date>="+"'"+fromDate+"'"); 
		}   
		if (StringUtils.hasText(toDate)) {	
			sqlStringBuffer.append(" and create_date<="+"'"+toDate+"'");
		}
		if (StringUtils.hasText(grade)) {	
			sqlStringBuffer.append(" and grade_code="+"'"+grade+"'");   
		}
		if (StringUtils.hasText(subject)) {    	
			sqlStringBuffer.append(" and subject_code="+"'"+subject+"'");
		}
		
		//group by分组依据老师的id，subject_id和grade_id进行分组
		sqlStringBuffer.append(" group by teacher_id,grade_code,subject_code");
		
		//依数量排序方式，desc 降序（默认） / asc 升序
		if (!StringUtils.hasText(order)) { 
			sqlStringBuffer.append(" order by count(*) "+" "+"desc");
		}else {
			sqlStringBuffer.append(" order by count(*) "+" "+order);
		}
			
		
		if (StringUtils.hasText(limit)) {
			sqlStringBuffer.append("  limit 0,").append(limit);
		}
				
		//sql查询并把查询结果给list
		String querySql = sqlStringBuffer.toString();
		final List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		jdbcTemplate.query(querySql, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String,Object> rowMap = new HashMap<String,Object>();
				rowMap.put("teacherId", rs.getString("teacherId"));
		        rowMap.put("teacherName", rs.getString("teacherName"));
		        rowMap.put("gradeCode",  rs.getString("gradeCode"));
		        rowMap.put("subjectCode", rs.getString("subjectCode"));
		        rowMap.put("data", rs.getInt(5));
		        resultList.add(rowMap);
	            return null;
	            }
	        });
		return resultList;
		
	
	}
	

	/**
	 * 实现Icrs2Dao的queryResourcePerHour接口方法，查询本校资源一天内各时段的产出数量，
	 * 查询统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型
	 * @author xm
	 * @date 2016年9月14
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return List<Map<String, Object>>
	 */
	@Override
	public List<Map<String, Object>> queryResourcePerHour(String schoolId,String resType, String queryDate) {

		//sql拼接
		StringBuffer sqlStringBuffer = new StringBuffer("select create_hour as hour,count(*) as data from icrs_resource  where 1 ");	
		if (StringUtils.hasText(schoolId)) {
			sqlStringBuffer.append(" and school_id = "+"'"+schoolId+"'");
		}
		if (StringUtils.hasText(resType)) {
			sqlStringBuffer.append(" and res_type="+"'"+resType+"'");
		}
		if (StringUtils.hasText(queryDate)) {
			sqlStringBuffer.append(" and create_date="+"'"+queryDate+"'");
		} 
		
		//sql查询
		sqlStringBuffer.append(" group by create_hour order by create_hour");
		String querySql = sqlStringBuffer.toString();
		final List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
		jdbcTemplate.query(querySql, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String,Object> rowMap = new HashMap<String,Object>();
				int isHourValid = rs.getInt(1);
				if (isHourValid<1||isHourValid>24) {
					return null; 
				}
				rowMap.put("hour", rs.getString("hour"));
	            rowMap.put("data", rs.getInt("data"));
	            resultList.add(rowMap);   
	            return null;
	            }
	        });

		return resultList;
		
		
	}
	
   

}
