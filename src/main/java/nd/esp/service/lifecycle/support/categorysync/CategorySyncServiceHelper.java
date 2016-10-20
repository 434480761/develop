package nd.esp.service.lifecycle.support.categorysync;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.educommon.vos.ResClassificationViewModel;
import nd.esp.service.lifecycle.educommon.vos.ResourceViewModel;
import nd.esp.service.lifecycle.repository.common.IndexSourceType;
import nd.esp.service.lifecycle.repository.exception.EspStoreException;
import nd.esp.service.lifecycle.repository.model.CategoryData;
import nd.esp.service.lifecycle.repository.model.categorysync.CategorySync;
import nd.esp.service.lifecycle.repository.model.categorysync.CategorySyncError;
import nd.esp.service.lifecycle.repository.sdk.CategoryDataRepository;
import nd.esp.service.lifecycle.repository.sdk.categorysync.CategorySyncErrorRepository;
import nd.esp.service.lifecycle.repository.sdk.categorysync.CategorySyncRepository;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.busi.CommonHelper;
import nd.esp.service.lifecycle.utils.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.nd.gaea.client.WafResourceAccessException;
import com.nd.gaea.client.auth.ServerAuthorizationProvider;
import com.nd.gaea.client.http.WafSecurityHttpClient;
import com.nd.gaea.util.WafJsonMapper;
/**
 * 维度同步帮助类
 * @author xiezy
 * @date 2016年10月18日
 */
public class CategorySyncServiceHelper {
	private static final Logger LOG = LoggerFactory.getLogger(CategorySyncServiceHelper.class);
	
	@Autowired
	private CategorySyncRepository categorySyncRepository;
	@Autowired
	private CategorySyncErrorRepository categorySyncErrorRepository;
	@Autowired
	private CategoryDataRepository categoryDataRepository;
	
	@Qualifier(value = "defaultJdbcTemplate")
	@Autowired
	private JdbcTemplate defaultJdbcTemplate;
	@Qualifier(value = "questionJdbcTemplate")
	@Autowired
	private JdbcTemplate questionJdbcTemplate;
	
	/**
	 * 维度数据同步
	 * @author xiezy
	 * @date 2016年10月19日
	 */
	public void syncCategory(){
		WafSecurityHttpClient wafSecurityHttpClient = getWafSecurityHttpClient(getToken());
		
		CategorySync category = new CategorySync();
//		category.setCategoryType(TYPE_CATEGORY);
		category.setHandle(CategorySyncConstant.UNHANDLE);
		List<CategorySync> categoryList = null;
		try {
			categoryList = categorySyncRepository.getAllByExample(category);
		} catch (EspStoreException e) {
			throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
                    e.getLocalizedMessage());
		}
		
