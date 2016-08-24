package nd.esp.service.lifecycle.controller.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.VrLifeTestImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewTags;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

import org.junit.Assert;
import org.junit.Test;
public class TestVrLifeController extends  VrLifeTestImpl{
	

	@Test
	public void testAll(){
		String resType="assets";
		//创建资源
		ResourceViewModel rvm = testCreate(resType,UUID.randomUUID().toString(),null);
		// 测试资源审核
		StatusReviewViewModel4In srvmi=getDefaultStatusReviewViewModel4In();
	   //String uuid="00046f6c-1b6f-48d0-8bec-e2b8239d54c6";
	   String uuid=rvm.getIdentifier();	
		srvmi.setIdentifier(uuid);
		srvmi.setResType(resType);
		StatusReviewViewModel4Out srvmo=testUpdate(resType, uuid, toJson(srvmi));
		Assert.assertNotNull(srvmo);
		Assert.assertEquals("测试VR资源审核不通过", uuid, srvmo.getIdentifier());
		
		//测试资源状态是否合法
		StatusReviewViewModel4In IllegalModel=getDefaultStatusReviewViewModel4In();
		IllegalModel.setStatus("haha");
		IllegalModel.setIdentifier(uuid);
		IllegalModel.setResType(resType);
		String errorStr= putUpdate(resType, uuid, toJson(IllegalModel));
		@SuppressWarnings("unchecked")
		Map<String,Object> errorMap = ObjectUtils.fromJson(errorStr, Map.class);
		Assert.assertEquals("测试资源审核时状态是否合法不通过","LC/VRLIFE_STATUS_IS_ILLEGAL",errorMap.get("code"));
		
		//测试资源操作类型是否合法
		StatusReviewViewModel4In IllegalOperation=getIllegalStatusReviewViewModel4In();
		IllegalOperation.setIdentifier(uuid);
		IllegalOperation.setResType(resType);
		String illegalStr= putUpdate(resType, uuid, toJson(IllegalOperation));
		@SuppressWarnings("unchecked")
		Map<String,Object> illegalMap = ObjectUtils.fromJson(illegalStr, Map.class);
		Assert.assertEquals("测试资源审核时操作类型是否合法不通过","LC/VRLIFE_TAGS_OPERATION_IS_ILLEGAL",illegalMap.get("code"));
		
		//测试资源类型是questions的情况
		//创建资源
		String restype="questions";
		ResourceViewModel qrvm = testCreate(restype,UUID.randomUUID().toString(),null);
		StatusReviewViewModel4In questions=getDefaultStatusReviewViewModel4In();
		//String identifier="0006cb2d-61f3-4069-aa10-86cb164e025f";
		String identifier=qrvm.getIdentifier();
		questions.setIdentifier(identifier);
		questions.setResType(restype);
		StatusReviewViewModel4Out questionsModel=testUpdate(restype, identifier, toJson(questions));
		Assert.assertNotNull(questionsModel);
		Assert.assertEquals("测试VR资源审核不通过", identifier, questionsModel.getIdentifier());
		
		//删除资源
		String s = testDelete(restype,identifier);
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnMap.get("process_code").toString());
		
		String ss = testDelete(resType,uuid);
		Map<String,Object> returnmap = ObjectUtils.fromJson(ss, Map.class);
		Assert.assertNotNull(returnmap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnmap.get("process_code").toString());
		
		/*//测试VR资源推荐功能type类型
		String Type="haha";
		String SkeletonId="";
		String Include="TI";
		String returnStr =getDetail(Type, SkeletonId, Include);
		@SuppressWarnings("unchecked")
		Map<String,Object> returnMap = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试资源审核时状态是否合法不通过","LC/VRLIFE_TYPE_IS_ILLEGAL",returnMap.get("code"));*/
		
		//测试VR资源推荐功能
		/*//String skeletonId="7235e275-ee05-4673-ab2b-ca06e071da8e";
		String type="skeleton";
		String include="TI";
		 ListViewModel<ResourceViewModel> getM=testGetDetail(type,include);
		Assert.assertEquals("测试VR资源推荐功能不通过","",getM );*/
	}
	
	
	public StatusReviewViewModel4In getDefaultStatusReviewViewModel4In(){

		StatusReviewViewModel4In srvm=new StatusReviewViewModel4In();	
		srvm.setStatus("ONLINE");
		srvm.setPublishType("PT01001");
		srvm.setReviewPerson("tommy");
		srvm.setTags(getDefaultStatusReviewTags());
		return srvm;
	}
	
	public StatusReviewViewModel4In getIllegalStatusReviewViewModel4In(){

		StatusReviewViewModel4In srvm=new StatusReviewViewModel4In();	
		srvm.setStatus("ONLINE");
		srvm.setPublishType("PT01001");
		srvm.setReviewPerson("tommy");
		srvm.setTags(getIllegalStatusReviewTags());
		return srvm;
	}
	
	public List<StatusReviewTags> getDefaultStatusReviewTags(){
		List<StatusReviewTags> resultList=new ArrayList<StatusReviewTags>();
		List<String> list=new ArrayList<String>();
		list.add("1");
		list.add("2");
		StatusReviewTags srtAdd=new StatusReviewTags();
		srtAdd.setOperation("add");
		srtAdd.setTags(list);
		resultList.add(srtAdd);
		StatusReviewTags srtDelete=new StatusReviewTags();
		srtDelete.setOperation("delete");
		srtDelete.setTags(list);
		resultList.add(srtDelete);
		return resultList;
	}
	
	public List<StatusReviewTags> getIllegalStatusReviewTags(){
		List<StatusReviewTags> resultList=new ArrayList<StatusReviewTags>();
		List<String> list=new ArrayList<String>();
		list.add("1");
		list.add("2");
		StatusReviewTags srtAdd=new StatusReviewTags();
		srtAdd.setOperation("get");
		srtAdd.setTags(list);
		resultList.add(srtAdd);
		StatusReviewTags srtDelete=new StatusReviewTags();
		srtDelete.setOperation("update");
		srtDelete.setTags(list);
		resultList.add(srtDelete);
		return resultList;
	}
}
