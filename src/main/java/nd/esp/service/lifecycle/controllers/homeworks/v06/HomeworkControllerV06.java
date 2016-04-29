package nd.esp.service.lifecycle.controllers.homeworks.v06;

import nd.esp.service.lifecycle.models.v06.HomeworkModel;
import nd.esp.service.lifecycle.services.homeworks.v06.HomeworkServiceV06;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
}
