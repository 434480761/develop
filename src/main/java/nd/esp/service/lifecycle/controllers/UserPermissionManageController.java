package nd.esp.service.lifecycle.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;
import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.models.UserCoverageMappingModel;
import nd.esp.service.lifecycle.models.UserRestypeMappingModel;
import nd.esp.service.lifecycle.services.usercoveragemapping.v06.UserCoverageMappingService;
import nd.esp.service.lifecycle.services.userrestypemapping.v06.UserRestypeMappingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ResTypeEunm;
import nd.esp.service.lifecycle.support.uc.UcClient;
import nd.esp.service.lifecycle.support.uc.UserBaseInfo;
import nd.esp.service.lifecycle.support.uc.UserItems;
import nd.esp.service.lifecycle.utils.AssertUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.vos.UserRoleViewModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 *
 * @author lanyl
 */
@RestController
@RequestMapping({"/v0.6/users"})
public class UserPermissionManageController {

	private final Logger LOG = LoggerFactory.getLogger(UserPermissionManageController.class);

	@Autowired
	@Qualifier("UserRestypeMappingServiceImpl")
	private UserRestypeMappingService userRestypeMappingService;

	@Autowired
	@Qualifier("UserCoverageMappingServiceImpl")
	private UserCoverageMappingService userCoverageMappingService;

	@Autowired
	private UcClient ucClient;

	/** 超级管理员*/
	private static String SUPERADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_super_admin");
	/** 库管理员*/
	private static String COVERAGEADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_coverage_admin");
	/** 资源创建者角色*/
	private static String RESCREATOR = LifeCircleApplicationInitializer.properties.getProperty("esp_res_creator");
	/** 维度管理者角色*/
	private static String CATEGORYDATAADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_category_data_admin");
	/** 资源消费者角色*/
	private static String RESCONSUMER = LifeCircleApplicationInitializer.properties.getProperty("esp_res_consumer");
	/** 游客角色*/
	private static String GUEST = LifeCircleApplicationInitializer.properties.getProperty("esp_guest");


