package nd.esp.service.lifecycle.controllers.icrs2.v06;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.services.icrs2.v06.impls.IcrsServiceV06imple;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.icrs.IcrsResourceType;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0.6/icrs/{school_id}")
public class Icrs2Controller {

	
	@Autowired
	@Qualifier("icrsServiceV06")
	private IcrsServiceV06imple icrsService;
	
	/**
	 * 查询本校教师的资源产出数据
	 * @author xm
	 * @version 
	 * @date 2016年9月14日 下午6:12:04
	 * @method getTeacherResourceOutput
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
	@RequestMapping(value = "/statistics/query", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "limit"})
	 public ListViewModel<TeacherOutputResource> getTeacherResourceOutput(
	            @PathVariable(value="school_id") String schoolId,
	            @RequestParam(required=false,value="res_type") String resType,
	            @RequestParam(required=false,value="from_date") String fromDate,
	            @RequestParam(required=false,value="to_date") String toDate,
	            @RequestParam(required=false,value="grade") String grade,
	            @RequestParam(required=false,value="subject") String subject,
	            @RequestParam(required=false,value="order") String order,
	            @RequestParam String limit)  {
		
		//入参检验
		isValidInput(schoolId,resType,fromDate,toDate,null,order);
		//转为数据库中reyType类型
		resType = IcrsResourceType.getCorrespondingType(resType);
		ListViewModel<TeacherOutputResource> returnList= icrsService.queryTeacherResourceOutput(schoolId, resType, fromDate, toDate, grade, subject, order, limit);
		return returnList;
	    }
		
	/**
	 * 查询本校资源一天内各时段的产出数量
	 * @author xm
	 * @version 
	 * @date 2016年9月14日 下午6:13:01
	 * @method getRecourcesPerHour
	 * @see 
	 * @param schoolId
	 * @param resType
	 * @param queryDate
	 * @return List<Map<String,Object>>
	 * @throws
	 */
	@RequestMapping(value = "/statistics/hour", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	 public List<Map<String, Object>> getRecourcesPerHour(
			 	@PathVariable(value="school_id") String schoolId,
	            @RequestParam(required=false,value="res_type") String resType,
	            @RequestParam(required=true,value="query_date") String queryDate
	            ) throws java.lang.Exception {
		
		//入参检验
		isValidInput(schoolId, resType, null, null, queryDate, null);
		resType = IcrsResourceType.getCorrespondingType(resType);
		List<Map<String, Object>> returnList= icrsService.queryResourcePerHourOutput(schoolId, resType, queryDate);
		return returnList;
	    }
	

	/**
	 * 对查询入参检验
	 * @author xm
	 * @version 
	 * @date 2016年9月14日 下午6:14:08
	 * @method isValidInput
	 * @see 
	 * @param schoolId
	 * @param resType
	 * @param fromDate
	 * @param toDate
	 * @param queryDate
	 * @param order
	 * @return void
	 * @throws
	 */
	public void isValidInput(String schoolId,String resType,String fromDate,String toDate,String queryDate,String order){
		      
		if (!StringUtils.hasText(schoolId)) { 
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckIcrsParamValidFail.getCode(), LifeCircleErrorMessageMapper.CheckIcrsParamValidFail.getMessage());			
			}
		if (StringUtils.hasText(resType)) {			
			if (!IcrsResourceType.validType(resType)) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckIcrsParamValidFail);
			}
		}
		if (StringUtils.hasText(order)) {	
			if (!order.equalsIgnoreCase("desc")&&!order.equalsIgnoreCase("asc")) {
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.CheckIcrsParamValidFail.getCode(), LifeCircleErrorMessageMapper.CheckIcrsParamValidFail.getMessage());
			}
		}				
		if (StringUtils.hasText(fromDate)) {
			isValidDate(fromDate);
		}
		if (StringUtils.hasText(toDate)) {
			isValidDate(toDate);
		}
		if (StringUtils.hasText(queryDate)) {
			isValidDate(queryDate);
		}
	}
	
	
	/**
	 * 对Date类型进行校验，看是否满足yyyy-MM-dd类型
	 * @author xm
	 * @version 
	 * @date 2016年9月14日 下午6:47:18
	 * @method isValidDate
	 * @see 
	 * @param date
	 * @return void
	 * @throws
	 */
	public void isValidDate(String date){
		if (StringUtils.hasText(date)) {
			try {
				String formDateString = date;
			    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");  
			    format.setLenient(false);  
			    format.parse(formDateString);  
			} catch (Exception e) {	
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						LifeCircleErrorMessageMapper.CheckIcrsParamValidFail.getCode(), 
						e.getMessage());
			}
		}
	}
}

