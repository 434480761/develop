package nd.esp.service.lifecycle.controllers.provider.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import nd.esp.service.lifecycle.models.provider.v06.ResourceProviderModel;
import nd.esp.service.lifecycle.services.provider.v06.ResourceProviderService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.provider.v06.ResourceProviderViewModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资源提供商 Controller层
 * @author xiezy
 * @date 2016年8月15日
 */
@RestController
@RequestMapping("/v0.6/resource/provider")
public class ResourceProviderController {
	@Autowired
	private ResourceProviderService resourceProviderService;
	
	/**
	 * 创建资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param resourceProviderViewModel
	 * @param validResult
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResourceProviderViewModel createResourceProvider(@Valid @RequestBody ResourceProviderViewModel resourceProviderViewModel,
									   BindingResult validResult){
		
		//校验
		ValidResultHelper.valid(validResult, "LC/CREATE_RESOURCE_PROVIDER_PARAM_VALID_FAIL", "ResourceProviderController", "createResourceProvider");
		resourceProviderViewModel.setIdentifier(UUID.randomUUID().toString());
		
		ResourceProviderModel rpm = 
				resourceProviderService.createResourceProvider(
						BeanMapperUtils.beanMapper(resourceProviderViewModel, ResourceProviderModel.class));
		
		return changeToViewModel(rpm);
	}
	
	/**
	 * 修改资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param id
	 * @param resourceProviderViewModel
	 * @param validResult
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResourceProviderViewModel updateResourceProvider(@PathVariable String id,
			@Valid @RequestBody ResourceProviderViewModel resourceProviderViewModel,BindingResult validResult){
		
		//校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_RESOURCE_PROVIDER_PARAM_VALID_FAIL", "ResourceProviderController", "updateResourceProvider");
		resourceProviderViewModel.setIdentifier(id);
		
		ResourceProviderModel rpm = 
				resourceProviderService.updateResourceProvider(
						BeanMapperUtils.beanMapper(resourceProviderViewModel, ResourceProviderModel.class));
		
		return changeToViewModel(rpm);
	}
	
	/**
	 * 删除资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, String> deleteResourceProvider(@PathVariable String id){
		
		resourceProviderService.deleteResourceProvider(id);
		
		return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteProviderSuccess);
	}
	
	/**
	 * 查询资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param words
	 * @param limit
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ListViewModel<ResourceProviderViewModel> getResourceProviderList(@RequestParam String words,@RequestParam String limit){
		
		ListViewModel<ResourceProviderModel> rpmList = resourceProviderService.getResourceProviderList(words, limit);
		
		ListViewModel<ResourceProviderViewModel> viewListResult = new ListViewModel<ResourceProviderViewModel>();
		viewListResult.setLimit(rpmList.getLimit());
		viewListResult.setTotal(rpmList.getTotal());
		List<ResourceProviderModel> modelItems = rpmList.getItems();
		List<ResourceProviderViewModel> viewItems = new ArrayList<ResourceProviderViewModel>();
		if (CollectionUtils.isNotEmpty(modelItems)) {
			for (ResourceProviderModel model : modelItems) {
				ResourceProviderViewModel viewModel = changeToViewModel(model);
				viewItems.add(viewModel);
			}
		}
		viewListResult.setItems(viewItems);
		return viewListResult;
	}
	
	/**
	 * Model转成viewModel
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param rpm
	 * @return
	 */
	private ResourceProviderViewModel changeToViewModel(ResourceProviderModel rpm){
		
		return BeanMapperUtils.beanMapper(rpm, ResourceProviderViewModel.class);
	}
}
