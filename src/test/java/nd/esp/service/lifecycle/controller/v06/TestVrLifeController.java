package nd.esp.service.lifecycle.controller.v06;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewTags;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4In;
import nd.esp.service.lifecycle.vos.vrlife.StatusReviewViewModel4Out;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.nd.gaea.rest.testconfig.MockUtil;
public class TestVrLifeController extends  SimpleJunitTest4ResourceImpl{
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	@Test
	public void testAll(){
	
		String resType=IndexSourceType.AssetType.getName();
		//创建资源
		ResourceViewModel rvm = testCreate(resType,UUID.randomUUID().toString(),null);
		// 测试资源审核
		StatusReviewViewModel4In srvmi=getDefaultStatusReviewViewModel4In();
		String uuid=rvm.getIdentifier();	
		srvmi.setIdentifier(uuid);
		srvmi.setResType(resType);
		StatusReviewViewModel4Out srvmo=testCheck(resType, uuid, toJson(srvmi));
		Assert.assertNotNull(srvmo);
		Assert.assertEquals("测试VR资源审核不通过", uuid, srvmo.getIdentifier());
		
		//测试资源状态是否合法
		StatusReviewViewModel4In IllegalModel=getDefaultStatusReviewViewModel4In();
		IllegalModel.setStatus("haha");
		IllegalModel.setIdentifier(uuid);
		IllegalModel.setResType(resType);
		String errorStr= putCheck(resType, uuid, toJson(IllegalModel));
		@SuppressWarnings("unchecked")
		Map<String,Object> errorMap = ObjectUtils.fromJson(errorStr, Map.class);
		Assert.assertEquals("测试资源审核时状态是否合法不通过","LC/VRLIFE_STATUS_IS_ILLEGAL",errorMap.get("code"));
		
		//测试资源操作类型是否合法
		StatusReviewViewModel4In IllegalOperation=getIllegalStatusReviewViewModel4In();
		IllegalOperation.setIdentifier(uuid);
		IllegalOperation.setResType(resType);
		String illegalStr= putCheck(resType, uuid, toJson(IllegalOperation));
		@SuppressWarnings("unchecked")
		Map<String,Object> illegalMap = ObjectUtils.fromJson(illegalStr, Map.class);
		Assert.assertEquals("测试资源审核时操作类型是否合法不通过","LC/VRLIFE_TAGS_OPERATION_IS_ILLEGAL",illegalMap.get("code"));
		
		//测试维度数据是否是合法的PT维度
		StatusReviewViewModel4In IllegalPT=getDefaultStatusReviewViewModel4In();
		IllegalPT.setPublishType("YZ01001");
		IllegalModel.setIdentifier(uuid);
		IllegalModel.setResType(resType);
		String illegalPT=putCheck(resType, uuid, toJson(IllegalPT));
		@SuppressWarnings("unchecked")
		Map<String,Object> illegalPTMap = ObjectUtils.fromJson(illegalPT, Map.class);
		Assert.assertEquals("测试资源审核时PT是否合法不通过","LC/PT_CODE_IS_NOT_EXIST",illegalPTMap.get("code"));
		
		//覆盖PT类型和tags是空的情况
		StatusReviewViewModel4In nullModel=getDefaultStatusReviewViewModel4In();
		nullModel.setStatus("init");
		nullModel.setIdentifier(uuid);
		nullModel.setResType(resType);
		nullModel.setPublishType(null);
		nullModel.setTags(null);
		putCheck(resType, uuid, toJson(nullModel));
		
		
		//测试资源类型是questions的情况
		//创建资源
		String restype=IndexSourceType.QuestionType.getName();
		ResourceViewModel qrvm = testCreate(restype,UUID.randomUUID().toString(),null);
		StatusReviewViewModel4In questions=getDefaultStatusReviewViewModel4In();
		String identifier=qrvm.getIdentifier();
		questions.setIdentifier(identifier);
		questions.setResType(restype);
		StatusReviewViewModel4Out questionsModel=testCheck(restype, identifier, toJson(questions));
		Assert.assertNotNull(questionsModel);
		Assert.assertEquals("测试VR资源审核不通过", identifier, questionsModel.getIdentifier());
		
		//删除资源
		String s = testDelete(restype,identifier);
		@SuppressWarnings("unchecked")
		Map<String,Object> returnMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnMap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnMap.get("process_code").toString());
		
		String ss = testDelete(resType,uuid);
		@SuppressWarnings("unchecked")
		Map<String,Object> returnmap = ObjectUtils.fromJson(ss, Map.class);
		Assert.assertNotNull(returnmap);
		Assert.assertEquals("测试删除接口不通过", LifeCircleErrorMessageMapper.DeleteResourceSuccess.getCode(), returnmap.get("process_code").toString());

	}
	
	
	public StatusReviewViewModel4In getDefaultStatusReviewViewModel4In(){

		StatusReviewViewModel4In srvm=new StatusReviewViewModel4In();	
		srvm.setStatus("ONLINE");
		srvm.setPublishType("PT01001");
		srvm.setReviewPerson("tommy");
		srvm.setTags(getDefaultStatusReviewTags());
		return srvm;
	}
	
	private StatusReviewViewModel4In getIllegalStatusReviewViewModel4In(){

		StatusReviewViewModel4In srvm=new StatusReviewViewModel4In();	
		srvm.setStatus("ONLINE");
		srvm.setPublishType("PT01001");
		srvm.setReviewPerson("tommy");
		srvm.setTags(getIllegalStatusReviewTags());
		return srvm;
	}
	
	private List<StatusReviewTags> getDefaultStatusReviewTags(){
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
	
	private List<StatusReviewTags> getIllegalStatusReviewTags(){
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
	
	private StatusReviewViewModel4Out testCheck(String resType,String uuid,String param){
		String resStr = putCheck(resType,uuid,param);
		StatusReviewViewModel4Out m = fromJson(resStr, StatusReviewViewModel4Out.class);
		return m;
	}
	
	private String putCheck(String resType,String uuid,String param){
		
		String uri = "/v0.6/vrlife/"+resType+"/status/review/"+uuid;
		String resStr = null;
		try {
			resStr = MockUtil.mockPut(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
}
