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
	public List<Icrs> queryBySchoolId(String schoolId,String resType,Date fromDate,Date toDate,String grade,String subject,
            String order,String limit) {
		
		StringBuffer sqlStringBuffer = new StringBuffer("select * from icrs_resource  where 1 ");
		
		if (schoolId!=null) {
			sqlStringBuffer.append(" and school_id = "+"'"+schoolId+"'");
		}
		if (resType!=null) {
			sqlStringBuffer.append(" and res_type="+"'"+resType+"'");
		}
		if (fromDate!=null) {
			sqlStringBuffer.append(" and create_date>="+fromDate);
		} 
		if (toDate!=null) {
			sqlStringBuffer.append(" and create_date<="+toDate);
		}
//		if (grade!=null) {	
//			sqlStringBuffer.append(" and grade="+"'"+grade+"'");   //如何根据gradecode来找grade
//		}
		if (subject!=null) {
			
			CategoryData cData = new CategoryData();  //根据subject来查找subject_code
			//cData.setNdCode(subject);
			cData.setTitle(subject);
			try {
				cData = categoryDataRepository.getByExample(cData);
			} catch (EspStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sqlStringBuffer.append(" and subject_code="+"'"+cData.getNdCode()+"'");//如何根据subject来找subjectcode
		}
		if (order!=null) {//这个地方怎么依据数量排序,这里按创建的先后进行排序了
			sqlStringBuffer.append(" order by create_date "+"'"+order+"'");
		}
		if (limit!=null) {
			sqlStringBuffer.append(" limit "+"'"+limit+"'");
		}
		
		
		
		Query query =em.createQuery(sqlStringBuffer.toString());	
		List<Icrs> icrsList = query.getResultList();
		return icrsList;
		
	}

	@Override
	public List<Map<String, Object>> getResourcePerHour(String schoolId,String resType, Date queryDate) {

		StringBuffer sqlStringBuffer = new StringBuffer("select create_hour as hour,count(*) from icrs_resource  where 1 ");
		
		if (schoolId!=null) {
			sqlStringBuffer.append(" and school_id = "+"'"+schoolId+"'");
		}
		if (resType!=null) {
			sqlStringBuffer.append(" and res_type="+"'"+resType+"'");
		}
		if (queryDate!=null) {
			sqlStringBuffer.append(" and create_date="+queryDate);
		} 
		
		sqlStringBuffer.append(" group by create_hour order by create_hour");
		String querySql = sqlStringBuffer.toString();
		final List<Map<String,Object>> knowledgeList = new ArrayList<Map<String,Object>>();
		jdbcTemplate.query(querySql, new RowMapper<String>() {

			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String,Object> rowMap = new HashMap<String,Object>();
				rowMap.put("hour", rs.getString("hour"));
	            rowMap.put("data", rs.getInt(2));
	            knowledgeList.add(rowMap);
	            return null;
	            }

	        });
		
		return knowledgeList;
		
	}

	

	

	
	
	

}
