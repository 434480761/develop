package nd.esp.service.lifecycle.services.icrs2.v06.impls;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.SimpleDateFormat;
import com.mysql.fabric.xmlrpc.base.Data;

import nd.esp.service.lifecycle.daos.icrs2.v06.Icrs2Dao;
import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.Icrs;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.vos.ListViewModel;


@Service("icrsServiceV06")
public class IcrsServiceV06imple implements IcrsServiceV06{

	
	@Autowired
	private Icrs2Dao Icrs2Dao;
	
	@Autowired
	private CategoryDataRepository categoryDataRepository;
	
	@Override
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutputBySchoolId(String schoolId,String resType,Date fromDate,Date toDate,String grade,String subject,
            String order,String limit) {
		// TODO Auto-generated method stub
		
		String gradeName = null;
		String subjectName = null;
		List<Map<String, Object>> List = Icrs2Dao.queryBySchoolId(schoolId,resType,fromDate,toDate,grade,subject,
	                                               order,limit); 
		List<TeacherOutputResource> items = new ArrayList<TeacherOutputResource>();	   
		//遍历
		TeacherOutputResource tor=null;
		for (Map<String, Object> m : List) {
			tor = new TeacherOutputResource();
			tor.setTeacherId((String)m.get("teacherId"));
			tor.setTeacherName((String)m.get("teacherName"));
			tor.setGradeCode((String)m.get("gradeCode"));
			tor.setSubjectCode((String)m.get("subjectCode"));	
			
			 if((String)m.get("gradeCode")!=null){
				gradeName = accordingCodeFindCodeName((String)m.get("gradeCode"));//通过subject_code获得这个值
				tor.setGrade(gradeName);
			   }
			  if((String)m.get("subjectCode")!=null){
				subjectName = accordingCodeFindCodeName((String)m.get("subjectCode"));
				tor.setSubject(subjectName);
			   }
			  
									 
			tor.setData( (Integer) m.get("data"));  
			items.add(tor);
		}
		
		ListViewModel<TeacherOutputResource> returnListViewModel = new ListViewModel<TeacherOutputResource>();
		if (items.size()>0) {
			returnListViewModel.setItems(items);
			returnListViewModel.setLimit("(0,"+limit+")");//在这里先设他为空值，controller的时候再给他赋值就好了
			returnListViewModel.setTotal((long) items.size());	
		}else {
			return null;
		}
		
		return returnListViewModel;
	}

	@Override
	public List<Map<String, Object>> getResourcePerHourBySchoolId(
			String schoolId, String resType, Date queryDate) {
		List<Map<String, Object>> list = Icrs2Dao.getResourcePerHour(schoolId, resType, queryDate);
		return list;
	}

	public  String accordingCodeFindCodeName(String sourceString) {
		 String name = null;
		 CategoryData cData = new CategoryData();  //根据grade来查找中文名称
		 cData.setNdCode(sourceString);	
		 try {
			cData = categoryDataRepository.getByExample(cData);
			name=cData.getTitle();
		 } catch (Exception e) {
			
			 throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL", "category_datas访问出错，通过code找不到相应的subject和grade");		
		 }
		 return name;
		 
	}

	
		
		
}
