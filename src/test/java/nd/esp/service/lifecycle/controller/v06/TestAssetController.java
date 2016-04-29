package nd.esp.service.lifecycle.controller.v06;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRelationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanUtils;

public class TestAssetController extends SimpleJunitTest4ResourceImpl {
    private final static String RES_TYPE = "assets";
	@Test
	public void testAll(){
		String uuid;
		//校验上传接口使用错误的资源类型
		testUploadErrorRES_TYPE();
		
		//校验上传接口使用错误的UUID
		testUploadErrorUuid();
		
		//校验上传接口，返回uuid
		AccessModel am = testUpload(RES_TYPE,"none",null,null);
		Assert.assertNotNull("测试上传接口不通过", am.getUuid());
		uuid = am.getUuid().toString();
		
		//校验不传tech_info参数
		testCreateNoneTechInfo();
		
		//创建资源
		ResourceViewModel rvm = testCreate(RES_TYPE,am.getUuid().toString(),null);
		Assert.assertEquals("测试创建资源不通过", uuid, rvm.getIdentifier());
		
		//使用重复id创建素材
		testCreateDuplicateId(rvm);
		
		//使用错误的维度数据创建素材
		testCreateErrorCategory();
		
		//不传creator值修改素材
		testUpdateNoneCreator(rvm);
		
		//使用UUID调用上传接口
		AccessModel am2  = testUpload(RES_TYPE,am.getUuid().toString(),null,null);
		Assert.assertEquals("测试上传接口不通过", uuid, am2.getUuid().toString());
		
		//检验正常的下载接口
		AccessModel am3  = testDownload(RES_TYPE,uuid);
		Assert.assertNotEquals("测试下载接口不通过",uuid,am3.getUuid());
		
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
		
		//修改素材不带tech_info
		testUpdateNoneTechInfo(uuid);
		
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
		
		//校验获取详细接口,UUID不规范
		String s1 = getDetail(RES_TYPE, "1321231", "LC", false);
		Map<String,Object> r1 = ObjectUtils.fromJson(s1, Map.class);
		Assert.assertNotNull(r1);
		Assert.assertEquals("测试获取详细接口,UUID不规范不通过", LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),r1.get("code"));
		
		//校验批量获取详细接口
		List<String> rids = new ArrayList<String>();
		rids.add(uuid);
		Map<String, ResourceViewModel> rMap = testBatchDetail(RES_TYPE,rids,"LC");
		Assert.assertNotNull(rMap);
		Assert.assertNotNull(rMap.get(uuid));
		Assert.assertNotNull("测试批量获取详细接口不通过",rMap.get(uuid).getLifeCycle());
		
		//校验批量获取详细接口,UUID不规范
		List<String> rids2 = new ArrayList<String>();
		rids2.add("111");
		String s2 = getBatchDetail(RES_TYPE, rids2, "LC");
		Map<String,Object> r2 = ObjectUtils.fromJson(s2, Map.class);
		Assert.assertNotNull(r2);
		Assert.assertEquals("测试批量获取详细接口,UUID不规范不通过",LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(),r2.get("code"));
		
		//不传life_cycle修改素材
		testUpdateNoneLifeCycle(rvm);
		
		//校验入参有关系
		String uuid2 = testCreateHasRelation(uuid);
		
		//使用特殊的维度数据创建资源
		String uuid3 = testCreateSpecialCategory("$RA0600");
		String uuid4 = testCreateSpecialCategory("$RA0700");
		
		//使用空的维度创建资源
		testCreateNoneCategory();
		
