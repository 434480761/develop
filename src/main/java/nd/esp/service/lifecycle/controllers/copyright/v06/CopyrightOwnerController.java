package nd.esp.service.lifecycle.controllers.copyright.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import nd.esp.service.lifecycle.models.copyright.v06.CopyrightOwnerModel;
import nd.esp.service.lifecycle.services.copyright.v06.CopyrightOwnerService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.busi.ValidResultHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.copyright.v06.CopyrightOwnerViewModel;

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
 * 资源版权方 Controller层
 * @author xiezy
 * @date 2016年8月15日
 */
@RestController
@RequestMapping("/v0.6/copyright/provider")
public class CopyrightOwnerController {
	@Autowired
	private CopyrightOwnerService copyrightOwnerService;
	
	/**
	 * 创建资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param resourceProviderViewModel
	 * @param validResult
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public CopyrightOwnerViewModel createCopyrightOwner(@Valid @RequestBody CopyrightOwnerViewModel copyrightOwnerViewModel,
									   BindingResult validResult){
		
		//校验
		ValidResultHelper.valid(validResult, "LC/CREATE_COPYRIGHT_OWNER_PARAM_VALID_FAIL", "CopyrightOwnerController", "createResourceProvider");
		copyrightOwnerViewModel.setIdentifier(UUID.randomUUID().toString());
		
		CopyrightOwnerModel com = 
				copyrightOwnerService.createCopyrightOwner(
						BeanMapperUtils.beanMapper(copyrightOwnerViewModel, CopyrightOwnerModel.class));
		
		return changeToViewModel(com);
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
	public CopyrightOwnerViewModel updateCopyrightOwner(@PathVariable String id,
			@Valid @RequestBody CopyrightOwnerViewModel copyrightOwnerViewModel,BindingResult validResult){
		
		//校验
		ValidResultHelper.valid(validResult, "LC/UPDATE_COPYRIGHT_OWNER_PARAM_VALID_FAIL", "CopyrightOwnerController", "updateResourceProvider");
		copyrightOwnerViewModel.setIdentifier(id);
		
		CopyrightOwnerModel com = 
				copyrightOwnerService.updateCopyrightOwner(
						BeanMapperUtils.beanMapper(copyrightOwnerViewModel, CopyrightOwnerModel.class));
		
		return changeToViewModel(com);
	}
	
	/**
	 * 删除资源提供商
	 * @author xiezy
	 * @date 2016年8月15日
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, String> deleteCopyrightOwner(@PathVariable String id){
		
		copyrightOwnerService.deleteCopyrightOwner(id);
		
		return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteCopyrightOwnerSuccess);
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
	public ListViewModel<CopyrightOwnerViewModel> getCopyrightOwnerList(@RequestParam String words,@RequestParam String limit){
		
		ListViewModel<CopyrightOwnerModel> comList = copyrightOwnerService.getCopyrightOwnerList(words, limit);
		
		ListViewModel<CopyrightOwnerViewModel> viewListResult = new ListViewModel<CopyrightOwnerViewModel>();
		viewListResult.setLimit(comList.getLimit());
		viewListResult.setTotal(comList.getTotal());
		List<CopyrightOwnerModel> modelItems = comList.getItems();
		List<CopyrightOwnerViewModel> viewItems = new ArrayList<CopyrightOwnerViewModel>();
		if (CollectionUtils.isNotEmpty(modelItems)) {
			for (CopyrightOwnerModel model : modelItems) {
				CopyrightOwnerViewModel viewModel = changeToViewModel(model);
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
	private CopyrightOwnerViewModel changeToViewModel(CopyrightOwnerModel com){
		
		return BeanMapperUtils.beanMapper(com, CopyrightOwnerViewModel.class);
	}
}
