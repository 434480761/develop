package nd.esp.service.lifecycle.controller.v06;

import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.impl.ResourceProviderTestImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.provider.v06.ResourceProviderViewModel;

import org.junit.Assert;
import org.junit.Test;
public class TestResourceProviderController extends ResourceProviderTestImpl{
	
	//几个变量使用的默认值，方便清理数据
		public final static String DERAULT_PROVIDER_TITLE="lcms-special-title-dev-test";
		public final static String DERAULT_PROVIDER_DESCRIPTION="lcms-special-description-dev-test";

	@SuppressWarnings("unchecked")
	@Test
	public void testAll(){
		
		ResourceProviderViewModel rpvm=new ResourceProviderViewModel();
		rpvm.setTitle(DERAULT_PROVIDER_TITLE);
		rpvm.setDescription(DERAULT_PROVIDER_DESCRIPTION);
		
		//创建ND资源提供商接口
		ResourceProviderViewModel createM = testCreate(rpvm);
		Assert.assertEquals("测试创建资源提供商不通过", DERAULT_PROVIDER_TITLE, createM.getTitle());
		
		//创建时测试title是否重复
		String returnStr= postCreate(createM);
		Map<String,Object> m = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试提供商名称已经存在不通过", LifeCircleErrorMessageMapper.CheckDuplicateProviderTitleFail.getCode(),m.get("code"));
		
		//修改ND资源提供商接口
		createM.setTitle("update-test");
		ResourceProviderViewModel updateM = testUpdate(createM);
		Assert.assertNotNull(updateM);
		Assert.assertEquals("测试修改资源提供商不通过", "update-test", updateM.getTitle());
		
	    //修改时测试提供商是否存在
		updateM.setIdentifier(UUID.randomUUID().toString());
		updateM.setTitle("test");
		String updateStr= putUpdate(updateM);
		Map<String,Object> updateMap = ObjectUtils.fromJson(updateStr, Map.class);
		Assert.assertEquals("测试修改时提供商是否存在不通过", LifeCircleErrorMessageMapper.ResourceProviderNotFound.getCode(),updateMap.get("code"));
		
		//修改时测试title是否重复
		ResourceProviderViewModel rpm=new ResourceProviderViewModel();
		rpm.setTitle("11111");
		rpm.setDescription(DERAULT_PROVIDER_DESCRIPTION);
		ResourceProviderViewModel rpmResult=testCreate(rpm);//先创建，后修改，然后测试
		rpmResult.setTitle("update-test");
		String updateTitle=putUpdate(rpmResult);
		Map<String,Object> updateResult=ObjectUtils.fromJson(updateTitle, Map.class);
		Assert.assertEquals("测试修改时title已经存在不通过", LifeCircleErrorMessageMapper.CheckDuplicateProviderTitleFail.getCode(),updateResult.get("code"));
		//删除创建的记录
		testDelete(rpmResult.getIdentifier());
		
		//测试查询资源提供商
		String limit="(0,20)";
		ListViewModel<ResourceProviderViewModel> getM=testGetDetail("update-test",limit);
		Assert.assertNotNull(getM);
		Assert.assertEquals("测试查询资源提供商不通过", limit, getM.getLimit());
		
		//测试删除时资源提供商找不到
		String str=testDelete(UUID.randomUUID().toString());
		Map<String,Object> Str= ObjectUtils.fromJson(str, Map.class);
		Assert.assertNotNull(Str);
		Assert.assertEquals("测试资源提供商找不到不通过", LifeCircleErrorMessageMapper.ResourceProviderNotFound.getCode(),Str.get("code"));
		
		//删除ND资源提供商接口
		String s=testDelete(createM.getIdentifier());
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除资源提供商接口不通过", LifeCircleErrorMessageMapper.DeleteProviderSuccess.getCode(), returnMap.get("process_code").toString());
	}
	
}
