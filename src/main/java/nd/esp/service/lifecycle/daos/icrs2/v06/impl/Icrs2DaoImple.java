package nd.esp.service.lifecycle.daos.icrs2.v06.impl;

import java.sql.Date;
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
import nd.esp.service.lifecycle.vos.ListViewModel;

@Repository
public class Icrs2DaoImple implements Icrs2Dao{

	
	
	@PersistenceContext(unitName="entityManagerFactory")
	EntityManager em;
	@Autowired
	private CategoryDataRepository categoryDataRepository;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	 
	@Override
	public List<Map<String, Object>> queryBySchoolId(String schoolId,String resType,Date fromDate,Date toDate,String grade,String subject,
            String order,String limit) {
				
		//拼接sql语句
		StringBuffer sqlStringBuffer = new StringBuffer("select teacher_id as teacherId,teacher_name as teacherName,grade_code as gradeCode,subject_code as subjectCode,count(*) from icrs_resource  where 1 ");
		if (schoolId!=null) {
			sqlStringBuffer.append(" and school_id = "+"'"+schoolId+"'");
		}
		if (resType!=null) {
			sqlStringBuffer.append(" and res_type="+"'"+resType+"'");
		}
		if (fromDate!=null) {
			sqlStringBuffer.append(" and create_date>="+"\""+fromDate+"\""); // \转义
		}   
		if (toDate!=null) {
			sqlStringBuffer.append(" and create_date<="+"\""+toDate+"\"");
		}
		if (grade!=null) {
			
			sqlStringBuffer.append(" and grade="+"'"+grade+"'");   
		}
		if (subject!=null) {    	
			
			sqlStringBuffer.append(" and subject_code="+"'"+subject+"'");
		}
		if (order!=null) {
			sqlStringBuffer.append(" order by create_date "+"'"+order+"'");
		}
		if (limit!=null) {
			sqlStringBuffer.append("  limit 0,").append(limit);
		}
		
		//sql查询并把查询结果给list
		String querySql = sqlStringBuffer.toString();
		final List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		jdbcTemplate.query(querySql, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String,Object> rowMap = new HashMap<String,Object>();
				rowMap.put("teacherId", rs.getString("teacherId"));
	            rowMap.put("teacherName", rs.getString("teacherName"));
	            rowMap.put("gradeCode",  rs.getString("gradeCode"));
	            rowMap.put("subjectCode", rs.getString("subjectCode"));
	            rowMap.put("data", rs.getInt(5));
	            returnList.add(rowMap);
	            return null;
	            }

	        });	
		return returnList;
	
	}
	

	@Override
	public List<Map<String, Object>> getResourcePerHour(String schoolId,String resType, Date queryDate) {

		//sql拼接
		StringBuffer sqlStringBuffer = new StringBuffer("select create_hour as hour,count(*) from icrs_resource  where 1 ");	
		if (schoolId!=null) {
			sqlStringBuffer.append(" and school_id = "+"'"+schoolId+"'");
		}
		if (resType!=null) {
			sqlStringBuffer.append(" and res_type="+"'"+resType+"'");
		}
		if (queryDate!=null) {
			sqlStringBuffer.append(" and create_date="+"\""+queryDate+"\"");
		} 
		
		//sql查询
		sqlStringBuffer.append(" group by create_hour order by create_hour");
		String querySql = sqlStringBuffer.toString();
		final List<Map<String,Object>> returnList = new ArrayList<Map<String,Object>>();
		jdbcTemplate.query(querySql, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String,Object> rowMap = new HashMap<String,Object>();
			//create_hour在数据定义是tinity取值范围为（-127，127），若是float类型或者double类型，则向上取整Math.ceil(float a)
				int isHourValid = rs.getInt(1);
				if (isHourValid<1||isHourValid>24) {
			//不取这个值,不注入到查询返回的结果中去
					return null; 
				}
				rowMap.put("hour", rs.getInt(1));
	            rowMap.put("data", rs.getInt(2));
	            returnList.add(rowMap);   
	            return null;
	            }

	        });

		return returnList;
		
		
	}
	
   

	

	

	
	
	

}