	/**
	 * 绑定用户角色
	 * @param userId
	 * @param jsonObject
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value="/{userId}/roles",method = RequestMethod.POST)
	public Map<String, Object> addUserRole(@PathVariable String userId, @RequestBody JSONObject jsonObject, @AuthenticationPrincipal UserInfo userInfo){

		String roleId = jsonObject.getString("role_id");
		JSONArray coverageArray = jsonObject.getJSONArray("coverages");
		JSONArray resTypeArray = jsonObject.getJSONArray("res_types");

		//参数效验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.rangeLength(userId, 0, 36, "user_id");

		// 覆盖范围List
		List<String> coverageList = new ArrayList<String>();
		// 请求类型List
		List<String> resTypeList = new ArrayList<String>();
		//获取覆盖范围
		coverageList = this.jsonArrayChangeToList(coverageArray);
		//获取请求类型
		resTypeList = this.jsonArrayChangeToList(resTypeArray);
		//效验请求类型参数
		AssertUtils.isMatches(resTypeList, ResTypeEunm.getRegex(), "res_types");

		//roleid不为空 绑定uc角色
		if(StringUtils.isNotBlank(roleId)){
			AssertUtils.isInteger(roleId, "role_id");
			this.isValidRoleId(roleId, "role_id");
			//判断当前用户角色是否有绑定用户权限，没有则报错，有则继续下一步操作
			this.validHasPermission(userInfo, roleId);

			//用户权限角色不存在，进行新增绑定, 存在则不再进行绑定
			if(!this.hasRoleIdByUserId(userId, roleId)){
				//添加用户角色
				this.ucClient.addUserRole(userId,Integer.valueOf(roleId));
			}
		}
		//coverage不为空，创建覆盖类型关系
		if(coverageList != null && coverageList.size() >0){
			this.userCoverageMappingService.addUserCoverageMappings(coverageList, userId);
		}
		//resType不为空，创建请求类型关系
		if(resTypeList != null && resTypeList.size() > 0){
			this.userRestypeMappingService.addUserRestypeMappings(resTypeList, userId);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("user_id", userId);
		params.put("role_id", roleId);
		params.put("coverages", coverageList.toArray());
		params.put("res_types", resTypeList.toArray());
		return 	params;
	}

	/**
	 * 解除绑定用户角色
	 * @param userId
	 * @param jsonObject
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value="/{userId}/roles",method = RequestMethod.DELETE)
	public Map<String, Object> deleteUserRole(@PathVariable String userId, @RequestBody JSONObject jsonObject, @AuthenticationPrincipal UserInfo userInfo){
		//获取参数
		String roleId = jsonObject.getString("role_id");
		JSONArray coverageArray = jsonObject.getJSONArray("coverages");
		JSONArray resTypeArray = jsonObject.getJSONArray("res_types");

		//参数效验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.rangeLength(userId, 0, 36, "user_id");
		// 覆盖范围List
		List<String> coverageList = new ArrayList<String>();
		// 请求类型List
		List<String> resTypeList = new ArrayList<String>();
		//获取覆盖范围,删除对应数据
		coverageList = this.jsonArrayChangeToList(coverageArray);
		//获取请求类型
		resTypeList = this.jsonArrayChangeToList(resTypeArray);
		//效验请求类型参数
		AssertUtils.isMatches(resTypeList, ResTypeEunm.getRegex(), "res_types");
		//返回内容
		Map<String, Object> params = new HashMap<String, Object>();

		//roleId不为空，进行解除绑定用户角色操作
		if(StringUtils.isNotBlank(roleId)){
			AssertUtils.isInteger(roleId, "role_id");
			this.isValidRoleId(roleId, "role_id");
			//判断当前用户角色是否有解除绑定用户权限，没有则报错，有则继续下一步操作
			this.validHasPermission(userInfo, roleId);
			// 存在则进行解绑
			if( this.hasRoleIdByUserId(userId, roleId) ){
				//解除用户角色绑定
				this.ucClient.deleteUserRole(userId,Integer.valueOf(roleId));
			}
		}
		//coverage不为空, 删除覆盖关系
		if(coverageList != null && coverageList.size() >0){
			this.userCoverageMappingService.deleteUserCoverageMappings(coverageList, userId);
		}

		//resType不为空，删除请求关系
		if(resTypeList != null && resTypeList.size() > 0){
			this.userRestypeMappingService.deleteUserRestypeMappings(resTypeList, userId);
		}
		params.put("user_id", userId);
		params.put("role_id", roleId);
		params.put("coverages", coverageList.toArray());
		params.put("res_types", resTypeList.toArray());
		return params;

	}
	/**
	 * 查询角色用户列表
	 * @param roleId
	 * @param limit
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value = "/roles/{roleId}/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody Map<String, Object> getUserRoleList(@PathVariable String roleId , @RequestParam(value ="limit",required =true) String limit) {
		//参数roleId效验
		AssertUtils.isEmpty(roleId, "role_id");
		AssertUtils.isInteger(roleId, "role_id");
		AssertUtils.rangeLength(roleId, 0, 36, "role_id");
		this.isValidRoleId(roleId, "role_id");
		// 检查limit参数
		ParamCheckUtil.checkLimit(limit);// 有抛出异常
		// 分解数据 获取limit跟offset
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);


		//根据角色id获取角色名
		String roleName = this.getRoleName(roleId);

		//通过uc接口获取roleid下的用户 with limit and offset
		UserItems userItems = this.ucClient.listRoleUsers(roleId,null,limitResult[0],limitResult[1]);
		List<String> userIdList = new ArrayList<String>(userItems.size());

		ArrayList<UserRoleViewModel> userRoleViewModelArrayList = new ArrayList<UserRoleViewModel>();
		for (UserBaseInfo userInfo : userItems) {
			//用户角色返回数据model
			UserRoleViewModel userRoleViewModel = new UserRoleViewModel();
			userRoleViewModel.setUserId(userInfo.getUserId());
			userRoleViewModel.setRoleId(roleId);
			userRoleViewModel.setRoleName(roleName);
			userRoleViewModel.setUserName(userInfo.getUserName());
			userRoleViewModelArrayList.add(userRoleViewModel);
			//获取用户id列表
			userIdList.add(userInfo.getUserId());
		}
		//根据用户id列表 获取用户覆盖映射关系列表
		List<UserCoverageMappingModel> userCoverageMappingModelList = this.userCoverageMappingService.findUserCoverageMappingModelList(userIdList);
		//根据用户id列表 获取用户请求映射关系列表
		List<UserRestypeMappingModel> userRestypeMappingModelList = this.userRestypeMappingService.findUserRestypeMappingModelList(userIdList);

		//循环封装返回数据结构
		for(int i=0; i <userRoleViewModelArrayList.size(); i++){
			String userId = userRoleViewModelArrayList.get(i).getUserId();
			//设置coverages
			userRoleViewModelArrayList.get(i).setCoverages(this.getCoverageList(userId, userCoverageMappingModelList));
			//设置resTypes
			userRoleViewModelArrayList.get(i).setResTypes(this.getResTypeList(userId, userRestypeMappingModelList));
		}
		//返回内容
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("limit", limit);
		result.put("items",userRoleViewModelArrayList.toArray());
		return result;
	}

	/**
	 * 查询角色用户
	 * @param roleId
	 * @param userId
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value = "/{userId}/roles/{roleId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public UserRoleViewModel getUserRole(@PathVariable String userId ,@PathVariable String roleId ,@AuthenticationPrincipal UserInfo UserInfo) {
		//参数userId效验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.isLong(userId, "user_id");
		//参数roleId效验
		AssertUtils.isEmpty(roleId, "role_id");
		AssertUtils.isInteger(roleId, "role_id");
		AssertUtils.rangeLength(roleId, 0, 36, "role_id");
		//角色id参数只能为6种权限角色中的一种
		this.isValidRoleId(roleId, "role_id");
		//用户角色返回数据model
		UserRoleViewModel userRoleViewModel = new UserRoleViewModel();

		//根据角色id获取角色名
		String roleName = this.getRoleNameByUserId(userId,roleId);
		if(StringUtils.isNotBlank(roleName)){
			userRoleViewModel.setRoleId(roleId);
			userRoleViewModel.setRoleName(roleName);
		}

		//通过uc接口获取用户角色
		String userName = this.ucClient.getUserName(userId);

		userRoleViewModel.setUserId(userId);
		userRoleViewModel.setUserName(userName);
		//根据用户id 获取用户覆盖映射关系列表
		userRoleViewModel.setCoverages(this.userCoverageMappingService.findUserCoverageList(userId));
		//根据用户id 获取用户请求映射关系列表
		userRoleViewModel.setResTypes(this.userRestypeMappingService.findUserRestypelList(userId));

		return userRoleViewModel;
	}

//	/**
//	 * 绑定用户角色
//	 * @param userId
//	 * @param jsonObject
//	 * @return
//	 * @author lanyl
//	 */
//	@RequestMapping(value="/{userId}/roles",method = RequestMethod.POST)
//	public Map<String, Object> addUserRole(@PathVariable String userId, @RequestBody JSONObject jsonObject, @AuthenticationPrincipal UserInfo userInfo){
//
//		String roleId = jsonObject.getString("role_id");
//		JSONArray coverageArray = jsonObject.getJSONArray("coverages");
//		JSONArray resTypeArray = jsonObject.getJSONArray("res_types");
//
//		//参数效验
//		AssertUtils.isEmpty(roleId, "role_id");
//		AssertUtils.isEmpty(userId, "user_id");
//		AssertUtils.rangeLength(userId, 0, 36, "user_id");
//		AssertUtils.isInteger(roleId, "role_id");
//		this.isValidRoleId(roleId, "role_id");
//
//		// 覆盖范围List
//		List<String> coverageList = new ArrayList<String>();
//		// 请求类型List
//		List<String> resTypeList = new ArrayList<String>();
//
//		//判断当前用户角色是否有绑定用户权限，没有则报错，有则继续下一步操作
//		this.validHasPermission(userInfo, roleId);
//
//		//用户权限角色不存在，进行新增绑定, 存在则不再进行绑定
//		if(!this.hasRoleIdByUserId(userId, roleId)){
//			//添加用户角色
//			this.ucClient.addUserRole(userId,Integer.valueOf(roleId));
//		}
//
//		//coverage跟库管理员，资源创建者角色，资源消费者角色有关
//		if(COVERAGEADMIN.equals(roleId) || RESCREATOR.equals(roleId) || RESCONSUMER.equals(roleId)){
//			//获取覆盖范围
//			coverageList = this.jsonArrayChangeToList(coverageArray);
//			if(coverageList != null && coverageList.size() >0){
//				this.userCoverageMappingService.addUserCoverageMappings(coverageList, userId);
//			}
//		}
//		//resType跟资源创建者角色，资源消费者角色有关
//		if(RESCREATOR.equals(roleId) || RESCONSUMER.equals(roleId)){
//			//获取请求类型
//			resTypeList = this.jsonArrayChangeToList(resTypeArray);
//			//效验请求类型参数
//			AssertUtils.isMatches(resTypeList, ResTypeEunm.getRegex(), "res_types");
//			if(resTypeList != null && resTypeList.size() > 0){
//				this.userRestypeMappingService.addUserRestypeMappings(resTypeList, userId);
//			}
//		}
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("user_id", userId);
//		params.put("role_id", roleId);
//		params.put("coverages", coverageList.toArray());
//		params.put("res_types", resTypeList.toArray());
//		return 	params;
//	}


//	/**
//	 * 解除绑定用户角色
//	 * @param userId
//	 * @param jsonObject
//	 * @return
//	 * @author lanyl
//	 */
//	@RequestMapping(value="/{userId}/roles",method = RequestMethod.DELETE)
//	public Map<String, Object> deleteUserRole(@PathVariable String userId, @RequestBody JSONObject jsonObject, @AuthenticationPrincipal UserInfo userInfo){
//		String roleId = jsonObject.getString("role_id");
//		JSONArray coverageArray = jsonObject.getJSONArray("coverages");
//		JSONArray resTypeArray = jsonObject.getJSONArray("res_types");
//
//		//参数效验
//		AssertUtils.isEmpty(roleId, "role_id");
//		AssertUtils.isEmpty(userId, "user_id");
//		AssertUtils.rangeLength(userId, 0, 36, "user_id");
//		AssertUtils.isInteger(roleId, "role_id");
//		this.isValidRoleId(roleId, "role_id");
//		// 覆盖范围List
//		List<String> coverageList = new ArrayList<String>();
//		// 请求类型List
//		List<String> resTypeList = new ArrayList<String>();
//		//返回内容
//		Map<String, Object> params = new HashMap<String, Object>();
//
//		//判断当前用户角色是否有解除绑定用户权限，没有则报错，有则继续下一步操作
//		this.validHasPermission(userInfo, roleId);
//
//		// 存在则进行解绑
//		if( this.hasRoleIdByUserId(userId, roleId) ){
//			//当coverages跟res_types为空时，进行uc角色解绑
//			if(coverageArray == null && resTypeArray ==null ){
//				//解除用户角色绑定
//				this.ucClient.deleteUserRole(userId,Integer.valueOf(roleId));
//				if(COVERAGEADMIN.equals(roleId) || RESCREATOR.equals(roleId) || RESCONSUMER.equals(roleId)){
//					this.userCoverageMappingService.deleteAllUserCoverageMappingsByUserId(userId);
//				}
//				if(RESCREATOR.equals(roleId) || RESCONSUMER.equals(roleId)){
//					this.userRestypeMappingService.deleteAllUserResTypeMappingsByUserId(userId);
//				}
//			}else{
//				//获取覆盖范围,删除对应数据
//				coverageList = this.jsonArrayChangeToList(coverageArray);
//				//获取请求类型
//				resTypeList = this.jsonArrayChangeToList(resTypeArray);
//				//效验请求类型参数
//				AssertUtils.isMatches(resTypeList, ResTypeEunm.getRegex(), "res_types");
//				//coverage跟库管理员，资源创建者角色，资源消费者角色有关
//				if(COVERAGEADMIN.equals(roleId) || RESCREATOR.equals(roleId) || RESCONSUMER.equals(roleId)){
//					//删除覆盖关系
//					if(coverageList != null && coverageList.size() >0){
//						this.userCoverageMappingService.deleteUserCoverageMappings(coverageList, userId);
//					}
//				}
//				//resType跟资源创建者角色，资源消费者角色有关
//				if(RESCREATOR.equals(roleId) || RESCONSUMER.equals(roleId)){
//					//删除请求关系
//					if(resTypeList != null && resTypeList.size() > 0){
//						this.userRestypeMappingService.deleteUserRestypeMappings(resTypeList, userId);
//					}
//				}
//
//			}
//			params.put("user_id", userId);
//			params.put("role_id", roleId);
//			params.put("coverages", coverageList.toArray());
//			params.put("res_types", resTypeList.toArray());
//		}else {
//			throw new LifeCircleException(HttpStatus.NOT_FOUND, LifeCircleErrorMessageMapper.userRoleNotFound.getCode()
//					, userId + LifeCircleErrorMessageMapper.userRoleNotFound.getMessage());
//		}
//		return params;
//
//	}

