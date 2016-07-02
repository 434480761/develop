package nd.esp.service.lifecycle.controller.v06;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nd.gaea.rest.testconfig.MockUtil;
import nd.esp.service.lifecycle.support.uc.UcRoleClient;
import nd.esp.service.lifecycle.utils.common.CusBaseControllerConfig;
import nd.esp.service.lifecycle.utils.common.TestException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Title: TestUserRoleController </p>
 * <p>Description: TestUserRoleController </p>
 * <p>Copyright: Copyright (c) 2016  </p>
 * <p>Company:ND Co., Ltd.  </p>
 * <p>Create Time: 2016/7/2 </p>
 *
 * @author lanyl
 */
public class TestUserRoleController extends CusBaseControllerConfig {

	private static Logger logger = LoggerFactory.getLogger(TestUserRoleController.class);
	private static final String TEST_BASE_URL_USER_ROLE = "/v0.6/users/";
	private static final String tmpUserId = "264312";


	/**
	 * 用例：绑定用户角色[POST] /v0.6/users/{userId}/roles
	 * @author lanyl
	 * @throws Exception
	 */
	@Test
	public void addUserRoleTest()  throws Exception{
		String url = TEST_BASE_URL_USER_ROLE + "{userId}/roles" ;
		logger.info("==================");
		logger.info("name:{}, url:{}", new Object[] {"绑定用户角色", url});
		logger.info("=== start ===");

		//例1：userId 为非法字符，其余参数正常 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE + "x/roles";
		JSONObject param = new JSONObject();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		String json = param.toJSONString();
		String result = MockUtil.mockPost(mockMvc, url, json);
		if(StringUtils.isNotEmpty(result)) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例2：userId 为-1，其余参数正常 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +"-1/roles";
		param.clear();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockPost(mockMvc, url, json);
		if(StringUtils.isNotEmpty(result)){
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例3：userId 有效的用户, role_id 为非法参数 其余参数正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", "xxx");
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockPost(mockMvc, url, json);
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例4：userId 有效的用户, role_id 无效的角色id 其余参数正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", "00000");
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockPost(mockMvc, url, json);
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例5：userId 有效的用户, role_id 有效的角色id res_type 无效的请求参数 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE  +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","xxxxx"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockPost(mockMvc, url, json);
		if("LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		}else {
			throw new TestException();
		}

		//例6：userId 有效的用户, role_id 有效的角色id  res_type 有效的请求参数 ，其余参数正常 【期望结果：pass】
		url = TEST_BASE_URL_USER_ROLE  +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockPost(mockMvc, url, json);
		if(UcRoleClient.SUPERADMIN.equals(JSON.parseObject(result).getString("role_id"))){
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
		}else {
			throw new TestException();
		}
		logger.info("=== END ===");
	}

	/**
	 * 用例：解除绑定用户角色[delete] /v0.6/users/{userId}/roles
	 * @author lanyl
	 * @throws Exception
	 */
	@Test
	public void deleteUserRoleTest()  throws Exception{
		String url = TEST_BASE_URL_USER_ROLE + "{userId}/roles" ;
		logger.info("==================");
		logger.info("name:{}, url:{}", new Object[] {"解除绑定用户角色", url});
		logger.info("=== start ===");

		//例1：userId 为非法字符 ,其他参数正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE + "x/roles";
		JSONObject param = new JSONObject();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		String json = param.toJSONString();
		String result = MockUtil.mockDelete(mockMvc, url, json);
		if(StringUtils.isNotEmpty(result)) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例2：userId 为-1,其他参数正常 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +"-1/roles";
		param.clear();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockDelete(mockMvc, url, json);
		if(StringUtils.isNotEmpty(result)){
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例3：userId 有效的用户, role_id 为非法参数 其余参数正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +this.tmpUserId + "/roles";
		param.clear();
		param = new JSONObject();
		param.put("role_id", "xxx");
		json = param.toJSONString();
		result = MockUtil.mockDelete(mockMvc, url, json);
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例4：userId 有效的用户, role_id 无效的角色id 其余参数正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", "00000");
		json = param.toJSONString();
		result = MockUtil.mockDelete(mockMvc, url, json);
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例5：userId 有效的用户, role_id 有效的角色id res_type 无效的请求参数 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE  +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","xxxxx"});
		json = param.toJSONString();
		result = MockUtil.mockDelete(mockMvc, url, json);
		if("LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		}else {
			throw new TestException();
		}

		//例6：userId 有效的用户, role_id 有效的角色id  res_type 有效的请求参数 ，其余参数正常 【期望结果：pass】
		url = TEST_BASE_URL_USER_ROLE  +this.tmpUserId + "/roles";
		param.clear();
		param.put("role_id", UcRoleClient.SUPERADMIN);
		param.put("res_types", new String[]{"assets","knowledges"});
		param.put("coverages", new String[]{"org/nd","org/481036342254"});
		json = param.toJSONString();
		result = MockUtil.mockDelete(mockMvc, url, json);
		if(UcRoleClient.SUPERADMIN.equals(JSON.parseObject(result).getString("role_id"))){
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
		}else {
			throw new TestException();
		}
		logger.info("=== END ===");
	}


	/**
	 * 用例：查询角色用户列表[get] /v0.6/users/roles/{roleId}/list
	 * @author lanyl
	 * @throws Exception
	 */
	@Test
	public void getUserRoleListTest()  throws Exception{
		//limit参数必传 org_id选传
		String url = TEST_BASE_URL_USER_ROLE + "roles/{roleId}/list?limit=(0,20)&org_id=XX" ;
		logger.info("==================");
		logger.info("name:{}, url:{}", new Object[] {"查询角色用户列表", url});
		logger.info("=== start ===");

		//例1：roleId 为非法字符 ,limit正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE + "roles/x/list?limit=(0,20)";
		String result = MockUtil.mockGet(mockMvc, url, "{}");
		if(StringUtils.isNotEmpty(result)) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例2：roleId 无效的角色,limit正常 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +"roles/0000/list?limit=(0,20)";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例3：roleId 有效的角色,limit为空 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +"roles/"+UcRoleClient.SUPERADMIN+"/list";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if(!"WAF/REQUIRE_ARGUMENT".equals(JSON.parseObject(result).getString("code"))) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例4：userId 有效的角色,limit正常 【期望结果：pass】
		url = TEST_BASE_URL_USER_ROLE  +"roles/"+UcRoleClient.SUPERADMIN+"/list?limit=(0,20)";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if("(0,20)".equals(JSON.parseObject(result).getString("limit"))){
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
		}else {
			throw new TestException();
		}

		//例5：userId 有效的角色,limit正常,org_id 为非法参数【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +"roles/"+UcRoleClient.SUPERADMIN+"/list?limit=(0,20)&org_id=xxx";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if("UC/ERROR_ARGUMENT".equals(JSON.parseObject(result).getString("code"))) {
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		} else {
			throw new TestException();
		}

		//例6：userId 有效的角色,limit非法参数,org_id 正常【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE +"roles/"+UcRoleClient.SUPERADMIN+"/list?limit=xxx&org_id=481036345765";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if("LC/LIMIT_PARAM_ILLEGAL".equals(JSON.parseObject(result).getString("code"))) {
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		} else {
			throw new TestException();
		}

		//例7：userId 有效的角色,limit 合法参数,org_id 正常【期望结果：pass】
		url = TEST_BASE_URL_USER_ROLE +"roles/"+UcRoleClient.SUPERADMIN+"/list?limit=(0,20)&org_id=481036345765";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if("(0,20)".equals(JSON.parseObject(result).getString("limit"))) {
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});
		} else {
			throw new TestException();
		}
		logger.info("=== END ===");
	}



	/**
	 * 用例：查询角色用户[get] /v0.6/users/{userId}/roles/{roleId}
	 * @author lanyl
	 * @throws Exception
	 */
	@Test
	public void getUserRoleTest()  throws Exception{
		String url = TEST_BASE_URL_USER_ROLE + "{userId}/roles/{roleId}" ;
		logger.info("==================");
		logger.info("name:{}, url:{}", new Object[] {"查询角色用户", url});
		logger.info("=== start ===");

		//例1：userId 为非法字符，roleId 正常值 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE + "x/roles/"+ UcRoleClient.RESCONSUMER;
		String result = MockUtil.mockGet(mockMvc, url, "{}");
		if(StringUtils.isNotEmpty(result)) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例2：userId 为正常值，roleId 非法字符 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE + this.userId +"/roles/x";
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if(StringUtils.isNotEmpty(result)) {
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例3：userId 有效的用户，roleId 无效的角色 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE + this.userId +"/roles/00000" ;
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if(!"LC/INVALIDARGUMENTS_ERROR".equals(JSON.parseObject(result).getString("code"))){
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例4：userId 无效的用户，roleId 有效的角色 【期望结果：error】
		url = TEST_BASE_URL_USER_ROLE  +"00000/roles/" + UcRoleClient.SUPERADMIN ;
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if(!"UC/ACCOUNT_NOT_EXIST".equals(JSON.parseObject(result).getString("code"))){
			throw new TestException();
		}
		logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "error", result, "{}"});

		//例5：userId 有效的用户，roleId 有效的角色 【期望结果：pass】
		url = TEST_BASE_URL_USER_ROLE + this.userId + "/roles/"+UcRoleClient.SUPERADMIN ;
		result = MockUtil.mockGet(mockMvc, url, "{}");
		if(UcRoleClient.SUPERADMIN.equals(JSON.parseObject(result).getString("role_id"))) {
			logger.info("url:{}, status:{}, response:{}, remark:{}", new Object[] {url, "pass", result, "{}"});
		} else {
			throw new TestException();
		}
		logger.info("=== END ===");
	}


}
