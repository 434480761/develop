package nd.esp.service.lifecycle.daos.icrs2.v06;

import java.util.List;

import nd.esp.service.lifecycle.models.icrs2.v06.HourDataModel;
import nd.esp.service.lifecycle.models.icrs2.v06.TeacherOutputResource;

public interface Icrs2Dao {

	/**
	 * 定义查询本校教师的资源产出数据，依教师、年级、学科进行数量统计的接口方法
	 * @author xm
	 * @version 
	 * @date 2016年9月14日 下午7:13:43
	 * @method querySchoolTeacherResource
	 * @see 
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param grade
	 * @param subject
	 * @param order
	 * @param limit
	 * @return List<Map<String,Object>>
	 * @throws
	 */
	public List<TeacherOutputResource> querySchoolTeacherResource(String schoolId,String resType,String fromDate,String toDate,String grade,String subject,
            String order,String limit);
	
	/**
	 * 定义查询本校教师的资源产出数据，依教师、年级、学科进行数量统计的接口方法 -- 计算total
	 * @author xiezy
	 * @date 2016年9月19日
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param grade
	 * @param subject
	 * @return
	 */
	public long countSchoolTeacherResource(String schoolId,String resType,String fromDate,String toDate,String grade,String subject);
	
	/**
	 * 定义查询本校资源一天内各时段的产出数量，统计范围为本校全部教师的个人库资源，统计类型包括课件、多媒体、基础习题、趣味题型接口方法。
	 * @author xm
	 * @version 
	 * @date 2016年9月14日 下午7:14:10
	 * @method queryResourcePerHour
	 * @see 
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return
	 * List<Map<String,Object>>
	 * @throws
	 */
	public List<HourDataModel> queryResourcePerHour(String schoolId,String resType,String queryDate);
}
