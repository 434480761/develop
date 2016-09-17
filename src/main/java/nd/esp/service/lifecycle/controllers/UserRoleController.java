package nd.esp.service.lifecycle.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;
import nd.esp.service.lifecycle.models.UserCoverageMappingModel;
import nd.esp.service.lifecycle.models.UserRestypeMappingModel;
import nd.esp.service.lifecycle.services.thirdpartybsys.v06.ThirdPartyBsysService;
import nd.esp.service.lifecycle.services.usercoveragemapping.v06.UserCoverageMappingService;
import nd.esp.service.lifecycle.services.userrestypemapping.v06.UserRestypeMappingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.enums.ResTypeEnum;
import nd.esp.service.lifecycle.support.enums.RoleEnum;
import nd.esp.service.lifecycle.support.uc.UcClient;
import nd.esp.service.lifecycle.support.uc.UcRoleClient;
import nd.esp.service.lifecycle.support.uc.UserBaseInfo;
import nd.esp.service.lifecycle.support.uc.UserItems;
import nd.esp.service.lifecycle.utils.AssertUtils;
import nd.esp.service.lifecycle.utils.ParamCheckUtil;
import nd.esp.service.lifecycle.vos.RoleViewModel;
import nd.esp.service.lifecycle.vos.UserRoleListViewModel;
import nd.esp.service.lifecycle.vos.UserRoleViewModel;
import org.apache.commons.lang3.StringUtils;
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
 * <p>Title: UserRoleController</p>
 * <p>Description: UserRoleController</p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/6/28 </p>
 * @author lanyl
 */
@RestController
@RequestMapping({"/v0.6/users"})
public class UserRoleController {

	//private final Logger LOG = LoggerFactory.getLogger(UserRoleController.class);

	@Autowired
	@Qualifier("UserRestypeMappingServiceImpl")
	private UserRestypeMappingService userRestypeMappingService;

	@Autowired
	@Qualifier("UserCoverageMappingServiceImpl")
	private UserCoverageMappingService userCoverageMappingService;

	@Autowired
	@Qualifier("ThirdPartyBsysServiceImpl")
	private ThirdPartyBsysService thirdPartyBsysService;

	@Autowired
	private UcClient ucClient;

	@Autowired
	private UcRoleClient ucRoleClient;


