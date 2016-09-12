package nd.esp.service.lifecycle.daos.icrs2.v06;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.repository.model.Icrs;

import org.apache.poi.ss.formula.functions.T;

import com.mysql.fabric.xmlrpc.base.Data;



public interface Icrs2Dao {

	/**
	 * 根据学校id查询本校教师的资源产出数据
	 * @param schoolId
	 * @return ListViewModel<TeacherOutputResource>
	 */
	public List<Icrs> queryBySchoolId(String schoolId,String resType,Date fromDate,Date toDate,String grade,String subject,
            String order,String limit);
	
	public List<Map<String, Object>> getResourcePerHour(String schoolId,String resType,Date queryDate);

    
}
