package nd.esp.service.lifecycle.services.icrs.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.icrs.v06.DailyDataModel;
import nd.esp.service.lifecycle.models.icrs.v06.HourDataModel;
import nd.esp.service.lifecycle.models.icrs.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs.v06.TeacherOutputResource;
import nd.esp.service.lifecycle.models.icrs.v06.TextbookModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface IcrsService {

	/**
	 * 查询本校不同类别资源的产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型
	 * 
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List<ResourceTotalModel> getResourceTotal(String schoolId, String fromDate,
			String toDate);

	/**
	 * 查询本校资源的日产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型。
	 * 
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List<DailyDataModel> getResourceStatisticsByDay(String schoolId,
			String resType, String fromDate, String toDate);

	/**
	 * 定义查询本校资源一天内各时段的产出数量接口
	 * 
	 * @author xm
	 * @version
	 * @date 2016年9月13日 下午5:56:14
	 * @method queryResourcePerHourOutput
	 * @see
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return List<Map<String,Object>>
	 * @throws
	 */
	public List<HourDataModel> queryResourcePerHourOutput(String schoolId,
			String resType, String queryDate);

	/**
	 * 定义查询本校教师的资源产出数据接口
	 * 
	 * @author xm
	 * @version
	 * @date 2016年9月13日 下午5:56:22
	 * @method queryTeacherResourceOutput
	 * @see
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param grade
	 * @param subject
	 * @param order
	 * @param limit
	 * @return ListViewModel<TeacherOutputResource>
	 * @throws
	 */
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutput(
			String schoolId, String resType, String fromDate, String toDate,
			String grade, String subject, String order, String limit);

	/**
	 * 取得某一个教师上传的资源，所对应的教材列表
	 * 
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param teacherId
	 * @param resType
	 * @return
	 */
	List<TextbookModel> getTeacherResource(String schoolId, String teacherId,
			String resType);
}
