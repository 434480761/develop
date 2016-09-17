package nd.esp.service.lifecycle.services.icrs2.v06;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.formula.functions.T;

import com.mysql.fabric.xmlrpc.base.Data;

import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.models.v06.AssetModel;
import nd.esp.service.lifecycle.vos.ListViewModel;

public interface IcrsServiceV06 {

	/**
	 *定义查询本校教师的资源产出数据接口
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
	public ListViewModel<TeacherOutputResource> queryTeacherResourceOutput (String schoolId,String resType,String fromDate,String toDate,String grade,String subject,
            String order,String limit);
	
	/**
	 * 定义查询本校资源一天内各时段的产出数量接口
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
	public List<Map<String, Object>> queryResourcePerHourOutput(String schoolId,String resType,String queryDate );
	
	
}
