package nd.esp.service.lifecycle.controller.v06;

import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.impl.CopyrightOwnerTestImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.copyright.v06.CopyrightOwnerViewModel;
import org.junit.Assert;
import org.junit.Test;
public class TestCopyrightOwnerController extends CopyrightOwnerTestImpl{
	
	//几个变量使用的默认值，方便清理数据
		public final static String DERAULT_COPYRIGHT_TITLE="lcms-special-title-dev-test";
		public final static String DERAULT_COPYRIGHT_DESCRIPTION="lcms-special-description-dev-test";

	@SuppressWarnings("unchecked")
	@Test
	public void testCopyrightAll(){
		
		CopyrightOwnerViewModel covm=new CopyrightOwnerViewModel();
		covm.setTitle(DERAULT_COPYRIGHT_TITLE);
		covm.setDescription(DERAULT_COPYRIGHT_DESCRIPTION);
		
		//创建ND资源版权方接口
		CopyrightOwnerViewModel createOVM = testCreate(covm);
		Assert.assertEquals("测试创建资源版权方不通过", DERAULT_COPYRIGHT_TITLE, createOVM.getTitle());
		
		//创建时测试title是否重复
		String returnStr= postCreate(createOVM);
		Map<String,Object> m = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试版权方名称已经存在不通过", LifeCircleErrorMessageMapper.CheckDuplicateCopyrightOwnerTitleFail.getCode(),m.get("code"));
		
		//修改ND资源版权方接口
		createOVM.setTitle("update-test");
		CopyrightOwnerViewModel updateM = testUpdate(createOVM);
		Assert.assertNotNull(updateM);
		Assert.assertEquals("测试修改资源版权方不通过", "update-test", updateM.getTitle());
		
	    //修改时测试版权方是否存在
		updateM.setIdentifier(UUID.randomUUID().toString());
		updateM.setTitle("test");
		String updateStr= putUpdate(updateM);
		Map<String,Object> updateMap = ObjectUtils.fromJson(updateStr, Map.class);
		Assert.assertEquals("测试修改时版权方是否存在不通过", LifeCircleErrorMessageMapper.CopyrightOwnerNotFound.getCode(),updateMap.get("code"));
		
		//修改时测试title是否重复
		CopyrightOwnerViewModel rpm=new CopyrightOwnerViewModel();
		rpm.setTitle("11111");
		rpm.setDescription(DERAULT_COPYRIGHT_DESCRIPTION);
		CopyrightOwnerViewModel rpmResult=testCreate(rpm);//先创建，后修改，然后测试
		rpmResult.setTitle("update-test");
		String updateTitle=putUpdate(rpmResult);
		Map<String,Object> updateResult=ObjectUtils.fromJson(updateTitle, Map.class);
		Assert.assertEquals("测试修改时title已经存在不通过", LifeCircleErrorMessageMapper.CheckDuplicateCopyrightOwnerTitleFail.getCode(),updateResult.get("code"));
		//删除创建的记录
		testDelete(rpmResult.getIdentifier());
		
		//测试查询资源版权方
		String limit="(0,20)";
		ListViewModel<CopyrightOwnerViewModel> getM=testGetDetail("update-test",limit);
		Assert.assertNotNull(getM);
		Assert.assertEquals("测试查询资源版权方不通过", limit, getM.getLimit());
		
		//测试删除时资源版权方找不到
		String str=testDelete(UUID.randomUUID().toString());
		Map<String,Object> Str= ObjectUtils.fromJson(str, Map.class);
		Assert.assertNotNull(Str);
		Assert.assertEquals("测试资源版权方找不到不通过", LifeCircleErrorMessageMapper.CopyrightOwnerNotFound.getCode(),Str.get("code"));
		
		//删除ND资源提供商接口
		String s=testDelete(createOVM.getIdentifier());
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除资源版权方接口不通过", LifeCircleErrorMessageMapper.DeleteCopyrightOwnerSuccess.getCode(), returnMap.get("process_code").toString());
	}
	
}
