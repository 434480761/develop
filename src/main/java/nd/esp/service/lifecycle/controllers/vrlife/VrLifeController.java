package nd.esp.service.lifecycle.controllers.vrlife;

import javax.validation.Valid;

import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.vrlife.VrLifeService;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.enums.ResourceNdCode;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewTags;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
/**
 * VR人生 定制化接口
 * @author xiezy
 * @date 2016年7月18日
 */
@RestController
@RequestMapping("/v0.6/vrlife/{res_type}")
public class VrLifeController {
	@Autowired
	@Qualifier("vrLifeServiceImpl")
    private VrLifeService vrLifeService;
	@Autowired
	@Qualifier("vrLifeServiceImpl4QuestionDb")
    private VrLifeService vrLifeService4QuestionDb;
	@Autowired
	private OfflineService offlineService;
	@Autowired
	private AsynEsResourceService esResourceOperation;
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	
	/**
	 * 资源审核,更新内容包括了资源状态与标签分类
	 * @author xiezy
	 * @date 2016年7月19日
	 * @param resType
	 * @param id
	 * @param statusReviewViewModel4In
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(value = "/status/review/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public StatusReviewViewModel4Out statusReview(@PathVariable(value="res_type") String resType,@PathVariable(value="id") String id,
			@Valid @RequestBody StatusReviewViewModel4In statusReviewViewModel4In,BindingResult bindingResult){
		//校验入参
        ValidResultHelper.valid(bindingResult, "LC/VRLIFE_STATUS_REVIEW_PARAM_VALID_FAIL", "VrLifeController", "statusReview");
        
        //业务校验
        if(!LifecycleStatus.isLegalStatus(statusReviewViewModel4In.getStatus())){
        	throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,"LC/VRLIFE_STATUS_IS_ILLEGAL","资源状态非NDR合法状态值");
        }
        if(CollectionUtils.isNotEmpty(statusReviewViewModel4In.getTags())){
        	for(StatusReviewTags tags : statusReviewViewModel4In.getTags()){
        		if(!tags.getOperation().equals("add") && !tags.getOperation().equals("delete")){
        			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
        					"LC/VRLIFE_TAGS_OPERATION_IS_ILLEGAL","tags中的operation仅支持add,delete");
        		}
        	}
        }
        //补全参数
        statusReviewViewModel4In.setIdentifier(id);
        statusReviewViewModel4In.setResType(resType);
        
        StatusReviewViewModel4Out statusReviewViewModel4Out = new StatusReviewViewModel4Out();
		//校验PT
		if(StringUtils.isNotEmpty(statusReviewViewModel4In.getPublishType())){
			commonServiceHelper.isPublishType(statusReviewViewModel4In.getPublishType());
		}
        if(CommonServiceHelper.isQuestionDb(resType)){
        	statusReviewViewModel4Out = vrLifeService4QuestionDb.statusReview(statusReviewViewModel4In);
        }else{
        	statusReviewViewModel4Out = vrLifeService.statusReview(statusReviewViewModel4In);
        }
		
		offlineService.writeToCsAsync(resType, id);
		if (ResourceTypeSupport.isValidEsResourceType(ResourceNdCode.assets.toString())) {
			esResourceOperation.asynAdd(new Resource(resType, id));
		}
		
		return statusReviewViewModel4Out;
	}
}