	/**
	 * json数组转list
	 * @param jsonArray
	 * @return
	 * @author lanyl
	 */
	private List<String> jsonArrayChangeToList(JSONArray jsonArray){
		ArrayList<String> arrayList = new ArrayList<String>();
		if(jsonArray != null && jsonArray.size() > 0 ){
			for (int i = 0; i < jsonArray.size(); i++) {
				String field = jsonArray.getString(i);
				arrayList.add(field);
			}
		}
		return arrayList;
	}

	/**
	 * 验证当前用户权限角色id 是否有权限
	 * @param userInfo
	 * @param roleId 需要绑定的角色id
	 * @return
	 * @author lanyl
	 */
	private void validHasPermission(UserInfo userInfo, String roleId) {
		//当前用户角色id
		String tmpRoleId = null;
		for(UserCenterRoleDetails userCenterRoleDetails: userInfo.getUserRoles()){
			//超级管理员
			if(userCenterRoleDetails.getRoleId().equals(SUPERADMIN)){
				tmpRoleId = SUPERADMIN;
				break;
			}
			//库管理员
			if(userCenterRoleDetails.getRoleId().equals(COVERAGEADMIN)){
				tmpRoleId = COVERAGEADMIN;
				break;
			}
		}
		//如果当前用户角色id不属于超级管理员和库管理员 则没有绑定/解除用户的权限
		if(tmpRoleId == null){
			//抛错
			throw new LifeCircleException(HttpStatus.FORBIDDEN,LifeCircleErrorMessageMapper.accessDenied.getCode()
					, LifeCircleErrorMessageMapper.accessDenied.getMessage());
		}else if(tmpRoleId.equals(COVERAGEADMIN)
				&& (COVERAGEADMIN.equals(roleId) || SUPERADMIN.equals(roleId))){
			//如果当前用户的roleid属于库管理员  则没有绑定/解除超级管理员和库管理员的权限、抛错
			throw new LifeCircleException(HttpStatus.FORBIDDEN,LifeCircleErrorMessageMapper.libAdminDenied.getCode()
					, LifeCircleErrorMessageMapper.libAdminDenied.getMessage());
		}
	}


