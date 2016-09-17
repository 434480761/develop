package nd.esp.service.lifecycle.controller.v06.coveragesharing;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.impl.SimpleJunitTest4ResourceImpl;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.coveragesharing.v06.CoverageSharingViewModel;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nd.gaea.rest.testconfig.MockUtil;

public class TestCoverageSharingController extends SimpleJunitTest4ResourceImpl {
    private static final Logger logger = LoggerFactory.getLogger(TestCoverageSharingController.class);
    
    private static final String DEFAULT_COVERAGE_SHARING_TARGET = "lcms-special-sharing-dev-test";
    
   @Test
    public void testAll(){
       
	   //测试创建资源库之间的资源分享
	   CoverageSharingViewModel csvm=getDefaultCoverageSharingViewModel();
	   CoverageSharingViewModel returnCsvm=testCreate(csvm);
	   Assert.assertEquals("测试创建资源库之间的资源分享不通过", csvm.getSourceCoverage() ,returnCsvm.getSourceCoverage());
	   
	   //测试参数逻辑校验
	   CoverageSharingViewModel illegal=getDefaultCoverageSharingViewModel();
	   illegal.setSourceCoverage("dadsadsadsadsadsa");
	   illegal.setTargetCoverage("ghggfhgfh");
	   String returnStr=postCreate(illegal);
		@SuppressWarnings("unchecked")
		Map<String,Object> m = ObjectUtils.fromJson(returnStr, Map.class);
		Assert.assertEquals("测试参数逻辑校验不通过", LifeCircleErrorMessageMapper.CoverageSharingParamFail.getCode(),m.get("code"));
		
		//测试覆盖范围类型是否在可选范围内
		 CoverageSharingViewModel illegalCsvm=getDefaultCoverageSharingViewModel();
		illegalCsvm.setSourceCoverage("Yzc/"+DEFAULT_COVERAGE_SHARING_TARGET+System.currentTimeMillis());
		illegalCsvm.setTargetCoverage("Yzc/"+DEFAULT_COVERAGE_SHARING_TARGET+System.currentTimeMillis());
		String illegalStr=postCreate(illegalCsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> illegalMap= ObjectUtils.fromJson(illegalStr, Map.class);
		Assert.assertEquals("测试覆盖范围类型是否在可选范围内不通过", LifeCircleErrorMessageMapper.CoverageTargetTypeNotExist.getCode(),illegalMap.get("code"));
		
		//测试是否已经分享过
		String againStr=postCreate(returnCsvm);
		@SuppressWarnings("unchecked")
		Map<String,Object> againMap = ObjectUtils.fromJson(againStr, Map.class);
		Assert.assertEquals("测试是否已经分享过不通过", LifeCircleErrorMessageMapper.CoverageSharingExistFail.getCode(),againMap.get("code"));
		
		//测试查询资源库分享时source和target都为空
		String getErrorStr=getList(null, null, "(0,20)");
		@SuppressWarnings("unchecked")
		Map<String,Object> errorMap = ObjectUtils.fromJson(getErrorStr, Map.class);
		Assert.assertEquals("测试查询资源库分享时source和target都为空不通过", LifeCircleErrorMessageMapper.CoverageSharingParamFail.getCode(),errorMap.get("code"));
		
		//测试查询资源库分享时source为空且target不为空
		ListViewModel<CoverageSharingViewModel> nullsourceList=testGetList(null,returnCsvm.getTargetCoverage(),"(0,20)");
		Assert.assertNotNull(nullsourceList);
		Assert.assertEquals("测试查询资源分享不通过", "(0,20)", nullsourceList.getLimit());
		
		//测试查询资源库分享时source为空且target不为空
		ListViewModel<CoverageSharingViewModel> nulltargetList=testGetList(returnCsvm.getSourceCoverage(),null,"(0,20)");
		Assert.assertNotNull(nulltargetList);
		Assert.assertEquals("测试查询资源分享不通过", "(0,20)", nulltargetList.getLimit());
		
		//测试查询资源分享
		String limit="(0,20)";
		ListViewModel<CoverageSharingViewModel> getList=testGetList(returnCsvm.getSourceCoverage(),returnCsvm.getTargetCoverage(),limit);
		Assert.assertNotNull(getList);
		Assert.assertEquals("测试查询资源分享不通过", limit, getList.getLimit());
				
		//测试取消资源库之间的资源分享找不到
		String str=testDelete(UUID.randomUUID().toString());
		@SuppressWarnings("unchecked")
		Map<String,Object> Str= ObjectUtils.fromJson(str, Map.class);
		Assert.assertNotNull(Str);
		Assert.assertEquals("测试取消资源库之间的资源分享找不到不通过", LifeCircleErrorMessageMapper.CoverageSharingNotFound.getCode(),Str.get("code"));
		
		//测试取消资源库之间的资源分享
		String s=testDelete(returnCsvm.getIdentifier());
		@SuppressWarnings("unchecked")
		Map<String,Object> returnDeleteMap = ObjectUtils.fromJson(s, Map.class);
		Assert.assertNotNull(returnDeleteMap);
		Assert.assertEquals("测试取消资源库之间的资源分享不通过", LifeCircleErrorMessageMapper.DeleteCoverageSharingSuccess.getCode(), returnDeleteMap.get("process_code").toString());
    }
   
   public CoverageSharingViewModel getDefaultCoverageSharingViewModel(){
	   CoverageSharingViewModel csvm=new CoverageSharingViewModel();
	   csvm.setSourceCoverage("Org/"+DEFAULT_COVERAGE_SHARING_TARGET+System.currentTimeMillis());
	   csvm.setTargetCoverage("Org/"+DEFAULT_COVERAGE_SHARING_TARGET+System.currentTimeMillis());
	   return csvm;
   }
   public CoverageSharingViewModel testCreate(CoverageSharingViewModel csvm){
		String resStr = postCreate(csvm);
		CoverageSharingViewModel m = fromJson(resStr, CoverageSharingViewModel.class);
		return m;
	}
   
   protected String postCreate(CoverageSharingViewModel csvm){
		
		StringBuffer uri = new StringBuffer("/v0.6/resources/coverages/sharing");
		String resStr = null;
		String param=toJson(csvm);
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	}
   
   public String testDelete(String uuid) {
		String str = del(uuid);
		return str;
	}
   
   protected String del(String uuid){
		String resStr = null;
		String uri = "/v0.6/resources/coverages/sharing"+"/"+uuid;
		try {
			resStr = MockUtil.mockDelete(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
   
   public ListViewModel<CoverageSharingViewModel> testGetList(String source,String target,String limit) {
		String resStr = getList(source,target,limit);
		ListViewModel<CoverageSharingViewModel> m=fromJson(resStr, ListViewModel.class);
		return m;
	}
   
	protected String getList(String source,String target,String limit){
		StringBuffer uri=new StringBuffer();
		if(source==null&&target==null){
			uri=new StringBuffer("/v0.6/resources/coverages/sharing?limit="+limit);
		}
		else if(source==null&&target!=null){
			uri=new StringBuffer("/v0.6/resources/coverages/sharing?source_coverage&target_coverage="+target+"&limit="+limit);
		}
		else if(source!=null&&target==null){
			uri=new StringBuffer("/v0.6/resources/coverages/sharing?source_coverage="+source+"&target_coverage&limit="+limit);
		}
		else{
		    uri = new StringBuffer("/v0.6/resources/coverages/sharing?source_coverage="+source+"&target_coverage="+target+"&limit="+limit);
		}
		String resStr = null;
		
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
   
}