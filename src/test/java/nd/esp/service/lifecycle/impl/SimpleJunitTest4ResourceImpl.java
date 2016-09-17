package nd.esp.service.lifecycle.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.JunitTest4Resource;
import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResCoverageViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResEducationalViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResLifeCycleViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRelationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResRightViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResTechInfoViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.models.AccessModel;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.utils.StringUtils;
import nd.esp.service.lifecycle.utils.gson.ObjectUtils;

import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import com.nd.gaea.rest.testconfig.MockUtil;
import com.nd.gaea.util.WafJsonMapper;
/**
 * 单元测试基本的实现类
 * @author xuzy
 *
 */
public class SimpleJunitTest4ResourceImpl extends BaseControllerConfig implements JunitTest4Resource {
	Logger logger = Logger.getLogger(this.getClass().getName());
	//几个变量使用的默认值，方便清理数据
	public final static String DERAULT_CREATOR="lcms-special-creator-dev-test";
	public final static String DERAULT_DESCRIPTION="lcms-special-description-dev-test";
	public final static String DERAULT_PUBLISHER="lcms-special-publisher-dev-test";
	
	
	public AccessModel testUpload(String resType,String uuid,Boolean renew,String coverage) {
		String returnStr = getUpload(resType,uuid,renew,coverage);
		AccessModel am = ObjectUtils.fromJson(returnStr, AccessModel.class);
        return am;
	}
	
	public AccessModel testDownload(String resType,String uuid) {
		String returnStr = getDownload(resType,uuid,null,null);
		AccessModel am = ObjectUtils.fromJson(returnStr, AccessModel.class);
        return am;
	}

	public ResourceViewModel testCreate(String resType,String uuid,String param){
		String resStr = postCreate(resType,uuid,param);
		ResourceViewModel m = fromJson(resStr, ResourceViewModel.class);
		return m;
	}
	
	public ResourceViewModel testUpdate(String resType,String uuid,String param) {
		String resStr = putUpdate(resType,uuid,param);
		ResourceViewModel m = fromJson(resStr, ResourceViewModel.class);
		return m;
	}

	public String testDelete(String resType,String uuid) {
		String str = del(resType,uuid);
		return str;
	}

	public ResourceViewModel testGetDetail(String resType,String uuid,String include,Boolean isAll) {
		String resStr = getDetail(resType,uuid,include,isAll);
		ResourceViewModel m = fromJson(resStr, ResourceViewModel.class);;
		return m;
	}
	
	public Map<String, ResourceViewModel> testBatchDetail(String resType,List<String> uuids,String include){
		String resStr = getBatchDetail(resType,uuids,include);
		Map<String, ResourceViewModel> m = ObjectUtils.fromJson(StringUtils.toCamelCase(resStr,'_'), new TypeToken<Map<String, ResourceViewModel>>() {
        });
		return m;
	}
	


	public void testSpecialFeature() {
		
	}

