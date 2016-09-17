package nd.esp.service.lifecycle.controllers;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.services.resourcesecuritykey.v06.ResourceSecurityKeyService;
import nd.esp.service.lifecycle.utils.AssertUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 
 * <p>Title: ResourceSecurityKeyController  </p>
 * <p>Description: ResourceSecurityKeyController </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月8日           </p>
 * @author lianggz
 */
@RestController
@RequestMapping({"/v0.6/resseck"})
public class ResourceSecurityKeyController {

	@Autowired
	@Qualifier("ResourceSecurityKeyServiceImpl")
	private ResourceSecurityKeyService resourceSecurityKeyService;

	/**
	 * 根据资源UUID和客户端的公钥获取服务端生成的密钥	     
	 * @param uuid
	 * @param key
	 * @return
	 * @author lianggz
	 * @throws Exception 
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> getResourceSecurityKey(
	        @RequestParam(required=true,value="uuid") String uuid,
	        @RequestParam(required=true,value="key") String key) throws Exception {
		
		// 有效性检验
		AssertUtils.isUuidPattern(uuid, "uuid");
		// 查询密钥
		String securityKey = this.resourceSecurityKeyService.findOrInsert(uuid, key);
		// 返回参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("msg", securityKey);
		return params;
	}

}
