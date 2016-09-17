package nd.esp.service.lifecycle.controllers;

import com.nd.gaea.rest.security.authens.UserInfo;
import nd.esp.service.lifecycle.services.securitykey.v06.SecurityKeyService;
import nd.esp.service.lifecycle.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** 
 * <p>Title: SecurityKeyController  </p>
 * <p>Description: SecurityKeyController </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年7月21日           </p>
 * @author lianggz
 */
@RestController
@RequestMapping({"/v0.6/security"})
public class SecurityKeyController {

	@Autowired
	@Qualifier("SecurityKeyServiceImpl")
	private SecurityKeyService securityKeyService;

	/**
	 * 根据用户id和客户端的公钥获取服务端生成的密钥
	 * @param userId
	 * @param key
	 * @return
	 * @author lianggz
	 * @throws Exception 
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public Map<String, Object> getSecurityKey(
	        @RequestParam(required=true,value="user_id") String userId,
	        @RequestParam(required=true,value="key") String key ,@AuthenticationPrincipal UserInfo userInfo) throws Exception {
		
		// 有效性检验
		AssertUtils.isEmpty(userId, "user_id");
		AssertUtils.isLong(userId, "user_id");
		// 查询密钥
		String securityKey = this.securityKeyService.getRsaEncryptDesKey(userId, key);
		// 返回参数
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("msg", securityKey);
		return params;
	}

}
