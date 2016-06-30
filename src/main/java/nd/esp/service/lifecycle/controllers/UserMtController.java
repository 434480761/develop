package nd.esp.service.lifecycle.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.models.ThirdPartyBsysModel;
import nd.esp.service.lifecycle.services.thirdpartybsys.v06.ThirdPartyBsysService;
import nd.esp.service.lifecycle.support.uc.UcClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping({"/v0.6/mt/users"})
public class UserMtController {

	private final Logger LOG = LoggerFactory.getLogger(UserMtController.class);

	@Autowired
	@Qualifier("ThirdPartyBsysServiceImpl")
	private ThirdPartyBsysService thirdPartyBsysService;

	@Autowired
	private UcClient ucClient;

	/** 超级管理员*/
	private static String SUPERADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_super_admin");
	

	/**
	 * 绑定用户角色(超级管理员)
	 * @return
	 * @author lianggz
	 */
	@RequestMapping(value="/roles/admin",method = RequestMethod.POST)
	public Map<String, Object> addUserRole(){

	    List<ThirdPartyBsysModel> thirdPartyBsysList = this.thirdPartyBsysService.findThirdPartyBsysList();

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("items", thirdPartyBsysList);
		return 	params;
	}

}
