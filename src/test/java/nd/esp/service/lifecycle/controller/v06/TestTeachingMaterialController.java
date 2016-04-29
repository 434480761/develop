package nd.esp.service.lifecycle.controller.v06;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.junit.Assert;
import org.junit.Test;

public class TestTeachingMaterialController extends SimpleJunitTest4ResourceImpl {
	private final static String RES_TYPE = "teachingmaterials";
//	@Test
	public void testAll(){
		String uuid;
		
		//创建资源
		ResourceViewModel rvm = testCreate(RES_TYPE,null,null);
		Assert.assertNotNull("测试创建资源不通过",rvm);
		uuid = rvm.getIdentifier();
		
		//创建资源（资源类型不对）
		String str = postCreate("tc",null,null);
		Assert.assertTrue("测试创建资源不通过", str.contains("类型不对")); 
		
		//修改资源（资源类型不对）
		String str2 = putUpdate("tc",uuid,toJson(rvm));
		Assert.assertTrue("测试修改资源不通过", str2.contains("类型不对")); 
		
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
		
		//创建状态为ONLINE的教材
		ResourceViewModel r = getDefaultResouceViewModel();
		r.getLifeCycle().setStatus("ONLINE");
		String returnStr = postCreate(RES_TYPE,null,toJson(r));
		Map<String,Object> map2 = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertNotNull("测试创建资源不通过", map2.get("identifier"));
		String uuid2 = map2.get("identifier").toString();
		
		//再创建一本相同的教材
		String returnStr2 = postCreate(RES_TYPE,null,toJson(r));
		Map<String,Object> map3 = ObjectUtils.fromJson(returnStr2, Map.class);
		Assert.assertNotNull("测试创建资源不通过", map3.get("code"));
		Assert.assertEquals("测试创建资源不通过", LifeCircleErrorMessageMapper.SameTeachingMaterialFail.getCode(),map3.get("code"));
		
		//创建教材(不传taxonpath)
		ResourceViewModel r2 = getDefaultResouceViewModel();
		ResClassificationViewModel rcvm = getDefaultCategory();
		rcvm.setTaxonpath(null);
		List<ResClassificationViewModel> l = new ArrayList<ResClassificationViewModel>();
		l.add(rcvm);
		Map<String, List<? extends ResClassificationViewModel>> m = new HashMap<String, List<? extends ResClassificationViewModel>>();
		m.put("phase", l);
		r2.setCategories(m);
		String returnStr3 = postCreate(RES_TYPE,null,toJson(r2));
		Map<String,Object> map4 = ObjectUtils.fromJson(returnStr3, Map.class);
		Assert.assertNotNull("测试创建资源不通过", map4.get("code"));
		Assert.assertEquals("测试创建资源不通过", LifeCircleErrorMessageMapper.CheckTaxonpathFail.getCode(),map4.get("code"));
		
		//删除资源
		testDelete(RES_TYPE,uuid2);
		
		//再创建一本相同的教材(原教材被删除过)
		String returnStr5 = postCreate(RES_TYPE,null,toJson(r));
		Map<String,Object> map5 = ObjectUtils.fromJson(returnStr5, Map.class);
		Assert.assertNotNull("测试创建资源不通过", map5.get("code"));
		Assert.assertEquals("测试创建资源不通过", LifeCircleErrorMessageMapper.TeachingMaterialDisable.getCode(),map5.get("code"));
		
		//校验删除接口
		String s = testDelete(RES_TYPE,uuid);
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnMap.get("process_code").toString());
		

	}
	
}
