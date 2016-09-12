package nd.esp.service.lifecycle.services.icrs2.v06.impls;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.SimpleDateFormat;
import com.mysql.fabric.xmlrpc.base.Data;

import nd.esp.service.lifecycle.daos.icrs2.v06.Icrs2Dao;
import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.repository.model.Icrs;
import nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06;
import nd.esp.service.lifecycle.vos.ListViewModel;


@Service("icrsServiceV06")
public class IcrsServiceV06imple implements IcrsServiceV06{

	
	@Autowired
	private Icrs2Dao Icrs2Dao;
	
	@Override
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutputBySchoolId(String schoolId,String resType,Date fromDate,Date toDate,String grade,String subject,
            String order,String limit) {
		// TODO Auto-generated method stub
		
		List<Icrs> List = Icrs2Dao.queryBySchoolId(schoolId,resType,fromDate,toDate,grade,subject,
	                                               order,limit); //从表中查询得到的值
		List<TeacherOutputResource> items = new ArrayList<TeacherOutputResource>();	   //结果的items	
		for (Icrs icrs : List) {
			TeacherOutputResource tor=null;
			tor.setTeacherId(icrs.getTeacherId());
			tor.setTeacherName(icrs.getTeacherName());
			tor.setGradeCode(icrs.getGradeCode());
			tor.setSubjectCode(icrs.getSubjectCode());
			//tor.setGrade(grade);//怎么通过code来得到grade_code
			//tor.setSubject(subject);//怎么通过SubjectCode来得到subject
			tor.setData(icrs.getCreateDate());  
			items.add(tor);
		}
		
		ListViewModel<TeacherOutputResource> returnListViewModel = new ListViewModel<TeacherOutputResource>();
		returnListViewModel.setItems(items);
		returnListViewModel.setLimit(limit);//在这里先设他为空值，controller的时候再给他赋值就好了
		returnListViewModel.setTotal((long) items.size());
		
		//遍历list，给returnListViewModel赋值
		return returnListViewModel;
	}

	@Override
	public List<Map<String, Object>> getResourcePerHourBySchoolId(
			String schoolId, String resType, Date queryDate) {
		List<Map<String, Object>> list = Icrs2Dao.getResourcePerHour(schoolId, resType, queryDate);
		return list;
	}

	

	
		
		
}
