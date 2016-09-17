package nd.esp.service.lifecycle.controller.v06;

import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.junit.Assert;
import org.junit.Test;

public class TestLessonController extends SimpleJunitTest4ResourceImpl {
	private final static String RES_TYPE = "lessons";
	@Test
	public void testAll(){
		String uuid;
		
		//创建资源
		ResourceViewModel rvm = testCreate(RES_TYPE,null,null);
		Assert.assertNotNull("测试创建资源不通过",rvm);
		uuid = rvm.getIdentifier();
		
		//校验修改接口
		rvm.setTitle("update-test");
		rvm = testUpdate(RES_TYPE,uuid,toJson(rvm));
		Assert.assertNotNull(rvm);
		Assert.assertEquals("测试修改接口不通过", "update-test", rvm.getTitle());
		
		//校验获取详细接口
		ResourceViewModel rvm2 = testGetDetail(RES_TYPE,uuid,"LC",false);
		Assert.assertNotNull(rvm2);
		Assert.assertNotNull(rvm2.getLifeCycle());
		Assert.assertEquals("测试获取详细接口不通过",uuid,rvm2.getIdentifier()); 
		
		//校验删除接口
		String s = testDelete(RES_TYPE,uuid);
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnMap.get("process_code").toString());
		

	}
	
}
