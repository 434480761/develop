package nd.esp.service.lifecycle.controller.v06;

import nd.esp.service.lifecycle.BaseControllerConfig;

import org.junit.Assert;
import org.junit.Test;

import com.nd.gaea.rest.testconfig.MockUtil;
/**
 * 工具类controller层单元测试类
 * @author xuzy
 *
 */
public class TestToolController extends BaseControllerConfig{
	/**
	 * 测试章节复用关系接口
	 */
	@Test
	public void testDealData(){
		//1、创建章节1
		//2、创建素材1
		//3、创建章节1与素材1的关系
		//4、创建章节2
		//5、调用章节复用关系接口(错误的章节)
		//6、调用复用关系接口（错误的资源类型）
		//7、调用章节复用关系接口(使用章节2)
	}
	
	/**
	 * 解析日期
	 */
	@Test
	public void testParseDate(){
		String uri = "/v0.6/resources/tools/parseDate?value=2016-01-01";
		String uri2 = "/v0.6/resources/tools/parseDate?value=2016-01-01&format=yyyy-MM-dd";
		String resStr = null;
		String resStr2 = null;
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
			resStr2 = MockUtil.mockGet(mockMvc, uri2.toString(), null);
		} catch (Exception e) {
			logger.error("getUpload error", e);
		}
		Assert.assertNotNull("解析日期出错！", resStr);
		Assert.assertNotNull("解析日期出错！", resStr2);
	}
	
	/**
	 * 格式化日期
	 */
	@Test
	public void testFormatDate(){
		String uri = "/v0.6/resources/tools/formatDate?value=1454774400000";
		String uri2 = "/v0.6/resources/tools/formatDate?value=1454774400000&format=yyyy-MM-dd";
		String resStr = null;
		String resStr2 = null;
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
			resStr2 = MockUtil.mockGet(mockMvc, uri2.toString(), null);
		} catch (Exception e) {
			logger.error("getUpload error", e);
		}
		Assert.assertNotNull("格式化日期出错！", resStr);
		Assert.assertNotNull("格式化日期出错！", resStr2);
	}
}
