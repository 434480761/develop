package nd.esp.service.lifecycle.controller.v06;

import nd.esp.service.lifecycle.BaseControllerConfig;

import org.junit.Assert;
import org.junit.Test;

import com.nd.gaea.rest.testconfig.MockUtil;
/**
 * 应用服务器的运行状态单元测试
 * @author xuzy
 *
 */
public class TestMonitorController extends BaseControllerConfig {
	@Test
	public void testAll(){
		String uri = "/v0.3/system/status";
		String resStr = null;
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("get error", e);
		}
		Assert.assertNotNull("应用服务器的运行状态校验失败", resStr);
		Assert.assertTrue("应用服务器的运行状态校验失败", resStr.contains("status"));
	}
}
