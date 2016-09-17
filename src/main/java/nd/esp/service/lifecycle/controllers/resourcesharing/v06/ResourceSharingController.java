package nd.esp.service.lifecycle.controllers.resourcesharing.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.models.ResourceModel;
import nd.esp.service.lifecycle.educommon.services.NDResourceService;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.educommon.vos.constant.IncludesConstant;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.models.resourcesharing.v06.ResourceSharingModel;
import nd.esp.service.lifecycle.services.resourcesharing.v06.ResourceSharingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.BeanMapperUtils;
import nd.esp.service.lifecycle.utils.CollectionUtils;
import nd.esp.service.lifecycle.utils.MessageConvertUtil;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.resourcesharing.v06.ResourceSharingViewModel;
import nd.esp.service.lifecycle.vos.statics.ResourceType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nd.gaea.rest.security.authens.UserInfo;

/**
 * 资源分享Controller层
 * @author xiezy
 * @date 2016年8月29日
 */
@RestController
@RequestMapping(value={"/v0.6"})
public class ResourceSharingController {
	
	@Autowired
	private ResourceSharingService resourceSharingService;
	
	@Autowired
    private NDResourceService ndResourceService;
	
	@Autowired
	private CommonServiceHelper commonServiceHelper;
	
	/**
	 * 将资源进行私密分享，生成资源的访问分享id和临时密码。
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resourceSharingViewModel
	 * @param resType
	 * @param resourceId
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value="{res_type}/{resource_uuid}/public/protected_sharing", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResourceSharingViewModel createResourceSharing(
			@RequestBody ResourceSharingViewModel resourceSharingViewModel,
			@PathVariable(value="res_type") String resType,
			@PathVariable(value="resource_uuid") String resourceId,
			@AuthenticationPrincipal UserInfo userInfo){
		
		// 用户信息校验
		if (userInfo == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.GetUserInfoFail);
		}
		
		//校验资源是否存在
		CommonHelper.resourceExist(resType, resourceId, ResourceType.RESOURCE_SOURCE);
		
		resourceSharingViewModel.setIdentifier(UUID.randomUUID().toString());
		resourceSharingViewModel.setResourceType(resType);
		resourceSharingViewModel.setResource(resourceId);
		
		ResourceSharingModel resourceSharingModel = resourceSharingService.createResourceSharing(
				BeanMapperUtils.beanMapper(resourceSharingViewModel, ResourceSharingModel.class), userInfo);
		
		return BeanMapperUtils.beanMapper(resourceSharingModel, ResourceSharingViewModel.class);
	}
	
	/**
	 * 删除一个资源的全部私密分享，执行后之前所有的分享将失效。仅针对本人分享的记录。
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resType
	 * @param resourceId
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/{res_type}/{resource_uuid}/public/protected_sharing", method = RequestMethod.DELETE)
	public Map<String,String> deleteAllResourceSharingBySomeone(
			@PathVariable(value="res_type") String resType,
			@PathVariable(value="resource_uuid") String resourceId,
			@AuthenticationPrincipal UserInfo userInfo){
		
		// 用户信息校验
		if (userInfo == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.GetUserInfoFail);
		}
		
		//校验资源是否存在
		CommonHelper.resourceExist(resType, resourceId, ResourceType.RESOURCE_SOURCE);
		
		resourceSharingService.deleteAllResourceSharingBySomeone(resType, resourceId, userInfo);
		return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceSharingSuccess);
	}
	
	/**
	 * 通过分享记录id删除一笔资源的私密分享，执行后分享将失效。只能取消本人分享的记录。
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resType
	 * @param resourceId
	 * @param sharingId
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value = "/{res_type}/{resource_uuid}/public/protected_sharing/{sharing_uuid}", method = RequestMethod.DELETE)
	public Map<String,String> deleteResourceSharingBySharingId(
			@PathVariable(value="res_type") String resType,
			@PathVariable(value="resource_uuid") String resourceId,
			@PathVariable(value="sharing_uuid") String sharingId,
			@AuthenticationPrincipal UserInfo userInfo){
		
		// 用户信息校验
		if (userInfo == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.GetUserInfoFail);
		}
		
		//校验资源是否存在
		CommonHelper.resourceExist(resType, resourceId, ResourceType.RESOURCE_SOURCE);
		
		resourceSharingService.deleteResourceSharingBySharingId(resType, resourceId, sharingId, userInfo);
		return MessageConvertUtil.getMessageString(LifeCircleErrorMessageMapper.DeleteResourceSharingSuccess);
	}
	
	/**
	 * 获取资源的私密分享记录列表，只能查询到本人分享的记录。
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resType
	 * @param resourceId
	 * @param limit
	 * @param userInfo
	 * @return
	 */
	@RequestMapping(value="/{res_type}/{resource_uuid}/public/protected_sharing", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE }, params = {"limit"})
	public ListViewModel<ResourceSharingViewModel> getResourceSharingList(
			@PathVariable(value="res_type") String resType,
			@PathVariable(value="resource_uuid") String resourceId,
			@RequestParam String limit,
			@AuthenticationPrincipal UserInfo userInfo){
		
		// 用户信息校验
		if (userInfo == null) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.GetUserInfoFail);
		}

		// 校验资源是否存在
		CommonHelper.resourceExist(resType, resourceId, ResourceType.RESOURCE_SOURCE);
		
		// 查询
		ListViewModel<ResourceSharingModel> csList = resourceSharingService.
				getResourceSharingList(resType, resourceId, limit, userInfo);

		// 返回结果
		ListViewModel<ResourceSharingViewModel> viewListResult = new ListViewModel<ResourceSharingViewModel>();
		viewListResult.setLimit(csList.getLimit());
		viewListResult.setTotal(csList.getTotal());
		List<ResourceSharingModel> modelItems = csList.getItems();
		List<ResourceSharingViewModel> viewItems = new ArrayList<ResourceSharingViewModel>();
		if (CollectionUtils.isNotEmpty(modelItems)) {
			for (ResourceSharingModel model : modelItems) {
				ResourceSharingViewModel viewModel = new ResourceSharingViewModel();
				viewModel.setIdentifier(model.getIdentifier());
				viewModel.setTitle(model.getTitle());
				
				viewItems.add(viewModel);
			}
		}
		viewListResult.setItems(viewItems);
		return viewListResult;
	}
	
	/**
	 * 通过分享后的id查看资源信息。返回资源内容。
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resourceSharingViewModel
	 * @param sharingId
	 * @return
	 */
	@RequestMapping(value="/public/protected_sharing/{sharing_uuid}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResourceViewModel getResourceBySharingId(
			@RequestBody ResourceSharingViewModel resourceSharingViewModel,
			@PathVariable(value="sharing_uuid") String sharingId){
		
		// 校验
		if (!StringUtils.hasText(resourceSharingViewModel.getProtectPasswd())) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.ProtectPasswdIsNotEmpty);
		}

		ResourceSharingModel rsm = resourceSharingService.verifyPasswd(
				sharingId, resourceSharingViewModel.getProtectPasswd());

		if (rsm == null) {
			throw new LifeCircleException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.ProtectedSharingResourceNotFound);
		}

		ResourceModel model = ndResourceService.getDetail(
				rsm.getResourceType(), rsm.getResource(), IncludesConstant.getIncludesList());

		return CommonHelper.changeToView(model, rsm.getResourceType(),
				IncludesConstant.getIncludesList(), commonServiceHelper);
	}
	
	/**
	 * 通过分享后的id和密码，获取资源的下载地址。
	 * @author xiezy
	 * @date 2016年8月29日
	 * @param resourceSharingViewModel
	 * @param sharingId
	 * @param key
	 * @return
	 */
	@RequestMapping(value="/public/protected_sharing/{sharing_uuid}/download", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	public AccessModel getDownloadUrlBySharingId(
			@RequestBody ResourceSharingViewModel resourceSharingViewModel,
			@PathVariable(value="sharing_uuid") String sharingId,
			@RequestParam(required = false, value = "key") String key,
			@AuthenticationPrincipal UserInfo userInfo){
		
		// 校验
		if (!StringUtils.hasText(resourceSharingViewModel.getProtectPasswd())) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.ProtectPasswdIsNotEmpty);
		}
		
		ResourceSharingModel rsm = resourceSharingService.verifyPasswd(
				sharingId, resourceSharingViewModel.getProtectPasswd());

		if (rsm == null) {
			throw new LifeCircleException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					LifeCircleErrorMessageMapper.ProtectedSharingResourceNotFound);
		}
		
		// 校验资源是否可下载
		commonServiceHelper.assertDownloadable(rsm.getResourceType());
		String uid = userInfo == null ? "777" : userInfo.getUserId();// FIXME uid用途不明
		AccessModel accessModel = ndResourceService.getDownloadUrl(rsm.getResourceType(), 
				rsm.getResource(), uid, key);

		accessModel.setAccessKey(null);
		accessModel.setPreview(null);
		accessModel.setUuid(null);
		accessModel.setExpireTime(null);
		return accessModel;
	}
}
