package nd.esp.service.lifecycle.controller.v06;



import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.coursewares.v06.CoursewareViewModel;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestTeachingactivitiesController extends SimpleJunitTest4ResourceImpl {
	private final static String RES_TYPE = "teachingactivities";
//	@Test
	public void testAll(){
		String uuid;
		//校验上传接口，返回uuid
		AccessModel am = testUpload(RES_TYPE,"none",null,null);
		Assert.assertNotNull("测试上传接口不通过", am.getUuid());
		uuid = am.getUuid().toString();
		
		//创建资源不带techInfo属性
		testCreateNoneTechInfo();
		
		//创建资源
		ResourceViewModel rvm = testCreate(RES_TYPE,am.getUuid().toString(),null);
		Assert.assertEquals("测试创建资源不通过", uuid, rvm.getIdentifier());
		
		//修改资源不带techInfo属性
		testUpdateNoneTechInfo(uuid);
		
		//校验需要转码的资源(创建接口)
		String ss = testCreateNeedTransCode(RES_TYPE,UUID.randomUUID().toString(),null,"TRANSCODE_WAITING");
		Assert.assertTrue("校验需要转码的资源(创建接口)不通过",ss.contains("title"));
		Map<String,Object> mm = ObjectUtils.fromJson(ss, Map.class);
		if(mm.get("identifier") != null){
			del(RES_TYPE, mm.get("identifier").toString());
		}
		
		//校验需要转码的资源(修改接口)
		String ss2 = testUpdateNeedTransCode(RES_TYPE,uuid,null,"TRANSCODE_WAITING");
		Assert.assertTrue("校验需要转码的资源(修改接口)不通过",ss2.contains("title"));
		
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
		Assert.assertEquals("测试删除接口不通过", "LC/DELETE_RESOURCE_SUCCESS", returnMap.get("process_code").toString());
		
	}
	
	/**
	 * 素材不传tech_info
	 */
	private void testCreateNoneTechInfo(){
		String target = getDefaultResouceViewModelJson();
		Map<String,Object> targetMap = ObjectUtils.fromJson(target, Map.class);
		targetMap.remove("tech_info");
		String s = postCreate(RES_TYPE, UUID.randomUUID().toString(), ObjectUtils.toJson(targetMap));
		Map<String,Object> m = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull("素材不传tech_info不通过", m);
		Assert.assertEquals("素材不传tech_info不通过", LifeCircleErrorMessageMapper.ChecTechInfoFail.getCode(), m.get("code"));
	}
	
	/**
	 * 修改素材不传tech_info
	 */
	private void testUpdateNoneTechInfo(String uuid){
		String target = getDefaultResouceViewModelJson();
		Map<String,Object> targetMap = ObjectUtils.fromJson(target, Map.class);
		targetMap.remove("tech_info");
		String s = putUpdate(RES_TYPE, uuid, ObjectUtils.toJson(targetMap));
		Map<String,Object> m = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull("素材不传tech_info不通过", m);
		Assert.assertEquals("素材不传tech_info不通过", LifeCircleErrorMessageMapper.ChecTechInfoFail.getCode(), m.get("code"));
	}
}
