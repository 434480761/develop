package nd.esp.service.lifecycle.controller.v06.resourcesharing;

import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.resourcesharing.v06.ResourceSharingViewModel;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestResourceSharingController extends SimpleJunitTest4ResourceImpl {
			
	private static final Logger logger = LoggerFactory.getLogger(TestResourceSharingController.class);
	private static final String DEFAULT_RESOURCE_SHARING_TITLE= "lcms-special-resource_sharing-title-test";
	
	@Test
	public void testResourceSharingAll(){
		
		String resType=IndexSourceType.AssetType.getName();
		String uuid=UUID.randomUUID().toString();
		
		//创建资源
		ResourceViewModel rvm = testCreate(resType,uuid,null);
		Assert.assertEquals("测试创建资源不通过", uuid, rvm.getIdentifier());
		
		//测试创建资源的私密分享
		ResourceSharingViewModel rsvm=new ResourceSharingViewModel();
		rsvm.setTitle(DEFAULT_RESOURCE_SHARING_TITLE);
		rsvm.setResourceType(resType);
		rsvm.setResource(rvm.getIdentifier());
		
		ResourceSharingViewModel returnRsvm=testCreateResourceSharing(rsvm);
		Assert.assertEquals("测试创建资源的私密分享不通过", rsvm.getResourceType() ,returnRsvm.getResourceType());
		
		//测试获取资源的私密分享记录列表
		String limit="(0,8)";
		ListViewModel<ResourceSharingViewModel> resourceSharingList=testGetResourceSharingList(resType,rvm.getIdentifier(),limit);
		Assert.assertNotNull(resourceSharingList);
		Assert.assertEquals("测试获取资源的私密分享记录列表不通过", limit,  resourceSharingList.getLimit());
		
			
		//测试通过分享id和密码获取资源详细信息
		ResourceViewModel  resmodel=getResourceBySharingId(returnRsvm);
		Assert.assertEquals("测试通过分享id和密码获取资源详细信息不通过", returnRsvm.getResource(),resmodel.getIdentifier());
		
		//测试通过分享id和密码获取资源详细信息时，密码为空
		ResourceSharingViewModel backupRsvm=new ResourceSharingViewModel();
		backupRsvm.setIdentifier(returnRsvm.getIdentifier());
		backupRsvm.setProtectPasswd(null);
		backupRsvm.setResource(returnRsvm.getResource());
		backupRsvm.setTitle(returnRsvm.getTitle());
		backupRsvm.setResourceType(returnRsvm.getResourceType());
		String nullPwd=getResource(backupRsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> nullPwdMap=ObjectUtils.fromJson(nullPwd,Map.class);
		Assert.assertEquals("测试密码为空不通过",LifeCircleErrorMessageMapper.ProtectPasswdIsNotEmpty.getCode(),nullPwdMap.get("code"));
			
		
		//测试通过分享id和密码获取资源详细信息时，密码不正确
		backupRsvm.setProtectPasswd(returnRsvm.getProtectPasswd()+System.currentTimeMillis());
		String errPwd=getResource(backupRsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> errPwdMap=ObjectUtils.fromJson(errPwd,Map.class);
		Assert.assertEquals("测试密码为空不通过",LifeCircleErrorMessageMapper.ProtectedSharingResourceNotFound.getCode(),errPwdMap.get("code"));
		
		//测试通过分享后的id和密码，获取资源的下载地址时，密码为空
		backupRsvm.setProtectPasswd(null);
		String NotPwd=getDownload(backupRsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> NotPwdMap=ObjectUtils.fromJson(NotPwd,Map.class);
		Assert.assertEquals("测试密码为空不通过",LifeCircleErrorMessageMapper.ProtectPasswdIsNotEmpty.getCode(),NotPwdMap.get("code"));
		
		//测试通过分享后的id和密码，获取资源的下载地址时， 密码不正确
		backupRsvm.setProtectPasswd(returnRsvm.getProtectPasswd()+System.currentTimeMillis());
		String ErrorPwd=getDownload(backupRsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> errorPwdMap=ObjectUtils.fromJson(ErrorPwd,Map.class);
		Assert.assertEquals("测试密码为空不通过",LifeCircleErrorMessageMapper.ProtectedSharingResourceNotFound.getCode(),errorPwdMap.get("code"));
		
		//测试通过分享后的id和密码，获取资源的下载地址
		AccessModel am=testDownload(returnRsvm);
		Assert.assertNotNull("测试获取资源的下载地址不通过", am.getSessionId());
				
		//测试通过分享记录id删除一笔资源的私密分享
		String deleteSharing=testDeteteSharing(returnRsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> deleteMap=ObjectUtils.fromJson(deleteSharing, Map.class);
		Assert.assertNotNull(deleteMap);
		Assert.assertEquals("测试删除一笔资源的私密分享不通过", LifeCircleErrorMessageMapper.DeleteResourceSharingSuccess.getCode(), deleteMap.get("process_code").toString());
		
		//测试删除资源的私密分享时，未找到可取消的资源分享
		String deletedSharing=delSharing(returnRsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> deletedMap=ObjectUtils.fromJson(deletedSharing,Map.class);
		Assert.assertEquals("测试未找到可取消的资源分享不通过",LifeCircleErrorMessageMapper.DeleteResourceSharingFail.getCode(),deletedMap.get("code"));
		
		//测试删除一个资源的全部私密分享
		ResourceSharingViewModel rsvm_one=testCreateResourceSharing(rsvm);
		Assert.assertEquals("测试创建资源的私密分享不通过", rsvm.getResourceType() ,rsvm_one.getResourceType());
		
		ResourceSharingViewModel rsvm_two=testCreateResourceSharing(rsvm);
		Assert.assertEquals("测试创建资源的私密分享不通过", rsvm.getResourceType() ,rsvm_two.getResourceType());
		
		//获取资源的下载地址,资源不可下载
 //   	rsvm_one.setResourceType("kkk");
//		String notResource=getDownload(rsvm_one);
//		@SuppressWarnings("unchecked")
//		Map<String,Object> notResourceMap=ObjectUtils.fromJson(notResource,Map.class);
//		Assert.assertEquals("测试资源不可下载不通过",LifeCircleErrorMessageMapper.CommonAPINotSupportResourceType.getCode(),notResourceMap.get("code"));
		
		//测试删除一个资源的全部私密分享
		String deleteAllSharing=testDeteteAllSgaring(rvm.getIdentifier(),resType);
		@SuppressWarnings("unchecked")
		Map<String,Object> deleteAllMap=ObjectUtils.fromJson(deleteAllSharing, Map.class);
		Assert.assertNotNull(deleteAllMap);
		Assert.assertEquals("测试删除资源的全部私密分享不通过", LifeCircleErrorMessageMapper.DeleteResourceSharingSuccess.getCode(), deleteAllMap.get("process_code").toString());
		
		
		//删除资源
		String s = testDelete(resType,uuid);
		@SuppressWarnings("unchecked")
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnMap.get("process_code").toString());
		
		//测试创建资源的私密分享时，资源是否存在
		String notExist=postCreateResourceSharing(rsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> notExistMap=ObjectUtils.fromJson(notExist,Map.class);
		Assert.assertEquals("测试资源是否存在不通过",LifeCircleErrorMessageMapper.SourceResourceNotFond.getCode(),notExistMap.get("code"));
		
		
	}

	private String testDeteteAllSgaring(String uuid,String resType) {
		String str=delAllSharing(uuid,resType);
		return str;
	}

	private String delAllSharing(String uuid,String resType) {
		String resStr = null;
		String uri = "/v0.6/"+resType+"/"+uuid+"/public/protected_sharing";
		try {
			resStr = MockUtil.mockDelete(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}

	private String testDeteteSharing(ResourceSharingViewModel returnRsvm) {
		String str = delSharing(returnRsvm);
		return str;
	}

	private String delSharing(ResourceSharingViewModel returnRsvm) {
		String resStr = null;
		String uri = "/v0.6/"+returnRsvm.getResourceType()+"/"+returnRsvm.getResource()+"/public/protected_sharing/"+returnRsvm.getIdentifier();
		try {
			resStr = MockUtil.mockDelete(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}

	private AccessModel testDownload(ResourceSharingViewModel returnRsvm) {
		
		String resStr = getDownload(returnRsvm);
		AccessModel m = fromJson(resStr, AccessModel.class);
		return m;
	}

	private String getDownload(ResourceSharingViewModel returnRsvm) {
		
		StringBuffer uri = new StringBuffer("/v0.6/public/protected_sharing/"+returnRsvm.getIdentifier()+"/download");
		String resStr = null;
		String param=toJson(returnRsvm);
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	}
	

	private ResourceViewModel getResourceBySharingId(ResourceSharingViewModel returnRsvm) {
		
		String resStr = getResource(returnRsvm);
		ResourceViewModel m = fromJson(resStr, ResourceViewModel.class);
		return m;
	}

	private String getResource(ResourceSharingViewModel returnRsvm) {
		
		StringBuffer uri = new StringBuffer("/v0.6/public/protected_sharing/"+returnRsvm.getIdentifier());
		String resStr = null;
		String param=toJson(returnRsvm);
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	}

	private ListViewModel<ResourceSharingViewModel> testGetResourceSharingList(String resType, String uuid, String limit) {
		String resStr = getResourceList(resType,uuid,limit);
		ListViewModel<ResourceSharingViewModel> m=fromJson(resStr, ListViewModel.class);
		return m;
	}

	private String getResourceList(String resType, String uuid, String limit) {
		
		StringBuffer uri = new StringBuffer("/v0.6/"+resType+"/"+uuid+"/public/protected_sharing?"+"limit="+limit);
		String resStr = null;
		String param=null;
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	}

	private ResourceSharingViewModel testCreateResourceSharing(ResourceSharingViewModel rsvm) {
		String resStr = postCreateResourceSharing(rsvm);
		ResourceSharingViewModel m = fromJson(resStr, ResourceSharingViewModel.class);
		return m;
	}

	

	private String postCreateResourceSharing(ResourceSharingViewModel rsvm) {
		
        StringBuffer uri = new StringBuffer("/v0.6/"+rsvm.getResourceType()+"/"+rsvm.getResource()+"/public/protected_sharing");
		String resStr = null;
		String param=toJson(rsvm);
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	 }



	

	
}
