package nd.esp.service.lifecycle.controllers.icrs2.v06;

import java.net.URLDecoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import nd.esp.service.lifecycle.educommon.support.QueryType;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.TeacherOutputResource;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.assets.v06.AssetServiceV06;
import nd.esp.service.lifecycle.services.icrs2.v06.impls.IcrsServiceV06imple;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.apache.xmlbeans.impl.jam.internal.javadoc.JavadocClassloadingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;





@RestController
@RequestMapping("/v0.6/icrs/{school_id}")
public class Icrs2Controller {

	
	@Autowired
	@Qualifier("icrsServiceV06")
	private IcrsServiceV06imple icrsService;
	
	@RequestMapping(value = "/statistics/query", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "limit"})
	 public ListViewModel<TeacherOutputResource> requestQueringByDBAndManagement(
	            @PathVariable(value="school_id") String schoolId,
	            @RequestParam(required=false,value="res_type") String resType,
	            @RequestParam(required=false,value="from_date") Date fromDate,
	            @RequestParam(required=false,value="to_date") Date toDate,
	            @RequestParam(required=false,value="grade") String grade,
	            @RequestParam(required=false,value="subject") String subject,
	            @RequestParam(required=false,value="order") String order,
	            @RequestParam String limit)  {
		
		//入参检验
		isValidInput(schoolId,resType,fromDate,toDate,null,order);
		ListViewModel<TeacherOutputResource> returnList= icrsService.queryTeacherResourceOutputBySchoolId(schoolId, resType, fromDate, toDate, grade, subject, order, limit);
		return returnList;
	    }
	
	
	@RequestMapping(value = "/statistics/hour", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	 public List<Map<String, Object>> getRecourcesPerHourBySchoolIdAndOther(
			 	@PathVariable(value="school_id") String schoolId,
	            @RequestParam(required=false,value="res_type") String resType,
	            @RequestParam(required=false,value="query_date") Date queryDate
	            ) throws java.lang.Exception {
		
		//入参检验
		isValidInput(schoolId, resType, null, null, queryDate, null);
		List<Map<String, Object>> returnList= icrsService.getResourcePerHourBySchoolId(schoolId, resType, queryDate);
		return returnList;
	    }
	
	
	
	public void isValidInput(String schoolId,String resType,Date fromDate,Date toDate,Date queryDate,String order){
		      //对schoolid进行验证，不为空，并且符合uuid的8-4-4-4-12 
				if (schoolId==null||!CommonHelper.checkUuidPattern(schoolId)) {
					//抛出异常，最好是throw new LifeCircleException
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL", "icrs查询/statistics/query入参schoolId错误");	
				}
				
				//对res_type做检验,只能为这四个课件 courseware / 多媒体 multimedia / 基础习题 basic_question / 趣味题型 funny_question
				if (resType!=null) {
					if (resType!="courseware"
							||resType!="multimedia"
							||resType!="basic_question"
							||resType!="funny_question") {
						throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL", "icrs查询/statistics/query入参res_type错误");
					}
				}
				
				//对order最检验
				if (order!=null) {	
					if (!order.equalsIgnoreCase("desc")&&!order.equalsIgnoreCase("asc")) {
						throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL", "icrs查询/statistics/query入参order错误");
					}
				}				
				//对日期格式进行判断
				if (fromDate!=null) {
					isValidDate(fromDate);
				}
				if (toDate!=null) {
					isValidDate(toDate);
				}
	}
	
	public void isValidDate(Date date){
		if (date!=null) {
			try {
				String formDateString = date+"";  
			    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");  
			    format.setLenient(false);  
			    java.util.Date formateDate =   format.parse(formDateString);  
			} catch (Exception e) {
				
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL", "icrs查询/statistics/query入参日期错误");
			}
		}
		
	}
	
}