	/**
	 * 请求通用的上传接口
	 * @param resType
	 * @param uuid
	 * @param renew
	 * @param coverage
	 * @return
	 */
	protected String getUpload(String resType,String uuid,Boolean renew,String coverage){
		StringBuffer uri = new StringBuffer("/v0.6/"+resType+"/"+uuid+"/uploadurl?uid=777");
		if(renew != null){
			uri.append("&renew="+renew);
		}
		if(coverage != null){
			uri.append("&coverage="+coverage);
		}
        String resStr = null;
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("getUpload error", e);
		}
		return resStr;
	}

	
	/**
	 * 请求通用的下传接口
	 * @param resType
	 * @param uuid
	 * @param key
	 * @param coverage
	 * @return
	 */
	protected String getDownload(String resType,String uuid,String key,String coverage){
		StringBuffer uri = new StringBuffer("/v0.6/"+resType+"/"+uuid+"/downloadurl?uid=777");
		if(key != null){
			uri.append("&key="+key);
		}
		if(coverage != null){
			uri.append("&coverage="+coverage);
		}
        String resStr = null;
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("getDownload error", e);
		}
		return resStr;
	}
	
	
	/**
	 * 请求通用的创建接口(支持入参json)
	 * @param resType
	 * @param uuid
	 * @param param
	 * @return
	 */
	protected String postCreate(String resType,String uuid,String param){
		if(param == null){
			ResourceViewModel rvm = getDefaultResouceViewModel();
			rvm.setIdentifier(uuid);
			param = toJson(rvm);
		}
		StringBuffer uri = new StringBuffer("/v0.6/"+resType);
		if(uuid != null){
			uri.append("/"+uuid);
		}
		String resStr = null;
		try {
			resStr = MockUtil.mockPost(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("postCreate error", e);
		}
		return resStr;
	}
	
	/**
	 * 需要转码（创建资源）
	 * @param resType
	 * @param uuid
	 * @param param
	 * @param status
	 * @return
	 */
	protected String testCreateNeedTransCode(String resType,String uuid, String param,String status) {
		if(param == null){
			ResourceViewModel rvm = getDefaultResouceViewModel();
			ResLifeCycleViewModel lc = getDefaultLifeCycle();
			lc.setStatus(status);
			rvm.setIdentifier(uuid);
			rvm.setLifeCycle(lc);
			param = toJson(rvm);
		}
		return postCreate(resType,uuid,param);
	}
	
	/**
	 * 请求通用的创建接口(支持入参json)
	 * @param resType
	 * @param uuid
	 * @param param
	 * @return
	 */
	protected String putUpdate(String resType,String uuid,String param){
		if(param == null){
			ResourceViewModel rvm = getDefaultResouceViewModel();
			rvm.setIdentifier(uuid);
			param = toJson(rvm);
		}
		String uri = "/v0.6/"+resType+"/"+uuid;
		String resStr = null;
		try {
			resStr = MockUtil.mockPut(mockMvc, uri.toString(), param);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
	/**
	 * 需要转码（创建资源）
	 * @param resType
	 * @param uuid
	 * @param param
	 * @param status
	 * @return
	 */
	protected String testUpdateNeedTransCode(String resType,String uuid, String param,String status) {
		if(param == null){
			ResourceViewModel rvm = getDefaultResouceViewModel();
			ResLifeCycleViewModel lc = getDefaultLifeCycle();
			rvm.setIdentifier(uuid);
			lc.setStatus(status);
			rvm.setIdentifier(uuid);
			rvm.setLifeCycle(lc);
			param = toJson(rvm);
		}
		return putUpdate(resType,uuid,param);
	}
	
	protected String del(String resType,String uuid){
		String resStr = null;
		String uri = "/v0.6/"+resType+"/"+uuid;
		try {
			resStr = MockUtil.mockDelete(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
	protected String getDetail(String resType,String uuid,String include,Boolean isAll){
		String resStr = null;
		StringBuffer uri = new StringBuffer("/v0.6/"+resType+"/"+uuid+"?");
		if(include != null){
			uri.append("&include=").append(include);
		}
		if(isAll != null){
			uri.append("&isAll=").append(isAll);
		}
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
	protected String getBatchDetail(String resType,List<String> uuids,String include){
		String resStr = null;
		StringBuffer uri = new StringBuffer("/v0.6/"+resType+"/list?1=1");
		for (String rid : uuids) {
			uri.append("&rid=").append(rid);
		}
		if(include != null){
			uri.append("&include=").append(include);
		}
		try {
			resStr = MockUtil.mockGet(mockMvc, uri.toString(), null);
		} catch (Exception e) {
			logger.error("putUpdate error", e);
		}
		return resStr;
	}
	
	/**
	 * 获取默认的资源入参属性(传入主键id)
	 * @param uuid
	 * @return
	 */
	protected ResourceViewModel getDefaultResouceViewModel(String uuid){
		ResourceViewModel rvm = getDefaultResouceViewModel();
		if(uuid != null){
			rvm.setIdentifier(uuid);
		}
		return rvm;
	}
	
	/**
	 * 获取默认的资源入参属性(传入关系属性)
	 * @param relations
	 * @return
	 */
	protected ResourceViewModel getDefaultResouceViewModel(List<ResRelationViewModel> relations){
		ResourceViewModel rvm = getDefaultResouceViewModel();
		rvm.setRelations(relations);
		return rvm;
	}
	
	/**
	 * 获取默认的资源入参属性
	 * @return
	 */
	protected ResourceViewModel getDefaultResouceViewModel(){
		ResourceViewModel rvm = new ResourceViewModel();
		rvm.setTitle("LC单元测试默认用例");
		rvm.setDescription(DERAULT_DESCRIPTION);
		rvm.setLanguage("zh-CN");
		
		Map<String,String> preview = new HashMap<String, String>();
		preview.put("120", "/package/..");
		rvm.setPreview(preview);
		
		Map<String,Object> customPropMap = new HashMap<String, Object>();
		customPropMap.put("key", "test");
		rvm.setCustomProperties(customPropMap);
		
		rvm.setLifeCycle(getDefaultLifeCycle());
		
		rvm.setEducationInfo(getDefaultEducationInfo());
		
		Map<String,ResTechInfoViewModel> techInfoMap = new HashMap<String, ResTechInfoViewModel>();
		techInfoMap.put("href", getDefaultTechInfo("href", "video/mp4"));
		techInfoMap.put("source", getDefaultTechInfo("source", "video/mp4"));
		rvm.setTechInfo(techInfoMap);
		
		List<ResCoverageViewModel> coverages = new ArrayList<ResCoverageViewModel>();
		coverages.add(getDefaultCoverage());
		rvm.setCoverages(coverages);
		
		Map<String, List<? extends ResClassificationViewModel>> categoryMap = new HashMap<String, List<? extends ResClassificationViewModel>>();
		List<ResClassificationViewModel> categories = new ArrayList<ResClassificationViewModel>();
		categories.add(getDefaultCategory());
		categoryMap.put("phase", categories);
		
		rvm.setCategories(categoryMap);
		
		rvm.setCopyright(getDefaultCopyRight());
		
		return rvm;
	}
	
	protected String getDefaultResouceViewModelJson(){
		ResourceViewModel rvm = getDefaultResouceViewModel();
		String result = null;
		result = toJson(rvm);
		return result;
	}
	
	/**
	 * 获取默认的生命周期属性
	 * @return
	 */
	protected ResLifeCycleViewModel getDefaultLifeCycle(){
		ResLifeCycleViewModel lc = new ResLifeCycleViewModel();
		lc.setCreator(DERAULT_CREATOR);
		lc.setEnable(true);
		lc.setProvider("LCMS");
		lc.setProviderSource("LC");
		lc.setPublisher(DERAULT_PUBLISHER);
		lc.setStatus("CREATED");
		lc.setVersion("v0.6");
		return lc;
	}
	
	/**
	 * 获取默认的教育属性
	 * @return
	 */
	protected ResEducationalViewModel getDefaultEducationInfo(){
		ResEducationalViewModel educationInfo = new ResEducationalViewModel();
		educationInfo.setAgeRange("20");
		return educationInfo;
	}
	
	/**
	 * 获取默认的techInfo信息
	 * @return
	 */
	protected ResTechInfoViewModel getDefaultTechInfo(String title,String format){
		ResTechInfoViewModel ti = new ResTechInfoViewModel();
		ti.setTitle(title);
		ti.setFormat(format);
		ti.setLocation("${ref-path}/edu/esp/assets/a7702137-a70f-41cc-a430-1b8d606fe13a.pkg/aa.jpg");
		ti.setSize(1000);
		return ti;
	}
	
	/**
     * 获取能进行打包的techInfo.href信息
     * @return
     */
    protected ResTechInfoViewModel getValidHref(String resType, String uuid, String format){
        ResTechInfoViewModel ti = new ResTechInfoViewModel();
        ti.setTitle("href");
        ti.setFormat(format);
        ti.setLocation("${ref-path}" + Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath() + "/"
                + resType + "/" + uuid + ".pkg/main.xml");
        ti.setSize(1000);
        return ti;
    }
	
	/**
	 * 获取默认的覆盖范围信息
	 * @return
	 */
	protected ResCoverageViewModel getDefaultCoverage(){
		ResCoverageViewModel rc = new ResCoverageViewModel();
		rc.setTargetType("User");
		rc.setTarget("291213");
		rc.setStrategy("OWNER");
		rc.setTargetTitle("单元测试用例");
		return rc;
	}
	
	/**
	 * 获取默认的版权信息
	 * @return
	 */
	protected ResRightViewModel getDefaultCopyRight(){
		ResRightViewModel cr = new ResRightViewModel();
		cr.setAuthor("xu");
		cr.setDescription("人民教育出版社");
		cr.setRight("haha");
		return cr;
	}
	
	/**
	 * 获取默认的维度信息
	 * @return
	 */
	protected ResClassificationViewModel getDefaultCategory(){
		ResClassificationViewModel c = new ResClassificationViewModel();
		c.setTaxonpath("K12/$ON020000/$ON020500/$SB0300/$E005000/"+UUID.randomUUID().toString());
		c.setTaxoncode("$ON020000");
		return c;
	}
	
	/**
	 * 将对象属性的驼峰结构转化为“_”分隔符
	 * @param o
	 * @return
	 */
	protected String toJson(Object o){
		try {
			return WafJsonMapper.toJson(o);
		} catch (IOException e) {
			logger.error("json转换出错！",e);
		}
		return null;
	}
	
	/**
	 * 将对象属性的“_”分隔符转化为驼峰结构
	 * @param json
	 * @param T
	 * @return
	 */
	protected <T> T fromJson(String json,Class T){
		try {
			return (T) WafJsonMapper.parse(json, T);
		} catch (IOException e) {
			logger.error("json转换出错！",e);
		}
		return null;
	} 
	
    

}
