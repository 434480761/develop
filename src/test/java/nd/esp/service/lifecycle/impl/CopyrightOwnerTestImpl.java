package nd.esp.service.lifecycle.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.nd.gaea.rest.testconfig.MockUtil;
import com.nd.gaea.util.WafJsonMapper;

import nd.esp.service.lifecycle.BaseControllerConfig;
import nd.esp.service.lifecycle.vos.ListViewModel;
import nd.esp.service.lifecycle.vos.copyright.v06.CopyrightOwnerViewModel;

public class CopyrightOwnerTestImpl extends BaseControllerConfig{

	Logger logger = Logger.getLogger(this.getClass().getName());
	
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
