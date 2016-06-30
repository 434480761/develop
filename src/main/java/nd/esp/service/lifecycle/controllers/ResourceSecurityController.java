package nd.esp.service.lifecycle.controllers;

import com.alibaba.fastjson.JSONObject;
import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;
import nd.esp.service.lifecycle.models.UserRestypeMappingModel;
import nd.esp.service.lifecycle.services.userrestypemapping.v06.UserRestypeMappingService;
import nd.esp.service.lifecycle.support.uc.UcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

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
@RequestMapping({"/v0.6/test"})
public class ResourceSecurityController {

	private final Logger LOG = LoggerFactory.getLogger(ResourceSecurityController.class);

	@Autowired
	@Qualifier("UserRestypeMappingServiceImpl")
	private UserRestypeMappingService userRestypeMappingService;

	@Autowired
	private UcClient ucClient;

	/**
	 * @desc 创建UserRestypeMapping接口
	 */
	@RequestMapping(value="/test",method = RequestMethod.GET)
	public int addUserRestypeMapping(){
		UserRestypeMappingModel userRestypeMappingModel = new UserRestypeMappingModel();
		userRestypeMappingModel.setCreateTime(new Timestamp(System.currentTimeMillis()));
		userRestypeMappingModel.setResType("assets");
		userRestypeMappingModel.setUserId("144231");
		return 	this.userRestypeMappingService.addUserRestypeMapping(userRestypeMappingModel);
	}

	@RequestMapping(value="/{id:\\d+}/test",method = RequestMethod.GET)
	public UserRestypeMappingModel addUserRestypeMapping(@PathVariable Integer id){
		return this.userRestypeMappingService.checkUserRestypeMappingInfo(id);
	}

	@RequestMapping(value="/role",method = RequestMethod.POST)
	public JSONObject addRole(@RequestBody JSONObject jsonObject){
		String roleName = jsonObject.getString("role_name");
		String remarks = jsonObject.getString("remarks");
		return this.ucClient.addRole(roleName, remarks);
	}

	@RequestMapping(value="/role",method = RequestMethod.GET)
	public JSONObject getRoleList(){
		return this.ucClient.listRealmRoles();
	}

	@RequestMapping(value="/role/{userId}",method = RequestMethod.GET)
	public JSONObject getRoleList(@PathVariable String userId){
		return this.ucClient.listUserRoles(userId);
	}

	@RequestMapping(value="/role",method = RequestMethod.PUT)
	public JSONObject addRoles(@RequestBody JSONObject jsonObject, @AuthenticationPrincipal UserInfo userInfo){

//		System.out.println((UserCenterRoleDetails)userInfo.getUserRoles().get(0).);
		for(UserCenterRoleDetails userCenterRoleDetails: userInfo.getUserRoles()){
			System.out.println(userCenterRoleDetails.getRoleName());
		}

		String userId = jsonObject.getString("user_id");
		String roleId = jsonObject.getString("role_id");
		return this.ucClient.addUserRole(userId, Integer.valueOf(roleId));
	}

	@RequestMapping(value="/role",method = RequestMethod.DELETE)
	public JSONObject delRoles(@RequestBody JSONObject jsonObject){
		String userId = jsonObject.getString("user_id");
		String roleId = jsonObject.getString("role_id");
		return this.ucClient.deleteUserRole(userId, Integer.valueOf(roleId));
	}
}
