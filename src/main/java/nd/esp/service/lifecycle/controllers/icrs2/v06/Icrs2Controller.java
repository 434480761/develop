package nd.esp.service.lifecycle.controllers.icrs2.v06;

import java.net.URLDecoder;
import java.sql.Date;
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
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.sun.tools.internal.ws.wsdl.document.jaxws.Exception;




@RestController
@RequestMapping("/v0.6/icrs/{school_id}")
public class Icrs2Controller {

	
	@Autowired
	@Qualifier("icrsServiceV06")
	private IcrsServiceV06imple icrsService;
	
	@RequestMapping(value = "/statistics/query", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE },params = { "limit"})
	 public ListViewModel<TeacherOutputResource> requestQueringByDBAndManagement(
	            @RequestParam(required=true,value="school_id") String schoolId,
	            @RequestParam(required=false,value="res_type") String resType,
	            @RequestParam(required=false,value="from_date") Date fromDate,
	            @RequestParam(required=false,value="to_date") Date toDate,
	            @RequestParam(required=false,value="grade") String grade,
	            @RequestParam(required=false,value="subject") String subject,
	            @RequestParam(required=false,value="order") String order,
	            @RequestParam String limit) throws java.lang.Exception {
		
		//对schoolid进行验证
		if (schoolId!=null&&CommonHelper.checkUuidPattern(schoolId)) {
			//抛出异常，最好是throw new LifeCircleException
			throw new java.lang.Exception();
			
		}
		//对其他的值也要最判断，最好写成一个函数来对所有的入参做验证
		
		ListViewModel<TeacherOutputResource> returnList= icrsService.queryTeacherResourceOutputBySchoolId(schoolId, resType, fromDate, toDate, grade, subject, order, limit);
		
		return returnList;
	    }
	
	@RequestMapping(value = "/statistics/hour", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	 public List<Map<String, Object>> getRecourcesPerHourBySchoolIdAndOther(
	            @RequestParam(required=true,value="school_id") String schoolId,
	            @RequestParam(required=false,value="res_type") String resType,
	            @RequestParam(required=false,value="query_date") Date queryDate
	            ) throws java.lang.Exception {
		
		//对schoolid进行验证
		if (schoolId!=null&&CommonHelper.checkUuidPattern(schoolId)) {
			//抛出异常，最好是throw new LifeCircleException
			throw new java.lang.Exception();
			
		}
		//对其他的值也要最判断，最好写成一个函数来对所有的入参做验证
		List<Map<String, Object>> returnList= icrsService.getResourcePerHourBySchoolId(schoolId, resType, queryDate);
		return returnList;
	    }
	
}