	/**
	 * 效验roleid参数是否合法
	 * @param roleId
	 * @param message
	 */
	private void isValidRoleId(String roleId, String message){
		if(SUPERADMIN.equals(roleId) || COVERAGEADMIN.equals(roleId) ||RESCREATOR.equals(roleId) || CATEGORYDATAADMIN.equals(roleId) || RESCONSUMER.equals(roleId) || GUEST.equals(roleId)){

		}else {
			throw new LifeCircleException(HttpStatus.BAD_REQUEST,LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode()
					, message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
		}
	}


	/**
	 * 判断用户是否存在该角色
	 * @param userId
	 * @param roleId
	 * @return
	 */
	private boolean hasRoleIdByUserId(String userId, String roleId){
		boolean flag = false;
		JSONObject jsonObject = this.ucClient.listUserRoles(userId);
		if(jsonObject != null && StringUtils.isNotBlank(roleId)){
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			if(jsonArray != null && jsonArray.size() > 0){
				Integer size = jsonArray.size();
				for(int i = 0; i < size; i++){
					if(roleId.equals(jsonArray.getJSONObject(i).getString("role_id"))){
						flag = true;
						break;
					}
				}
			}
		}
		return flag;
	}


	/**
	 * 通过用户id跟roleid 查询角色名称
	 * @param userId
	 * @param roleId
	 * @return
	 */
	private String getRoleNameByUserId(String userId, String roleId){
		JSONObject jsonObject = this.ucClient.listUserRoles(userId);
		if(jsonObject != null && StringUtils.isNotBlank(roleId)){
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			if(jsonArray != null && jsonArray.size() > 0){
				Integer size = jsonArray.size();
				for(int i = 0; i < size; i++){
					if(roleId.equals(jsonArray.getJSONObject(i).getString("role_id"))){
						return jsonArray.getJSONObject(i).getString("role_name");
					}
				}
			}
		}
		return null;
	}

	/**
	 *　根据角色id获取角色名
	 * @param roleId
	 * @return
	 * @author lanyl
	 */
	private String getRoleName(String roleId){
		JSONObject jsonObject = this.ucClient.listRealmRoles();
		if(jsonObject != null && StringUtils.isNotBlank(roleId)){
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			if(jsonArray != null && jsonArray.size() > 0){
				Integer size = jsonArray.size();
				for(int i = 0; i < size; i++){
					if(roleId.equals(jsonArray.getJSONObject(i).getString("role_id"))){
						return jsonArray.getJSONObject(i).getString("role_name");
					}
				}
			}
		}
		return null;
	}

	/**
	 * 根据用户id获取覆盖类型列表
	 * @param userId
	 * @param userCoverageMappingModelList
	 * @return
	 * @author lanyl
	 */
	private List<String> getCoverageList(String userId, List<UserCoverageMappingModel> userCoverageMappingModelList){
		ArrayList<String> coverageList= new ArrayList<String>();
		if(StringUtils.isNotBlank(userId)){
			for(UserCoverageMappingModel userCoverageMappingModel : userCoverageMappingModelList){
				if(userId.equals(userCoverageMappingModel.getUserId())){
					coverageList.add(userCoverageMappingModel.getCoverage());
				}
			}
		}
		return coverageList;
	}

	/**
	 * 根据用户id获取请求类型列表
	 * @param userId
	 * @param userRestypeMappingModelList
	 * @return
	 * @author lanyl
	 */
	private List<String> getResTypeList(String userId, List<UserRestypeMappingModel> userRestypeMappingModelList){
		ArrayList<String> resTypeList= new ArrayList<String>();
		if(StringUtils.isNotBlank(userId)){
			for(UserRestypeMappingModel userRestypeMappingModel : userRestypeMappingModelList){
				if(userId.equals(userRestypeMappingModel.getUserId())){
					resTypeList.add(userRestypeMappingModel.getResType());
				}
			}
		}
		return resTypeList;
	}

//	/**
//	 * 查询用户是否存在权限角色
//	 * @param userId
//	 * @return
//	 * @author lanyl
//	 */
//	private String getRoleIdByUserId(String userId){
//		JSONObject jsonObject = this.ucClient.listUserRoles(userId);
//		if(jsonObject != null){
//			JSONArray jsonArray = jsonObject.getJSONArray("items");
//			if(jsonArray != null && jsonArray.size() > 0){
//				Integer size = jsonArray.size();
//				for(int i = 0; i < size; i++){
//					String roleId = jsonArray.getJSONObject(i).getString("role_id");
//					if(SUPERADMIN.equals(roleId)){
//						return SUPERADMIN;
//					}
//					if(COVERAGEADMIN.equals(roleId)){
//						return COVERAGEADMIN;
//					}
//					if(RESCREATOR.equals(roleId)){
//						return RESCREATOR;
//					}
//					if(CATEGORYDATAADMIN.equals(roleId)){
//						return CATEGORYDATAADMIN;
//					}
//					if(RESCONSUMER.equals(roleId)){
//						return RESCONSUMER;
//					}
//					if(GUEST.equals(roleId)){
//						return GUEST;
//					}
//				}
//			}
//		}
//		return null;
//	}
}
