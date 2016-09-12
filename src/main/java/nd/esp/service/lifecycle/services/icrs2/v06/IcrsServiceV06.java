package nd.esp.service.lifecycle.services.icrs2.v06;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

import com.mysql.fabric.xmlrpc.base.Data;

import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.models.v06.AssetModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface IcrsServiceV06 {

	/**
	 * 查询本校的教师资源产出数据
	 * @param String schoolId
	 * @return
	 */
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutputBySchoolId (String schoolId,String resType,Date fromDate,Date toDate,String grade,String subject,
            String order,String limit);
	
	
	public List<Map<String, Object>> getResourcePerHourBySchoolId(String schoolId,String resType,Date queryDate );
	
	
}