		//校验删除接口
		String s = testDelete(RES_TYPE,uuid);
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnMap.get("process_code").toString());
		
		testDelete(RES_TYPE,uuid2);
		testDelete(RES_TYPE,uuid3);
		testDelete(RES_TYPE,uuid4);
		
		//校验获取详细接口
		ResourceViewModel rvm3 = testGetDetail(RES_TYPE,uuid,"LC",true);
		Assert.assertNotNull(rvm3);
		Assert.assertNotNull(rvm3.getLifeCycle());
		Assert.assertEquals("测试获取详细接口不通过",uuid,rvm3.getIdentifier());
		Assert.assertEquals("测试获取详细接口不通过", false, rvm3.getLifeCycle().isEnable());
		
	}
	
	/**
	 * 带关系参数创建资源
	 */
	private String testCreateHasRelation(String sourceUuid){
		List<ResRelationViewModel> list = new ArrayList<ResRelationViewModel>();
		ResRelationViewModel r = new ResRelationViewModel();
		r.setSourceType(RES_TYPE);
		r.setSource(sourceUuid);
		r.setOrderNum(1);
		r.setRelationType("ASSOCIATE");
		r.setLabel("test");
		list.add(r);
		ResourceViewModel target = getDefaultResouceViewModel();
		target.setRelations(list);
		String uuid = UUID.randomUUID().toString();
		String re = postCreate(RES_TYPE, uuid, toJson(target));
		Map<String,Object> m = ObjectUtils.fromJson(re, Map.class);
		Assert.assertEquals("校验带关系参数创建资源不通过", uuid, m.get("identifier"));
		return uuid;
	}
	
	/**
	 * 使用错误的资源类型上传
	 */
	private void testUploadErrorRES_TYPE(){
		String returnStr = getUpload("res","none",null,null);
		Map<String,Object> m = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试上传接口错误资源类型不通过", LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(), m.get("code"));
	}
	
	/**
	 * 使用错误的uuid上传
	 */
	private void testUploadErrorUuid(){
		String returnStr = getUpload(RES_TYPE,"123",null,null);
		Map<String,Object> m = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试上传接口错误资源类型不通过", LifeCircleErrorMessageMapper.CheckIdentifierFail.getCode(), m.get("code"));
	}
	
	/**
	 * 测试重复id
	 */
	private void testCreateDuplicateId(ResourceViewModel rvm) {
		ResourceViewModel target = new ResourceViewModel();
		BeanUtils.copyProperties(rvm, target);
		String resStr = postCreate(RES_TYPE,target.getIdentifier(),toJson(target));
		Map<String,Object> m = ObjectUtils.fromJson(resStr, Map.class);
		Assert.assertEquals("测试重复id不通过", LifeCircleErrorMessageMapper.CheckDuplicateIdFail.getCode(), m.get("code"));
	}

	/**
	 * 测试错误的nd_code
	 */
	private void testCreateErrorCategory() {
		ResourceViewModel target = getDefaultResouceViewModel();
		target.setIdentifier(UUID.randomUUID().toString());
		Map<String, List<? extends ResClassificationViewModel>> map = target.getCategories();
		List<? extends ResClassificationViewModel> list = map.get("phase");
		ResClassificationViewModel c = list.get(0);
		c.setTaxonpath("K12/error/$ON020500/$SB0300/$E005000/$E00500");
		String resStr = postCreate(RES_TYPE,target.getIdentifier(),toJson(target));
		Map<String,Object> m = ObjectUtils.fromJson(resStr, Map.class);
		Assert.assertEquals("测试错误的nd_code不通过", "维度数据有错误", m.get("message"));
	}
	
	/**
	 * 素材不传tech_info
	 */
	private void testCreateNoneTechInfo(){
		String target = getDefaultResouceViewModelJson();
		Map<String,Object> targetMap = ObjectUtils.fromJson(target, Map.class);
		targetMap.remove("tech_info");
		String s = postCreate(RES_TYPE, UUID.randomUUID().toString(), toJson(targetMap));
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
		String s = putUpdate(RES_TYPE, uuid, toJson(targetMap));
		Map<String,Object> m = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull("素材不传tech_info不通过", m);
		Assert.assertEquals("素材不传tech_info不通过", LifeCircleErrorMessageMapper.ChecTechInfoFail.getCode(), m.get("code"));
	}
	
	/**
	 * 创建素材使用$RA06|07开头的维度数据
	 * @return
	 */
	private String testCreateSpecialCategory(String ndCode){
		String uuid = UUID.randomUUID().toString();
		ResourceViewModel target = getDefaultResouceViewModel();
		ResClassificationViewModel rc = getDefaultCategory();
		rc.setTaxoncode(ndCode);
		rc.setTaxonpath(null);
		
		List<ResClassificationViewModel> list = new ArrayList<ResClassificationViewModel>();
		list.add(rc);
		target.getCategories().put("asset_type", list);
		
		String re = postCreate(RES_TYPE, uuid, toJson(target));
		Map<String,Object> m = ObjectUtils.fromJson(re, Map.class);
		Assert.assertNotNull("创建素材使用$RA06|07开头的维度数据不通过", m);
		Assert.assertNotNull("创建素材使用$RA06|07开头的维度数据不通过", m.get("identifier"));
		return m.get("identifier").toString();
	}
	
	/**
	 * 创建素材使用空的维度数据
	 * @return
	 */
	private void testCreateNoneCategory(){
		String uuid = UUID.randomUUID().toString();
		ResourceViewModel target = getDefaultResouceViewModel();
		target.setCategories(null);
		String re = postCreate(RES_TYPE, uuid, toJson(target));
		Map<String,Object> m = ObjectUtils.fromJson(re, Map.class);
		Assert.assertNotNull("创建素材使用空的维度数据不通过", m);
		Assert.assertEquals("创建素材使用空的维度数据不通过", "LC/CREATE_ASSET_PARAM_VALID_FAIL", m.get("code"));
	}
	
	/**
	 * 不传lifeCycle
	 */
	private void testUpdateNoneLifeCycle(ResourceViewModel rvm) {
		String target = getDefaultResouceViewModelJson();
		Map<String,Object> targetMap = ObjectUtils.fromJson(target, Map.class);
		targetMap.remove("life_cycle");
		targetMap.put("identifier", rvm.getIdentifier());
		String resStr = putUpdate(RES_TYPE, rvm.getIdentifier(), toJson(targetMap));
		ResourceViewModel r = fromJson(resStr, ResourceViewModel.class);
		Assert.assertNotNull("不传lifeCycle不通过",r);
		Assert.assertNotNull("不传lifeCycle不通过",r.getLifeCycle());
		Assert.assertEquals("不传lifeCycle不通过","{USER}",r.getLifeCycle().getCreator());
	}
	
	/**
	 * 修改素材不传creator
	 */
	private void testUpdateNoneCreator(ResourceViewModel rvm) {
		ResourceViewModel target = getDefaultResouceViewModel();
		target.setIdentifier(rvm.getIdentifier());
		target.getLifeCycle().setCreator(null);
		String resStr = putUpdate(RES_TYPE, target.getIdentifier(), toJson(target));
		ResourceViewModel r = fromJson(resStr, ResourceViewModel.class);
		Assert.assertNotNull("修改素材不传creator不通过",r);
		Assert.assertEquals("修改素材不传creator不通过", "lcms-special-creator-dev-test", r.getLifeCycle().getCreator());
	}
   
}