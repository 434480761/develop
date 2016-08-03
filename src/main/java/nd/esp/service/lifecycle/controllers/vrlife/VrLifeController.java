package nd.esp.service.lifecycle.controllers.vrlife;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.entity.elasticsearch.Resource;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.services.elasticsearch.AsynEsResourceService;
import nd.esp.service.lifecycle.services.offlinemetadata.OfflineService;
import nd.esp.service.lifecycle.services.vrlife.VrLifeService;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.support.busi.elasticsearch.ResourceTypeSupport;
import nd.esp.service.lifecycle.support.enums.LifecycleStatus;
import nd.esp.service.lifecycle.support.vrlife.VrLifeType;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * VR人生 定制化接口
 * @author xiezy
 * @date 2016年7月18日
 */
@RestController
@RequestMapping("/v0.6/vrlife")
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
	@RequestMapping(value = "/{res_type}/status/review/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public StatusReviewViewModel4Out statusReview(@PathVariable(value="res_type") String resType, @PathVariable(value="id") String id,
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
     	//校验PT
        if(StringUtils.isNotEmpty(statusReviewViewModel4In.getPublishType())){
  			commonServiceHelper.isPublishType(statusReviewViewModel4In.getPublishType());
  		}
        //补全参数
        statusReviewViewModel4In.setIdentifier(id);
        statusReviewViewModel4In.setResType(resType);
        
        StatusReviewViewModel4Out statusReviewViewModel4Out = new StatusReviewViewModel4Out();
        if(CommonServiceHelper.isQuestionDb(resType)){
        	statusReviewViewModel4Out = vrLifeService4QuestionDb.statusReview(statusReviewViewModel4In);
        }else{
        	statusReviewViewModel4Out = vrLifeService.statusReview(statusReviewViewModel4In);
        }
		
		offlineService.writeToCsAsync(resType, id);
		if (ResourceTypeSupport.isValidEsResourceType(resType)) {
			esResourceOperation.asynAdd(new Resource(resType, id));
		}
		
		return statusReviewViewModel4Out;
	}
	
	/**
	 * VR资源推荐功能， 此功能原先为中间库，为了保证原中间库停止运营后，VR角色编辑器能正常使用，特开发此接口。
	 * 
	 * @author xiezy
	 * @date 2016年8月2日
	 * @param skeletonId
	 * @param type
	 * @param include
	 * @return
	 */
	@RequestMapping(value = "/role/recommend", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ListViewModel<ResourceViewModel> recommendResourceList(
			@RequestParam(value="type",defaultValue="skeleton") String type,
			@RequestParam(required=false,value="skeleton_uuid") String skeletonId,
			@RequestParam(required=false,value="include",defaultValue="TI") String include){
		//校验
		if(!VrLifeType.validType(type)){
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					"LC/VRLIFE_TYPE_IS_ILLEGAL","type仅支持:skeleton(骨骼)、roleconfig(角色配置)、action(动作)");
		}
		if(!type.equals(VrLifeType.SKELETON.getName())){
			if(StringUtils.isEmpty(skeletonId)){
				throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
						"LC/VRLIFE_SKELETON_UUID_IS_NOT_EMPTY","当type传的值为roleconfig(角色配置)、action(动作)时,skeleton_uuid不能为空");
			}
		}
		List<String> includeList = IncludesConstant.getValidIncludes(include);
		
		//获取推荐的资源
		ListViewModel<ResourceModel> resourceModelList = vrLifeService.recommendResourceList(skeletonId, type, includeList);
		        
		return changeToViewModel(resourceModelList, includeList);
	}
	
	/**
	 * VR角色模块系统中，一个角色对象会存在多套模型(头型、发型、上身、下身、足)，
	 * 业务场景根据蒙皮(也可能是角色对象)ID来随机获取对应的一套完整模型(包括头型、发型、上身、下身、足)。 
	 * 此功能原先为中间库，为了保证原中间库停止运营后，VR角色编辑器能正常使用，特开发此接口。
	 * 
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param skeletonId
	 * @param include
	 * @return
	 */
	@RequestMapping(value = "/role/{skeleton_uuid}/dynamicComposition", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ListViewModel<ResourceViewModel> dynamicComposition(
			@PathVariable(value="skeleton_uuid") String skeletonId,
			@RequestParam(required=false,value="include",defaultValue="TI") String include){
		
		List<String> includeList = IncludesConstant.getValidIncludes(include);
		
		//获取组合的资源
		ListViewModel<ResourceModel> resourceModelList = vrLifeService.dynamicComposition(skeletonId, includeList);
		
		return changeToViewModel(resourceModelList, includeList);
	}
	
	/**
	 * ListViewModel<ResourceModel> 转换为  ListViewModel<ResourceViewModel>
	 * @author xiezy
	 * @date 2016年8月3日
	 * @param resourceModelList
	 * @param includeList
	 * @return
	 */
	private ListViewModel<ResourceViewModel> changeToViewModel(
			ListViewModel<ResourceModel> resourceModelList, List<String> includeList){
		
		//ListViewModel<ResourceModel> 转换为  ListViewModel<ResourceViewModel>
        ListViewModel<ResourceViewModel> result = new ListViewModel<ResourceViewModel>();
        result.setTotal(resourceModelList.getTotal());
        result.setLimit(resourceModelList.getLimit());
        //items处理
        List<ResourceViewModel> items = new ArrayList<ResourceViewModel>();
        for(ResourceModel resourceModel : resourceModelList.getItems()){
            ResourceViewModel resourceViewModel = CommonHelper.changeToView(resourceModel, IndexSourceType.AssetType.getName(), includeList, commonServiceHelper);
            items.add(resourceViewModel);
        }
        result.setItems(items);
        
		return result;
	}
}