	/**
	 * 绑定用户角色
	 * @param userId
	 * @param jsonObject
	 * @param userInfo
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value="/{userId:\\d+}/roles",method = RequestMethod.POST)
	public Map<String, Object> addUserRole(@PathVariable String userId, @RequestBody JSONObject jsonObject, @AuthenticationPrincipal UserInfo userInfo){
		// 覆盖范围List
		List<String> coverageList = new ArrayList<String>();
		// 请求类型List
		List<String> resTypeList = new ArrayList<String>();
		// 获取参数
		String roleId = jsonObject.getString("role_id");
		JSONArray coverages = AssertUtils.checkJsonArray(jsonObject,"coverages");
		JSONArray resTypes = AssertUtils.checkJsonArray(jsonObject,"res_types");

		//参数效验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.rangeLength(userId, 0, 36, "user_id");
		// 校验覆盖范围
		if(coverages != null){
			//获取覆盖范围
			coverageList = this.jsonArrayChangeToList(coverages);
		}
		// 校验请求类型参数
		if(resTypes != null){
			//获取请求类型
			resTypeList = this.jsonArrayChangeToList(resTypes);
			//效验请求类型参数
			AssertUtils.isMatches(resTypeList, ResTypeEnum.getRegex(), "res_types");
		}

		//roleid不为空 绑定uc角色
		if(StringUtils.isNotBlank(roleId)){
			ucRoleClient.checkValidRoleId(roleId, "role_id");
			//判断当前用户角色是否有绑定用户权限，没有则报错，有则继续下一步操作
			this.validHasPermission(userInfo, roleId);

			//用户权限角色不存在，进行新增绑定, 存在则不再进行绑定
			if(!ucClient.hasRoleIdByUserId(userId, roleId)){
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
		params.put("coverages", coverageList == null ? null :coverageList.toArray());
		params.put("res_types", resTypeList == null ? null : resTypeList.toArray());
		return 	params;
	}

	/**
	 * 解除绑定用户角色
	 * @param userId
	 * @param roleId
	 * @param coverages
	 * @param resTypes
	 * @param userInfo
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value="/{userId:\\d+}/roles",method = RequestMethod.DELETE)
	public Map<String, Object> deleteUserRole(@PathVariable String userId,
											  @RequestParam(required=false,value="role_id") String roleId,
											  @RequestParam(required=false,value="coverages") List<String> coverages,
											  @RequestParam(required=false,value="res_types") List<String> resTypes,
											  @AuthenticationPrincipal UserInfo userInfo){
		//参数效验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.rangeLength(userId, 0, 36, "user_id");

		// 校验请求类型参数
		if(resTypes != null && resTypes.size() > 0){
			//效验请求类型参数
			AssertUtils.isMatches(resTypes, ResTypeEnum.getRegex(), "res_types");
		}
		//返回内容
		Map<String, Object> params = new HashMap<String, Object>();

		//roleId不为空，进行解除绑定用户角色操作
		if(StringUtils.isNotBlank(roleId)){
			ucRoleClient.checkValidRoleId(roleId, "role_id");
			//判断当前用户角色是否有解除绑定用户权限，没有则报错，有则继续下一步操作
			this.validHasPermission(userInfo, roleId);
			// 存在则进行解绑
			if( ucClient.hasRoleIdByUserId(userId, roleId) ){
				//解除用户角色绑定
				this.ucClient.deleteUserRole(userId,Integer.valueOf(roleId));
			}
		}
		//coverage不为空, 删除覆盖关系
		if(coverages != null && coverages.size() >0){
			this.userCoverageMappingService.deleteUserCoverageMappings(coverages, userId);
		}
		//resType不为空，删除请求关系
		if(resTypes != null && resTypes.size() > 0){
			this.userRestypeMappingService.deleteUserRestypeMappings(resTypes, userId);
		}
		params.put("user_id", userId);
		params.put("role_id", roleId);
		params.put("coverages", coverages);
		params.put("res_types", resTypes);
		return params;

	}
	/**
	 * 查询角色用户列表
	 * @param roleId
	 * @param limit
	 * @param orgId
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value = "/roles/{roleId:\\d+}/list", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody Map<String, Object> getUserRoleList(@PathVariable String roleId, 
	        @RequestParam(value = "limit", required = true) String limit,
	        @RequestParam(value = "org_id", required = false) String orgId) {
		//参数roleId效验
		AssertUtils.isEmpty(roleId, "role_id");
		ucRoleClient.checkValidRoleId(roleId, "role_id");
		// 检查limit参数 and 分解数据 获取limit跟offset(有抛出异常)
		Integer limitResult[] = ParamCheckUtil.checkLimit(limit);
		//根据角色id获取角色名
		String roleName = ucClient.getRoleName(roleId);

		//通过uc接口获取roleid下的用户 with limit and offset
		UserItems userItems = this.ucClient.listRoleUsers(roleId,orgId,limitResult[0],limitResult[1]);
		List<String> userIdList = new ArrayList<String>(userItems.size());

		ArrayList<UserRoleListViewModel> userRoleViewModelArrayList = new ArrayList<UserRoleListViewModel>();
		for (UserBaseInfo userInfo : userItems) {
			//用户角色返回数据model
			UserRoleListViewModel userRoleListViewModel = new UserRoleListViewModel();
			userRoleListViewModel.setUserId(userInfo.getUserId());
			userRoleListViewModel.setRoleId(roleId);
			userRoleListViewModel.setRoleName(roleName);
			userRoleListViewModel.setUserName(userInfo.getUserName());
			userRoleViewModelArrayList.add(userRoleListViewModel);
			//获取用户id列表
			userIdList.add(userInfo.getUserId());
		}
		//根据用户id列表 获取用户覆盖映射关系列表
		List<UserCoverageMappingModel> userCoverageMappingModelList = this.userCoverageMappingService.findUserCoverageMappingModelList(userIdList);
		//根据用户id列表 获取用户请求映射关系列表
		List<UserRestypeMappingModel> userRestypeMappingModelList = this.userRestypeMappingService.findUserRestypeMappingModelList(userIdList);

		//循环封装返回数据结构
		for(int i=0; i < userRoleViewModelArrayList.size(); i++){
			String userId = userRoleViewModelArrayList.get(i).getUserId();
			//设置coverages
			userRoleViewModelArrayList.get(i).setCoverages(this.getCoverageList(userId, userCoverageMappingModelList));
			//设置resTypes
			userRoleViewModelArrayList.get(i).setResTypes(this.getResTypeList(userId, userRestypeMappingModelList));
		}
		//返回内容
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("limit", limit);
		result.put("items", userRoleViewModelArrayList);
		return result;
	}

	/**
	 * 查询角色用户
	 * @param userId
	 * @param userInfo
	 * @return
	 * @author lanyl
	 */
	@RequestMapping(value = "/{userId:\\d+}/roles", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public UserRoleViewModel getUserRole(@PathVariable String userId ,@AuthenticationPrincipal UserInfo userInfo) {
		//参数userId效验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.isLong(userId, "user_id");
		
		//用户角色返回数据model
		UserRoleViewModel userRoleViewModel = new UserRoleViewModel();

		//根据角色id获取角色列表
		List<RoleViewModel> roleList = this.getRoleList(userId);
		userRoleViewModel.setRoles(roleList);

		//通过uc接口获取用户角色
		String userName = this.ucClient.getUserName(userId);

		userRoleViewModel.setUserId(userId);
		userRoleViewModel.setUserName(userName);
		//根据用户id 获取用户覆盖映射关系列表
		userRoleViewModel.setCoverages(this.userCoverageMappingService.findUserCoverageList(userId));
		//根据用户id 获取用户请求映射关系列表
		userRoleViewModel.setResTypes(this.userRestypeMappingService.findUserRestypeList(userId));

		return userRoleViewModel;
	}

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
		boolean isThirdBearerUser = false;
		String tmpRoleId = null;
		for(UserCenterRoleDetails userCenterRoleDetails: userInfo.getUserRoles()){
			//判断是否是bearer_token用户
			if(RoleEnum.BEARERTOKEN.getValue().equals(userCenterRoleDetails.getRoleName())){
				if(thirdPartyBsysService.checkThirdPartyBsys(userInfo.getUserId())){
					//bearer_token用户存在白名单中，继续下一步
					isThirdBearerUser = true;
					break;
				}else {
					//bearer_token用户不存在白名单中，抛错
					throw new LifeCircleException(HttpStatus.FORBIDDEN,LifeCircleErrorMessageMapper.Forbidden.getCode()
							, LifeCircleErrorMessageMapper.Forbidden.getMessage());
				}
			}
		}
		//不是bearer_token用户 判断用户拥有的角色权限
		if(!isThirdBearerUser){
			for(UserCenterRoleDetails userCenterRoleDetails: userInfo.getUserRoles()){
				//库管理员
				if(UcRoleClient.COVERAGEADMIN.equals(userCenterRoleDetails.getRoleId())){
					tmpRoleId = UcRoleClient.COVERAGEADMIN;
					break;
				}
			}
			for(UserCenterRoleDetails userCenterRoleDetails: userInfo.getUserRoles()){
				//超级管理员
				if(UcRoleClient.SUPERADMIN.equals(userCenterRoleDetails.getRoleId())){
					tmpRoleId = UcRoleClient.SUPERADMIN;
					break;
				}
			}
			//如果当前用户角色id不属于超级管理员和库管理员 则没有绑定/解除用户的权限
			if(tmpRoleId == null){
				//抛错
				throw new LifeCircleException(HttpStatus.FORBIDDEN,LifeCircleErrorMessageMapper.Forbidden.getCode()
						, LifeCircleErrorMessageMapper.Forbidden.getMessage());
			}else if(tmpRoleId.equals(UcRoleClient.COVERAGEADMIN)
					&& (UcRoleClient.COVERAGEADMIN.equals(roleId) || UcRoleClient.SUPERADMIN.equals(roleId))){
				//如果当前用户的roleid属于库管理员  则没有绑定/解除超级管理员和库管理员的权限、抛错
				throw new LifeCircleException(HttpStatus.FORBIDDEN,LifeCircleErrorMessageMapper.CoverageAdminDenied.getCode()
						, LifeCircleErrorMessageMapper.CoverageAdminDenied.getMessage());
			}
		}
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


	/**
	 * 通过用户id获取角色信息
	 * @param userId
	 * @return
	 */
	private List<RoleViewModel> getRoleList(String userId){
		List<RoleViewModel> result = new ArrayList<RoleViewModel>();
		JSONObject jsonObject = this.ucClient.listUserRoles(userId);
		if(jsonObject != null){
			JSONArray jsonArray = jsonObject.getJSONArray("items");
			if(jsonArray != null && jsonArray.size() > 0){
				Integer size = jsonArray.size();
				for(int i = 0; i < size; i++){
					RoleViewModel tmp = new RoleViewModel();
					tmp.setRoleId(jsonArray.getJSONObject(i).getString("role_id"));
					tmp.setRoleName(jsonArray.getJSONObject(i).getString("role_name"));
					result.add(tmp);
				}
			}
			return result;
		}else {
			return result;
		}
	}


}
