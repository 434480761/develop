package nd.esp.service.lifecycle.services.icrs2.v06.impls;

import java.util.Date;
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
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;


@Service("icrsServiceV06")
public class IcrsServiceV06imple implements IcrsServiceV06{

	
	@Autowired
	private Icrs2Dao Icrs2Dao;
	
	@Autowired
	private CategoryDataRepository categoryDataRepository;
	
	
	/**
	 *  查询本校教师的资源产出数据，并将数据放入TeacherOutputResourcemodel输出
	 * @methodName IcrsServiceV06imple.java
	 * @author xm
	 * @date 2016年9月15日 上午10:12:23
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param grade
	 * @param subject
	 * @param order
	 * @param limit
	 * @return
	 * @see nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06#queryTeacherResourceOutput(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutput(String schoolId,String resType,String fromDate,String toDate,String grade,String subject,
            String order,String limit) {
		String gradeName = null;
		String subjectName = null;
		List<Map<String, Object>> list = Icrs2Dao.querySchoolTeacherResource(schoolId,resType,fromDate,toDate,grade,subject,
	                                               order,limit); 
		List<TeacherOutputResource> items = new ArrayList<TeacherOutputResource>();	   
		//遍历
		TeacherOutputResource tor=null;
		for (Map<String, Object> m : list) {
			tor = new TeacherOutputResource();
			tor.setTeacherId((String)m.get("teacherId"));
			tor.setTeacherName((String)m.get("teacherName"));
			tor.setGradeCode((String)m.get("gradeCode"));
			tor.setSubjectCode((String)m.get("subjectCode"));	
			
			 if(StringUtils.hasText((String)m.get("gradeCode"))){
				gradeName = accordingCodeFindCodeName((String)m.get("gradeCode"));//通过subject_code获得这个值
				tor.setGrade(gradeName);
			   }
			  if(StringUtils.hasText((String)m.get("subjectCode"))){
				subjectName = accordingCodeFindCodeName((String)m.get("subjectCode"));
				tor.setSubject(subjectName);
			   }
			  									 
			tor.setData( (Integer) m.get("data"));  
			items.add(tor);
		}
		
		ListViewModel<TeacherOutputResource> returnListViewModel = new ListViewModel<TeacherOutputResource>();
		returnListViewModel.setItems(items);
		returnListViewModel.setLimit("(0,"+limit+")");//在这里先设他为空值，controller的时候再给他赋值就好了
		returnListViewModel.setTotal((long) items.size());		
		return returnListViewModel;
	}

	
	/**
	 * 查询本校资源一天内各时段的产出数量
	 * @methodName IcrsServiceV06imple.java
	 * @author xm
	 * @date 2016年9月15日 上午10:12:46
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return
	 * @see nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06#queryResourcePerHourOutput(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<Map<String, Object>> queryResourcePerHourOutput(
			String schoolId, String resType, String queryDate) {
		List<Map<String, Object>> list = Icrs2Dao.queryResourcePerHour(schoolId, resType, queryDate);
		return list;
	}

	
	/**
	 * 根据code查找title，例如subject_code=$SB02000,查找subject为音乐
	 * @author xm
	 * @version 
	 * @date 2016年9月15日 下午6:01:17
	 * @method accordingCodeFindCodeName
	 * @see 
	 * @param sourceString
	 * @return
	 * String
	 * @throws
	 */
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
