package nd.esp.service.lifecycle.services.icrs1.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.icrs1.v06.DailyDataModel;
import nd.esp.service.lifecycle.models.icrs1.v06.ResourceTotalModel;
import nd.esp.service.lifecycle.models.icrs1.v06.TextbookModel;

public interface Icrs1Service {


	/**
	 * 查询本校不同类别资源的产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型
	 * @author yuzc
	 * @date 2016年9月9日
	 * @param schoolId
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	List<ResourceTotalModel> getResourceTotal(String schoolId,
			String fromDate, String toDate);

	/**
	 * 查询本校资源的日产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型。
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
	 * 取得某一个教师上传的资源，所对应的教材列表
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
