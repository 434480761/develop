package nd.esp.service.lifecycle.services.icrs.v06.impls;

import java.util.List;

import nd.esp.service.lifecycle.daos.Icrs.v06.IcrsDao;
import nd.esp.service.lifecycle.models.icrs.v06.DailyDataModel;
import nd.esp.service.lifecycle.models.icrs.v06.HourDataModel;
import nd.esp.service.lifecycle.models.icrs.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs.v06.TeacherOutputResource;
import nd.esp.service.lifecycle.models.icrs.v06.TextbookModel;
import nd.esp.service.lifecycle.services.icrs.v06.IcrsService;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("IcrsServiceImpl")
public class IcrsServiceImpl implements IcrsService {

	@Autowired
	private IcrsDao icrsDao;

	@Override
	public List<ResourceTotalModel> getResourceTotal(String schoolId,
			String fromDate, String toDate) {
		return icrsDao.getResourceTotal(schoolId, fromDate, toDate);
	}

	@Override
	public List<DailyDataModel> getResourceStatisticsByDay(String schoolId,
			String resType, String fromDate, String toDate) {

		return icrsDao.getResourceStatisticsByDay(schoolId, resType, fromDate,
				toDate);
	}

	@Override
	public List<TextbookModel> getTeacherResource(String schoolId,
			String teacherId, String resType) {

		return icrsDao.getTeacherResource(schoolId, teacherId, resType);
	}

	/**
	 * 查询本校教师的资源产出数据，并将数据放入TeacherOutputResourcemodel输出
	 * 
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
	 * @see nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06#queryTeacherResourceOutput(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutput(
			String schoolId, String resType, String fromDate, String toDate,
			String grade, String subject, String order, String limit) {

		List<TeacherOutputResource> items = icrsDao.querySchoolTeacherResource(
				schoolId, resType, fromDate, toDate, grade, subject, order,
				limit);
		long total = icrsDao.countSchoolTeacherResource(schoolId, resType,
				fromDate, toDate, grade, subject);

		ListViewModel<TeacherOutputResource> returnListViewModel = new ListViewModel<TeacherOutputResource>();
		returnListViewModel.setItems(items);
		returnListViewModel.setLimit(limit);
		returnListViewModel.setTotal(total);

		return returnListViewModel;
	}

	/**
	 * 查询本校资源一天内各时段的产出数量
	 * 
	 * @methodName IcrsServiceV06imple.java
	 * @author xm
	 * @date 2016年9月15日 上午10:12:46
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return
	 * @see nd.esp.service.lifecycle.services.icrs2.v06.IcrsServiceV06#queryResourcePerHourOutput(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public List<HourDataModel> queryResourcePerHourOutput(String schoolId,
			String resType, String queryDate) {
		return icrsDao.queryResourcePerHour(schoolId, resType, queryDate);
	}
}
