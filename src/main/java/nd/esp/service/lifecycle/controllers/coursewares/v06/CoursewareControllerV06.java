package nd.esp.service.lifecycle.controllers.coursewares.v06;

import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.courseware.v06.CoursewareModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.coursewares.v06.CoursewareServiceV06;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ModelPropertiesValidUitl;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.transcode.TransCodeManager;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.coursewares.v06.CoursewareViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 课件V0.6API
 * @author xuzy
 *
 */
@RestController
@RequestMapping("/v0.6/coursewares")
public class CoursewareControllerV06 {
	@Autowired
	@Qualifier("coursewareServiceV06")
	private CoursewareServiceV06 coursewareService;
	
	@Autowired
    private TransCodeUtil transCodeUtil;

	@Autowired
	private OfflineService offlineService;
	@Autowired
	private AsynEsResourceService esResourceOperation;
	

	private final static Logger LOG= LoggerFactory.getLogger(CoursewareControllerV06.class);
	
	/**
	 * 创建课件对象
	 * @param viewModel		课件对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public CoursewareViewModel create(@Validated({ValidGroup.class}) @RequestBody CoursewareViewModel viewModel,BindingResult validResult,@PathVariable String id){
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/CREATE_COURSEWARE_PARAM_VALID_FAIL", "CoursewareControllerV06", "create");
		viewModel.setIdentifier(id);
		//业务校验
		CommonHelper.inputParamValid(viewModel,"10111",OperationType.CREATE);
		
		//课件需要转码tech_info单独作校验
		Map<String,? extends ResTechInfoViewModel> techInfoMap = viewModel.getTechInfo();
		if(techInfoMap == null || !(techInfoMap.containsKey("href") || techInfoMap.containsKey("source"))){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoHrefOrSourceFail);
		}
		
		//techInfo属性校验
		try {
           if(null!=viewModel.getLifeCycle()&&LifecycleStatus.isNeedTranscode(viewModel.getLifeCycle().getStatus())) {  
        	   ModelPropertiesValidUitl.verificationHref(viewModel);
           }
        } catch (Exception e) {

           LOG.warn("teachinfo属性校验失败",e);

           throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_HREF_FAIL",e.getMessage());
           
        }
		
		//model入参转换,部分数据初始化
		CoursewareModel cm = CommonHelper.convertViewModelIn(viewModel, CoursewareModel.class,ResourceNdCode.coursewares);
		
		boolean bTranscode = TransCodeManager.canTransCode(viewModel, IndexSourceType.SourceCourseWareType.getName());
        if(bTranscode) {
            cm.getLifeCycle().setStatus(TransCodeUtil.getTransIngStatus(true));
        }
		
		//创建课件
		cm = coursewareService.createCourseware(IndexSourceType.SourceCourseWareType.getName(),cm);
		
		//model转换
		viewModel = CommonHelper.convertViewModelOut(cm,CoursewareViewModel.class);
		
		if (bTranscode) {
            transCodeUtil.triggerTransCode(cm, IndexSourceType.SourceCourseWareType.getName());
        }
		
		return viewModel;
	}
	
	/**
	 * 修改课件对象
	 * @param viewModel		课件对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public CoursewareViewModel update(@Validated({Valid4UpdateGroup.class}) @RequestBody CoursewareViewModel viewModel,BindingResult validResult,@PathVariable String id){
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_COURSEWARE_PARAM_VALID_FAIL", "CoursewareControllerV06", "update");
		viewModel.setIdentifier(id);
		//业务校验
		CommonHelper.inputParamValid(viewModel,"10111",OperationType.UPDATE);
		
		//课件需要转码tech_info单独作校验
		Map<String,? extends ResTechInfoViewModel> techInfoMap = viewModel.getTechInfo();
		if(techInfoMap == null || !(techInfoMap.containsKey("href") || techInfoMap.containsKey("source"))){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoHrefOrSourceFail);
		}
		
		//model入参转换，部分数据初始化
		CoursewareModel cm = CommonHelper.convertViewModelIn(viewModel, CoursewareModel.class,ResourceNdCode.coursewares);
		
		//修改课件
		cm = coursewareService.updateCourseware(IndexSourceType.SourceCourseWareType.getName(),cm);
		
		//model出参转换
		viewModel = CommonHelper.convertViewModelOut(cm,CoursewareViewModel.class);
		return viewModel;
	}

	/**
	 * 修改课件对象
	 * @param viewModel		课件对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public CoursewareViewModel patch(@Validated({Valid4UpdateGroup.class}) @RequestBody CoursewareViewModel viewModel,BindingResult validResult,@PathVariable String id,
									 @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
		//入参合法性校验
//		ValidResultHelper.valid(validResult, "LC/UPDATE_COURSEWARE_PARAM_VALID_FAIL", "CoursewareControllerV06", "update");
		viewModel.setIdentifier(id);
		//业务校验
//		CommonHelper.inputParamValid(viewModel,"10111",OperationType.UPDATE);

		//课件需要转码tech_info单独作校验
//		Map<String,? extends ResTechInfoViewModel> techInfoMap = viewModel.getTechInfo();
//		if(techInfoMap == null || !(techInfoMap.containsKey("href") || techInfoMap.containsKey("source"))){
//			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,LifeCircleErrorMessageMapper.ChecTechInfoHrefOrSourceFail);
//		}

		//model入参转换，部分数据初始化
		CoursewareModel cm = CommonHelper.convertViewModelIn(viewModel, CoursewareModel.class,ResourceNdCode.coursewares, true);

		//修改课件
		cm = coursewareService.patchCourseware(IndexSourceType.SourceCourseWareType.getName(), cm);

		//model出参转换
		viewModel = CommonHelper.convertViewModelOut(cm,CoursewareViewModel.class);
		if(notice) {
			offlineService.writeToCsAsync(ResourceNdCode.coursewares.toString(), id);
			// offline metadata(coverage) to elasticsearch
			if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.coursewares.toString())) {
				esResourceOperation.asynAdd(
						new Resource(ResourceNdCode.coursewares.toString(), id));
			}
		}

		return viewModel;
	}
}
