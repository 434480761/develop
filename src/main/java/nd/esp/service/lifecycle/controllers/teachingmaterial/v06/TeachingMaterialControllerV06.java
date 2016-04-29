package nd.esp.service.lifecycle.controllers.teachingmaterial.v06;

import java.util.UUID;

import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.TeachingMaterialServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.vos.teachingmaterial.v06.TeachingMaterialViewModel;
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
 * 教材V0.6API
 * @author xuzy
 *
 */
@RestController
@RequestMapping("/v0.6/{resType}")
public class TeachingMaterialControllerV06 {
	@Autowired
	@Qualifier("teachingMaterialServiceV06")
	private TeachingMaterialServiceV06 teachingMaterialService;

	/**
	 * 创建教材对象
	 * @param rm		教材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineToES
	@RequestMapping(value="",method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public TeachingMaterialViewModel create(
	                                         @PathVariable String resType,@Validated(ValidGroup.class) @RequestBody TeachingMaterialViewModel tmvm,BindingResult validResult){
		
	    if(!(ResourceNdCode.teachingmaterials.toString().equals(resType)||ResourceNdCode.guidancebooks.toString().equals(resType))){
	        throw new LifeCircleException("类型不对");
	    }
	    
	    String id = UUID.randomUUID().toString();
		tmvm.setIdentifier(id);
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/CREATE_TEACHINGMATERIAL_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "create");
		
		//业务校验
		CommonHelper.inputParamValid(tmvm,"10110",OperationType.CREATE);
		
		//model入参转换,部分数据初始化
		TeachingMaterialModel tmm = CommonHelper.convertViewModelIn(tmvm, TeachingMaterialModel.class,ResourceNdCode.teachingmaterials);
		
		//创建教材
		tmm = teachingMaterialService.createTeachingMaterial(resType,tmm);

		//model出参转换
		tmvm = CommonHelper.convertViewModelOut(tmm,TeachingMaterialViewModel.class);
		
		//教材没有techInfo属性
		tmvm.setTechInfo(null);
		return tmvm;
	}
	
	/**
	 * 修改教材对象
	 * @param rm		教材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineToES
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public TeachingMaterialViewModel update(@PathVariable String resType,@Validated(Valid4UpdateGroup.class) @RequestBody TeachingMaterialViewModel tmvm,BindingResult validResult,@PathVariable String id){
		
	    
	    if(!(ResourceNdCode.teachingmaterials.toString().equals(resType)||ResourceNdCode.guidancebooks.toString().equals(resType))){
            throw new LifeCircleException("类型不对");
        }
	    
	    //入参合法性校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_TEACHINGMATERIAL_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "update");
		tmvm.setIdentifier(id);
		
		//业务校验
		CommonHelper.inputParamValid(tmvm,"10111",OperationType.UPDATE);
		
		//model入参转换，部分数据初始化
		TeachingMaterialModel tmm = CommonHelper.convertViewModelIn(tmvm, TeachingMaterialModel.class,ResourceNdCode.teachingmaterials);
		
		//修改教材
		tmm = teachingMaterialService.updateTeachingMaterial(resType,tmm);
		
		//model转换
		tmvm = CommonHelper.convertViewModelOut(tmm,TeachingMaterialViewModel.class);
		
		//教材没有techInfo属性
		tmvm.setTechInfo(null);
		return tmvm;
	}
}