		if(CollectionUtils.isNotEmpty(categoryList)){
			for(CategorySync categorySync : categoryList){
				try {
					Map<String, Map<String,List<? extends ResClassificationViewModel>>> map = 
							getNeedUpdateResourceMap(categorySync.getSyncCategory(), categorySync.getCategoryType(), categorySync.getOperationType());
				
					if(CollectionUtils.isNotEmpty(map)){
						for(String key : map.keySet()){
							try {
								String[] array = key.split("\\+");
								ResourceViewModel viewModel = new ResourceViewModel();
								viewModel.setCategories(map.get(key));
								
								String uri = LifeCircleApplicationInitializer.properties.getProperty("lcms.uri") + "/v0.6/" 
										+ array[0] + "/" + array[1] + "?is_obvious=false";
//								String uri = "localhost:8080/esp-lifecycle/v0.6/" 
//										+ array[0] + "/" + array[1] + "?is_obvious=false";
								HttpHeaders httpHeaders = new HttpHeaders();
								httpHeaders.setContentType(MediaType.APPLICATION_JSON);
								@SuppressWarnings("unchecked")
								HttpEntity<Map<String, Object>> entity = 
										new HttpEntity<Map<String, Object>>(WafJsonMapper.parse(WafJsonMapper.toJson(viewModel), Map.class), httpHeaders);
								wafSecurityHttpClient.executeForObject(uri, HttpMethod.PATCH, entity, Map.class);
							} catch (Exception e) {
								String[] array = key.split("\\+");
								
								if(e instanceof WafResourceAccessException){
									WafResourceAccessException wafE = (WafResourceAccessException)e;
									if("LC/CHANGE_OBJECT_NOT_EXIST".equalsIgnoreCase(wafE.getRemoteResponseEntity().getBody().getCode())){
										continue;
									}else{
										recordSyncError(categorySync.getSyncCategory(), array[0], array[1], e.getLocalizedMessage(), CategorySyncConstant.PATCH_ERROR);
										continue;
									}
								}else{
									recordSyncError(categorySync.getSyncCategory(), array[0], array[1], e.getLocalizedMessage(), CategorySyncConstant.PATCH_ERROR);
									continue;
								}
							}
						}
					}
				} catch (Exception e) {
					recordSyncError(categorySync.getSyncCategory(), "", "", e.getLocalizedMessage(), CategorySyncConstant.UNCERTAIN_ERROR);
					continue;
				}
				
				//对已经处理做的CategorySync的handle进行修改
				categorySync.setHandle(CategorySyncConstant.HANDLED);
				try {
					categorySyncRepository.update(categorySync);
				} catch (EspStoreException e) {
					recordSyncError(categorySync.getSyncCategory(), "", "", e.getLocalizedMessage(), CategorySyncConstant.UPDATE_CATEGORY_SYNC_ERROR);
					continue;
				}
			}
		}
	}
	
	/**
	 * 获取WafSecurityHttpClient
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param token
	 * @return
	 */
	private WafSecurityHttpClient getWafSecurityHttpClient(final String token){
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		wafSecurityHttpClient.setServerAuthorizationProvider(new ServerAuthorizationProvider() {
			
			@Override
			public void resetToken() {
			}
			
			@Override
			public String getUserId() {
				return null;
			}
			
			@Override
			public String getAuthorization(URI uri, HttpMethod method) {
				return "Bearer \""+ token +"\"";
			}
		});
		
		return wafSecurityHttpClient;
	}
	
	/**
	 * 获取Bearer Token
	 * @author xiezy
	 * @date 2016年10月19日
	 * @return
	 */
	private String getToken() {
		WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
		String url = LifeCircleApplicationInitializer.properties
				.getProperty("esp_uc_api_domain") + "bearer_tokens";

		Map<String, String> map = new HashMap<String, String>();
		map.put("login_name", "esp_lifecycle");
		map.put("password", "d4876ded8c0df211825893ae8a3c6df9");

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> entity = 
				new HttpEntity<Map<String, String>>(map, httpHeaders);
		Map<String, Object> tokenInfo = wafSecurityHttpClient.executeForObject(url, HttpMethod.POST, entity, Map.class);
		
		if(CollectionUtils.isNotEmpty(tokenInfo)){
			return (String) tokenInfo.get("access_token");
		}
		
		return "";
	}
	
	/**
	 * 记录错误日志
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param syncCategory
	 * @param resType
	 * @param resource
	 * @param message
	 * @param code
	 */
	private void recordSyncError(String syncCategory, String resType, String resource,
			String message, Integer code){
		CategorySyncError categorySyncError = new CategorySyncError();
		categorySyncError.setIdentifier(UUID.randomUUID().toString());
		categorySyncError.setSyncCategory(syncCategory);
		categorySyncError.setResType(resType);
		categorySyncError.setResource(resource);
		categorySyncError.setCode(code);
		if(message.length() > 998){
			message = message.substring(0, 998);
		}
		categorySyncError.setMessage(message);
		try {
			categorySyncErrorRepository.add(categorySyncError);
		} catch (EspStoreException e) {
			LOG.error("维度数据同步错误记录出现异常", e);
		}
	}
	
	/**
	 * 获取需要修改的维度Map
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param syncCategory
	 * @param categoryType
	 * @param operationType
	 * @return
	 */
	private Map<String, Map<String,List<? extends ResClassificationViewModel>>> getNeedUpdateResourceMap(String syncCategory, int categoryType, final int operationType){
		String sql = "select rc.identifier as rcid,rc.resource as rid,rc.taxOnCode as code,"
				+ "rc.taxOnPath as path,rc.primary_category as type,rc.category_name as mapkey "
				+ "from resource_categories rc where ";
		if(categoryType == CategorySyncConstant.TYPE_CATEGORY){
			sql += " rc.category_name='" + syncCategory + "'";
		}else if(categoryType == CategorySyncConstant.TYPE_CATEGORY_DATA){
			sql += " rc.taxOnCode='" + syncCategory + "'";
		}
		
		final Map<String, Map<String,List<? extends ResClassificationViewModel>>> resultMap = 
				new HashMap<String, Map<String,List<? extends ResClassificationViewModel>>>();
		
		getResultMap(resultMap, defaultJdbcTemplate, sql, operationType);
		getResultMap(resultMap, questionJdbcTemplate, sql, operationType);
		
		return resultMap;
	}
	
	/**
	 * 获取需要修改的维度Map -- map处理
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param resultMap
	 * @param jdbcTemplate
	 * @param url
	 * @param operationType
	 */
	private void getResultMap(final Map<String, Map<String,List<? extends ResClassificationViewModel>>> resultMap,
								JdbcTemplate jdbcTemplate, String sql, final int operationType){
		
		jdbcTemplate.query(sql, new RowMapper<String>(){
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				String resType = rs.getString("type");
				String resource = rs.getString("rid");
				String ndCode = rs.getString("code");
				String key = rs.getString("mapkey");
				
				//$O,$S,$E,$R特殊处理
				if(ndCode.startsWith("$O") || ndCode.startsWith("$S") || ndCode.startsWith("$E") || ndCode.startsWith("$R")){
					String defaultKey = "res_type";
					if(resType.equals(IndexSourceType.AssetType.getName())){
						defaultKey = "assets_type";
					}
					key = CommonHelper.getResCategoryKey(ndCode, defaultKey);
				}
				
				if(resultMap.containsKey(resType + "+" + resource)){
					Map<String,List<? extends ResClassificationViewModel>> map = resultMap.get(resType + "-" + resource);
					if(CollectionUtils.isNotEmpty(map)){
						if(map.containsKey(key)){
							@SuppressWarnings("unchecked")
							List<ResClassificationViewModel> list = (List<ResClassificationViewModel>) map.get(key);
							ResClassificationViewModel rcvm = new ResClassificationViewModel();
							rcvm.setIdentifier(rs.getString("rcid"));
							rcvm.setTaxoncode(ndCode);
							rcvm.setTaxonpath(rs.getString("path"));
							if(operationType == CategorySyncConstant.OPERATION_DELETE){//删除
								rcvm.setOperation("delete");
							}else if(operationType == CategorySyncConstant.OPERATION_UPDATE){//更新
								rcvm.setOperation("update");
							}
							list.add(rcvm);
						}else{
							List<ResClassificationViewModel> list = new ArrayList<ResClassificationViewModel>();
							ResClassificationViewModel rcvm = new ResClassificationViewModel();
							rcvm.setIdentifier(rs.getString("rcid"));
							rcvm.setTaxoncode(ndCode);
							rcvm.setTaxonpath(rs.getString("path"));
							if(operationType == CategorySyncConstant.OPERATION_DELETE){//删除
								rcvm.setOperation("delete");
							}else if(operationType == CategorySyncConstant.OPERATION_UPDATE){//更新
								rcvm.setOperation("update");
							}
							list.add(rcvm);
							map.put(key, list);
						}
					}else{
						map = new HashMap<String, List<? extends ResClassificationViewModel>>();
						List<ResClassificationViewModel> list = new ArrayList<ResClassificationViewModel>();
						ResClassificationViewModel rcvm = new ResClassificationViewModel();
						rcvm.setIdentifier(rs.getString("rcid"));
						rcvm.setTaxoncode(ndCode);
						rcvm.setTaxonpath(rs.getString("path"));
						if(operationType == CategorySyncConstant.OPERATION_DELETE){//删除
							rcvm.setOperation("delete");
						}else if(operationType == CategorySyncConstant.OPERATION_UPDATE){//更新
							rcvm.setOperation("update");
						}
						list.add(rcvm);
						map.put(key, list);
					}
				}else{
					Map<String,List<? extends ResClassificationViewModel>> map = new HashMap<String, List<? extends ResClassificationViewModel>>();
					List<ResClassificationViewModel> list = new ArrayList<ResClassificationViewModel>();
					ResClassificationViewModel rcvm = new ResClassificationViewModel();
					rcvm.setIdentifier(rs.getString("rcid"));
					rcvm.setTaxoncode(ndCode);
					rcvm.setTaxonpath(rs.getString("path"));
					if(operationType == CategorySyncConstant.OPERATION_DELETE){//删除
						rcvm.setOperation("delete");
					}else if(operationType == CategorySyncConstant.OPERATION_UPDATE){//更新
						rcvm.setOperation("update");
					}
					list.add(rcvm);
					map.put(key, list);
					resultMap.put(resType + "+" + resource, map);
				}
				
				return null;
			}
		});
	}
	
	/**
	 * 记录修改的维度数据
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param syncCategory
	 * @param categoryType
	 * @param operationType
	 */
	public void categorySync(String syncCategory, Integer categoryType, Integer operationType){
		try {
			CategorySync temp = new CategorySync();
			temp.setSyncCategory(syncCategory);
			temp = categorySyncRepository.getByExample(temp);
			
			if(temp == null){
				CategorySync categorySync = new CategorySync();
				categorySync.setIdentifier(UUID.randomUUID().toString());
				categorySync.setSyncCategory(syncCategory);
				categorySync.setCategoryType(categoryType);
				categorySync.setOperationType(operationType);
				categorySync.setOperationTime(new Timestamp(System.currentTimeMillis()));
				categorySync.setHandle(CategorySyncConstant.UNHANDLE);
				
				categorySyncRepository.add(categorySync);
			}else{
				temp.setCategoryType(categoryType);
				temp.setOperationType(operationType);
				temp.setOperationTime(new Timestamp(System.currentTimeMillis()));
				temp.setHandle(CategorySyncConstant.UNHANDLE);
				
				categorySyncRepository.update(temp);
			}
		} catch (EspStoreException e) {
			
			LOG.error("维度数据同步出现异常", e);
			
			recordSyncError(syncCategory, "", "", e.getLocalizedMessage(), CategorySyncConstant.INSERT_CATEGORY_SYNC_ERROR);
		}
	}
	
	/**
	 * 初始化,处理旧数据
	 * @author xiezy
	 * @date 2016年10月20日
	 * @return
	 */
	public Map<String, Integer> initSync(){
		Map<String, Integer> resultMap = new HashMap<String, Integer>();
		
		WafSecurityHttpClient wafSecurityHttpClient = getWafSecurityHttpClient(getToken());
		
		final Set<String> ndCodes = new HashSet<String>();
		getAllNdCode(ndCodes, defaultJdbcTemplate);
		getAllNdCode(ndCodes, questionJdbcTemplate);
		
		List<CategorySync> categoryList = getAllCategorySyncs(ndCodes);
		
		int count = 0;
		
		if(CollectionUtils.isNotEmpty(categoryList)){
			for(CategorySync categorySync : categoryList){
				try {
					Map<String, Map<String,List<? extends ResClassificationViewModel>>> map = 
							getNeedUpdateResourceMap(categorySync.getSyncCategory(), categorySync.getCategoryType(), categorySync.getOperationType());
				
					if(CollectionUtils.isNotEmpty(map)){
						for(String key : map.keySet()){
							try {
								String[] array = key.split("\\+");
								ResourceViewModel viewModel = new ResourceViewModel();
								viewModel.setCategories(map.get(key));
								
								String uri = LifeCircleApplicationInitializer.properties.getProperty("lcms.uri") + "/v0.6/" 
										+ array[0] + "/" + array[1] + "?is_obvious=false";
//								String uri = "localhost:8080/esp-lifecycle/v0.6/" 
//								+ array[0] + "/" + array[1] + "?is_obvious=false";
								HttpHeaders httpHeaders = new HttpHeaders();
								httpHeaders.setContentType(MediaType.APPLICATION_JSON);
								@SuppressWarnings("unchecked")
								HttpEntity<Map<String, Object>> entity = 
										new HttpEntity<Map<String, Object>>(WafJsonMapper.parse(WafJsonMapper.toJson(viewModel), Map.class), httpHeaders);
								wafSecurityHttpClient.executeForObject(uri, HttpMethod.PATCH, entity, Map.class);
							} catch (Exception e) {
								String[] array = key.split("\\+");
								
								if(e instanceof WafResourceAccessException){
									WafResourceAccessException wafE = (WafResourceAccessException)e;
									if("LC/CHANGE_OBJECT_NOT_EXIST".equalsIgnoreCase(wafE.getRemoteResponseEntity().getBody().getCode())){
										continue;
									}else{
										recordSyncError(categorySync.getSyncCategory(), array[0], array[1], e.getLocalizedMessage(), CategorySyncConstant.PATCH_ERROR);
										continue;
									}
								}else{
									recordSyncError(categorySync.getSyncCategory(), array[0], array[1], e.getLocalizedMessage(), CategorySyncConstant.PATCH_ERROR);
									continue;
								}
							}
						}
					}
				} catch (Exception e) {
					recordSyncError(categorySync.getSyncCategory(), "", "", e.getLocalizedMessage(), CategorySyncConstant.UNCERTAIN_ERROR);
					continue;
				}
				
				count++;
			}
		}
		
		resultMap.put("ndCodes", ndCodes.size());
		resultMap.put("categoryList", categoryList.size());
		resultMap.put("dealCount", count);
		return resultMap;
	}
	
	/**
	 * 获取全部的resource_categories中的taxOnCode
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param ndCodeList
	 * @param jdbcTemplate
	 */
	private void getAllNdCode(final Set<String> ndCodes, JdbcTemplate jdbcTemplate){
		String sql = "SELECT DISTINCT(rc.taxOnCode) as code FROM resource_categories rc";
		jdbcTemplate.query(sql, new RowMapper<String>(){
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				ndCodes.add(rs.getString("code"));
				return null;
			}
		});
	}
	
	/**
	 * 获取全部需要处理的CategorySync
	 * @author xiezy
	 * @date 2016年10月19日
	 * @param ndCodes
	 * @return
	 */
	private List<CategorySync> getAllCategorySyncs(Set<String> ndCodes){
		List<CategorySync> list = new ArrayList<CategorySync>();
		
		if(CollectionUtils.isNotEmpty(ndCodes)){
			for(String ndCode : ndCodes){
				CategoryData categoryData = new CategoryData();
				categoryData.setNdCode(ndCode);
				try {
					categoryData = categoryDataRepository.getByExample(categoryData);
				} catch (EspStoreException e) {
					throw new LifeCircleException(HttpStatus.INTERNAL_SERVER_ERROR,
		                    LifeCircleErrorMessageMapper.StoreSdkFail.getCode(),
		                    e.getLocalizedMessage());
				}
				
				CategorySync categorySync = new CategorySync();
				if(categoryData != null){
					categorySync.setIdentifier(UUID.randomUUID().toString());
					categorySync.setSyncCategory(ndCode);
					categorySync.setCategoryType(CategorySyncConstant.TYPE_CATEGORY_DATA);
					categorySync.setOperationType(CategorySyncConstant.OPERATION_UPDATE);
					categorySync.setOperationTime(new Timestamp(System.currentTimeMillis()));
					categorySync.setHandle(CategorySyncConstant.UNHANDLE);
				}else{//说明已经被删了
					categorySync.setIdentifier(UUID.randomUUID().toString());
					categorySync.setSyncCategory(ndCode);
					categorySync.setCategoryType(CategorySyncConstant.TYPE_CATEGORY_DATA);
					categorySync.setOperationType(CategorySyncConstant.OPERATION_DELETE);
					categorySync.setOperationTime(new Timestamp(System.currentTimeMillis()));
					categorySync.setHandle(CategorySyncConstant.UNHANDLE);
				}
				
				list.add(categorySync);
			}
		}
		
		return list;
	}
}
