package nd.esp.service.lifecycle.controllers.teachingmaterial.v06;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.models.teachingmaterial.v06.TeachingMaterialModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.teachingmaterial.v06.TeachingMaterialServiceV06;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4Format2Category;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineJsonToCS;
import nd.esp.service.lifecycle.support.annotation.MarkAspect4OfflineToES;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.busi.transcode.TransCodeManager;
import nd.esp.service.lifecycle.support.enums.OperationType;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.teachingmaterial.v06.TeachingMaterialViewModel;
import nd.esp.service.lifecycle.vos.valid.Valid4UpdateGroup;
import nd.esp.service.lifecycle.vos.valid.ValidGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@Autowired
	private TransCodeUtil transCodeUtil;

	@Autowired
	private OfflineService offlineService;
	
	@Autowired
	private AsynEsResourceService esResourceOperation;
	
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	/**
	 * 创建教材对象（带id）
	 * @param rm		教材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value="/{id}",method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public TeachingMaterialViewModel create4Id(@PathVariable String resType,@PathVariable String id,@Validated(ValidGroup.class) @RequestBody TeachingMaterialViewModel tmvm,BindingResult validResult){
		
	    if(!(ResourceNdCode.teachingmaterials.toString().equals(resType)||ResourceNdCode.guidancebooks.toString().equals(resType)||ResourceNdCode.metacurriculums.toString().equals(resType))){
	        throw new LifeCircleException("类型不对");
	    }
	    
		tmvm.setIdentifier(id);
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/CREATE_TEACHINGMATERIAL_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "create");
		
		//业务校验
		CommonHelper.inputParamValid(tmvm,"10111",OperationType.CREATE);
		
		TeachingMaterialModel tmm = CommonHelper.convertViewModelIn(tmvm, TeachingMaterialModel.class,ResourceNdCode.teachingmaterials);
		
		boolean bTranscode = TransCodeManager.canTransCode(tmvm, IndexSourceType.TeachingMaterialType.getName());
		if(bTranscode) {
		    tmm.getLifeCycle().setStatus(TransCodeUtil.getTransIngStatus(true));
		}
		
		//创建教材
		tmm = teachingMaterialService.createTeachingMaterial(resType,tmm);
		
		//model转换
		tmvm = CommonHelper.convertViewModelOut(tmm,TeachingMaterialViewModel.class);
		
		if (bTranscode) {
            transCodeUtil.triggerTransCode(tmm, IndexSourceType.TeachingMaterialType.getName());
        }
		
		return tmvm;
	}

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
		
	    if(!(ResourceNdCode.teachingmaterials.toString().equals(resType)||ResourceNdCode.guidancebooks.toString().equals(resType)||ResourceNdCode.metacurriculums.toString().equals(resType))){
	        throw new LifeCircleException("类型不对");
	    }
	    
	    String id = UUID.randomUUID().toString();
		tmvm.setIdentifier(id);
		//入参合法性校验
		ValidResultHelper.valid(validResult, "LC/CREATE_TEACHINGMATERIAL_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "create");
		
		//业务校验
		CommonHelper.inputParamValid(tmvm,"10110",OperationType.CREATE);
		
		return operate(resType,tmvm,OperationType.CREATE);
	}
	
	/**
	 * 修改教材对象
	 * @param rm		教材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@MarkAspect4OfflineJsonToCS
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public TeachingMaterialViewModel update(@PathVariable String resType,@Validated(Valid4UpdateGroup.class) @RequestBody TeachingMaterialViewModel tmvm,BindingResult validResult,@PathVariable String id){
		
	    
	    if(!(ResourceNdCode.teachingmaterials.toString().equals(resType)||ResourceNdCode.guidancebooks.toString().equals(resType)||ResourceNdCode.metacurriculums.toString().equals(resType))){
            throw new LifeCircleException("类型不对");
        }
	    
	    //入参合法性校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_TEACHINGMATERIAL_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "update");
		tmvm.setIdentifier(id);
		
		//业务校验
		CommonHelper.inputParamValid(tmvm,"10111",OperationType.UPDATE);

		return operate(resType,tmvm,OperationType.UPDATE);
	}
	
	/**
	 * 操作教材对象
	 * @param resType
	 * @param tmvm
	 * @param ot
	 * @return
	 */
	private TeachingMaterialViewModel operate(String resType,TeachingMaterialViewModel tmvm,OperationType ot){
		//model入参转换，部分数据初始化
		TeachingMaterialModel tmm = CommonHelper.convertViewModelIn(tmvm, TeachingMaterialModel.class,ResourceNdCode.teachingmaterials);
		
		if(ot == OperationType.CREATE){
			//创建教材
			tmm = teachingMaterialService.createTeachingMaterial(resType,tmm);
		}else if(ot == OperationType.UPDATE){
			//修改教材
			tmm = teachingMaterialService.updateTeachingMaterial(resType,tmm);
		}
		
		//model转换
		tmvm = CommonHelper.convertViewModelOut(tmm,TeachingMaterialViewModel.class);
		
		return tmvm;
	}

	/**
	 * 修改教材对象
	 * @param rm		教材对象
	 * @return
	 */
	@MarkAspect4Format2Category
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public TeachingMaterialViewModel patch(@PathVariable String resType,@Validated(Valid4UpdateGroup.class) @RequestBody TeachingMaterialViewModel tmvm,BindingResult validResult,
										   @PathVariable String id, @RequestParam(value = "notice_file", required = false,defaultValue = "true") boolean notice){


		if(!(ResourceNdCode.teachingmaterials.toString().equals(resType)||ResourceNdCode.guidancebooks.toString().equals(resType)||ResourceNdCode.metacurriculums.toString().equals(resType))){
			throw new LifeCircleException("类型不对");
		}

		//入参合法性校验
//		ValidResultHelper.valid(validResult, "LC/UPDATE_TEACHINGMATERIAL_PARAM_VALID_FAIL", "TeachingMaterialControllerV06", "update");
		tmvm.setIdentifier(id);

		//业务校验
//		CommonHelper.inputParamValid(tmvm,"10111",OperationType.UPDATE);

		//model入参转换，部分数据初始化
		TeachingMaterialModel tmm = CommonHelper.convertViewModelIn(tmvm, TeachingMaterialModel.class,ResourceNdCode.teachingmaterials, true);

		//修改教材
		tmm = teachingMaterialService.patchTeachingMaterial(resType,tmm);

		//model转换
		tmvm = CommonHelper.convertViewModelOut(tmm,TeachingMaterialViewModel.class);

		if(notice) {
			offlineService.writeToCsAsync(resType, id);
		}
		// offline metadata(coverage) to elasticsearch
		if (ResourceTypeSupport.isValidEsResourceType(resType)) {
			esResourceOperation.asynAdd(
					new Resource(resType, id));
		}
		return tmvm;
	}
	
	
	/**
	 * 根据教材id查找章节资源
	 * @param tmId		教材id
	 * @param resTypes	查找的资源类型，多个用逗号分隔
	 * @return
	 */
	@RequestMapping(value="/{tmId}/resources",method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<Map<String,Object>> queryResourcesByTmId(@PathVariable String tmId,@RequestParam String resTypes,@RequestParam(required=false) String include,@RequestParam String coverage){
		List<String> resTypeList = null;
		//1、判断参数的合法性
		if(StringUtils.isNotEmpty(resTypes)){
			String[] array = resTypes.split(",");
			resTypeList = Arrays.asList(array);
		}
		if(CollectionUtils.isNotEmpty(resTypeList)){
			Map<String, Object> map = commonServiceHelper.getRepositoryAndModelMap();
			for (String rt : resTypeList) {
				if(!map.containsKey(rt)){
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/RES_TYPE_ERROR","资源类型有误");
				}
			}
		}else{
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/RES_TYPE_EMPTY","资源类型不能为空");
		}
		
		if(StringUtils.isNotEmpty(coverage)){
			String[] cs = coverage.split("/");
			if(cs.length != 2 && cs.length != 3){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL","coverage格式不对");
			}else if(StringUtils.isEmpty(cs[0]) || StringUtils.isEmpty(cs[1])){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL","coverage格式不对");
			}
		}else{
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/CHECK_PARAM_VALID_FAIL","coverage不能为空");
		}
		
		//2、调用service层接口
		List<String> includes;
		if(StringUtils.isNotEmpty(include)){
			String[] s = include.split(",");
			includes = Arrays.asList(s);
		}else{
			includes = new ArrayList<String>();
		}
		return teachingMaterialService.queryResourcesByTmId(tmId,resTypeList,includes,coverage);
	}
}
