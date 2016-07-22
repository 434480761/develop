package nd.esp.service.lifecycle.controllers.homeworks.v06;

import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.v06.HomeworkModel;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.homeworks.v06.HomeworkServiceV06;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.homeworks.v06.HomeworkViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 作业V0.6API
 * @author xuzy
 *
 */
@RestController
@RequestMapping("/v0.6/homeworks")
public class HomeworkControllerV06 {
	@Autowired
	@Qualifier("homeworkServiceV06")
	private HomeworkServiceV06 homeworkService;

	@Autowired
	private OfflineService offlineService;
	@Autowired
	private AsynEsResourceService esResourceOperation;
	
	/**
	 * 创建作业对象
	 * @param rm		作业对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public HomeworkViewModel create(@Validated(ValidGroup.class) @RequestBody HomeworkViewModel hvm,BindingResult validResult,@PathVariable String id){
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/CREATE_HOMEWORK_PARAM_VALID_FAIL", "HomeworkControllerV06", "create");
		hvm.setIdentifier(id);
		
		//业务校验
		CommonHelper.inputParamValid(hvm,null,OperationType.CREATE);

		//model入参转换,部分数据初始化
		HomeworkModel hm = CommonHelper.convertViewModelIn(hvm, HomeworkModel.class,ResourceNdCode.homeworks);
		
		//创建作业
		hm = homeworkService.createHomework(hm);
		
		//model转换
		hvm = CommonHelper.convertViewModelOut(hm,HomeworkViewModel.class);
		return hvm;
	}
	
	/**
	 * 修改作业对象    
	 * @param rm		作业对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public HomeworkViewModel update(@Validated(Valid4UpdateGroup.class) @RequestBody HomeworkViewModel hvm,BindingResult validResult,@PathVariable String id){
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_HOMEWORK_PARAM_VALID_FAIL", "HomeworkControllerV06", "update");
		hvm.setIdentifier(id);
		
		//业务校验
		CommonHelper.inputParamValid(hvm,null,OperationType.UPDATE);
		
		//model入参转换,部分数据初始化
		HomeworkModel hm = CommonHelper.convertViewModelIn(hvm, HomeworkModel.class,ResourceNdCode.homeworks);
		
		//修改作业
		hm = homeworkService.updateHomework(hm);
		
		//model转换
		hvm = CommonHelper.convertViewModelOut(hm,HomeworkViewModel.class);
		return hvm;
	}

	/**
	 * 修改作业对象
	 * @param hvm		作业对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public HomeworkViewModel patch(@Validated(Valid4UpdateGroup.class) @RequestBody HomeworkViewModel hvm,BindingResult validResult,@PathVariable String id,
								   @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){
		//入参合法性校验
//		ValidResultHelper.valid(validResult, "LC/UPDATE_HOMEWORK_PARAM_VALID_FAIL", "HomeworkControllerV06", "update");
		hvm.setIdentifier(id);

		//业务校验
//		CommonHelper.inputParamValid(hvm,null,OperationType.UPDATE);

		//model入参转换,部分数据初始化
		HomeworkModel hm = CommonHelper.convertViewModelIn(hvm, HomeworkModel.class,ResourceNdCode.homeworks, true);

		//修改作业
		hm = homeworkService.patchHomework(hm);

		//model转换
		hvm = CommonHelper.convertViewModelOut(hm,HomeworkViewModel.class);

		if(notice) {
			offlineService.writeToCsAsync(ResourceNdCode.homeworks.toString(), id);
		}
		// offline metadata(coverage) to elasticsearch
		if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.homeworks.toString())) {
			esResourceOperation.asynAdd(
					new Resource(ResourceNdCode.homeworks.toString(), id));
		}
		return hvm;
	}
}
