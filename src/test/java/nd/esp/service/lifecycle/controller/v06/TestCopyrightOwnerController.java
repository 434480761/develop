package nd.esp.service.lifecycle.controller.v06;

import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.copyright.v06.CopyrightOwnerViewModel;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.nd.gaea.rest.testconfig.MockUtil;
public class TestCopyrightOwnerController extends SimpleJunitTest4ResourceImpl{
	
	   //几个变量使用的默认值，方便清理数据
	    public final static String DERAULT_RIGHT="lcms-special-right-dev-test";
		public final static String DERAULT_COPYRIGHT_DESCRIPTION="lcms-special-description-dev-test";
		Logger logger = Logger.getLogger(this.getClass().getName());
	@SuppressWarnings("unchecked")
	@Test
	public void testCopyrightAll(){
		
		CopyrightOwnerViewModel covm=new CopyrightOwnerViewModel();
		covm.setTitle(DERAULT_RIGHT);
		covm.setDescription(DERAULT_COPYRIGHT_DESCRIPTION);
		
		//创建ND资源版权方接口
		CopyrightOwnerViewModel createOVM = testCreate(covm);
		Assert.assertEquals("测试创建资源版权方不通过", DERAULT_RIGHT, createOVM.getTitle());
		
		//创建时测试title是否重复
		String returnStr= postCreate(createOVM);
		Map<String,Object> m = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试版权方名称已经存在不通过", LifeCircleErrorMessageMapper.CheckDuplicateCopyrightOwnerTitleFail.getCode(),m.get("code"));
		
		//修改ND资源版权方接口
		createOVM.setTitle(DERAULT_RIGHT+"update-test");
		CopyrightOwnerViewModel updateM = testUpdate(createOVM);
		Assert.assertNotNull(updateM);
		Assert.assertEquals("测试修改资源版权方不通过", DERAULT_RIGHT+"update-test", updateM.getTitle());
		
	    //修改时测试版权方是否存在
		updateM.setIdentifier(UUID.randomUUID().toString());
		updateM.setTitle("test79b68b55");
		String updateStr= putUpdate(updateM);
		Map<String,Object> updateMap = ObjectUtils.fromJson(updateStr, Map.class);
		Assert.assertEquals("测试修改时版权方是否存在不通过", LifeCircleErrorMessageMapper.CopyrightOwnerNotFound.getCode(),updateMap.get("code"));
		
		//修改时测试title是否重复
		CopyrightOwnerViewModel rpm=new CopyrightOwnerViewModel();
		rpm.setTitle("1daada8324dj");
		rpm.setDescription(DERAULT_COPYRIGHT_DESCRIPTION);
		CopyrightOwnerViewModel rpmResult=testCreate(rpm);//先创建，后修改，然后测试
		rpmResult.setTitle(DERAULT_RIGHT+"update-test");
		String updateTitle=putUpdate(rpmResult);
		Map<String,Object> updateResult=ObjectUtils.fromJson(updateTitle, Map.class);
		Assert.assertEquals("测试修改时title已经存在不通过", LifeCircleErrorMessageMapper.CheckDuplicateCopyrightOwnerTitleFail.getCode(),updateResult.get("code"));
		//删除创建的记录
		testDelete(rpmResult.getIdentifier());
		
		//测试查询资源版权方
		String limit="(0,20)";
		ListViewModel<CopyrightOwnerViewModel> getM=testGetDetail(DERAULT_RIGHT+"update-test",limit);
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
	
	public CopyrightOwnerViewModel testCreate(CopyrightOwnerViewModel covm){
		String resStr = postCreate(covm);
		CopyrightOwnerViewModel m = fromJson(resStr, CopyrightOwnerViewModel.class);
		return m;
	}
	
	public CopyrightOwnerViewModel testUpdate(CopyrightOwnerViewModel rpvm) {
		String resStr = putUpdate(rpvm);
		CopyrightOwnerViewModel m = fromJson(resStr, CopyrightOwnerViewModel.class);
		return m;
	}

	public String testDelete(String uuid) {
		String str = del(uuid);
		return str;
	}

	public ListViewModel<CopyrightOwnerViewModel> testGetDetail(String words,String limit) {
		String resStr = getDetail(words,limit);
		//ResourceProviderViewModel m = fromJson(resStr, ResourceProviderViewModel.class);
		ListViewModel<CopyrightOwnerViewModel> m=fromJson(resStr, ListViewModel.class);
		return m;
	}
	
	protected String postCreate(CopyrightOwnerViewModel covm){
		
		StringBuffer uri = new StringBuffer("/v0.6/copyright/provider");
		String resStr = null;
		String param=toJson(covm);
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	}
	
	protected String putUpdate(CopyrightOwnerViewModel rpvm){
		
		String param = toJson(rpvm);
		
		String uri = "/v0.6/copyright/provider/"+rpvm.getIdentifier();
		String resStr = null;
		try {
			resStr = MockUtil.mockPut(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
	protected String del(String uuid){
		String resStr = null;
		String uri = "/v0.6/copyright/provider"+"/"+uuid;
		try {
			resStr = MockUtil.mockDelete(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
	protected String getDetail(String words,String limit){
		String resStr = null;
		StringBuffer uri = new StringBuffer("/v0.6/copyright/provider?words="+words+"&limit="+limit);
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
}
