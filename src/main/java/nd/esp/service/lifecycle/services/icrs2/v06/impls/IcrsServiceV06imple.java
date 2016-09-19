package nd.esp.service.lifecycle.services.icrs2.v06.impls;

import java.util.List;

import nd.esp.service.lifecycle.daos.icrs2.v06.Icrs2Dao;
import nd.esp.service.lifecycle.models.icrs2.v06.HourDataModel;
import nd.esp.service.lifecycle.models.icrs2.v06.TeacherOutputResource;
import nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("icrsServiceV06")
public class IcrsServiceV06imple implements IcrsServiceV06{
	
	@Autowired
	private Icrs2Dao icrs2Dao;
	
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
		
		List<TeacherOutputResource> items = 
				icrs2Dao.querySchoolTeacherResource(
						schoolId,resType,fromDate,toDate,grade,subject,order,limit);
		long total = icrs2Dao.countSchoolTeacherResource(schoolId, resType, fromDate, toDate, grade, subject);
		
		ListViewModel<TeacherOutputResource> returnListViewModel = new ListViewModel<TeacherOutputResource>();
		returnListViewModel.setItems(items);
		returnListViewModel.setLimit(limit);
		returnListViewModel.setTotal(total);	
		
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
	public List<HourDataModel> queryResourcePerHourOutput(
			String schoolId, String resType, String queryDate) {
		return icrs2Dao.queryResourcePerHour(schoolId, resType, queryDate);
	}
}
