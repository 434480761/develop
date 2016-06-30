package nd.esp.service.lifecycle.support.uc;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.nd.gaea.client.http.WafSecurityHttpClient;
import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 
 * <p>Title: UC组件         </p>
 * <p>Description: UcClient </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年3月24日           </p>
 * @author lianggz
 */
@Component
public class UcClient {
    private final static Logger LOG= LoggerFactory.getLogger(UcClient.class);


    private static String ucUri = LifeCircleApplicationInitializer.properties.getProperty("esp_uc_api_domain");

    private static String realm = LifeCircleApplicationInitializer.properties.getProperty("esp_uc_api_realm");
    
    private static Long CACHE_EXPIRE = 1L;
    private static Long CACHE_MAX_SIZE = 2000L;
    
    private static LoadingCache<String, String> orgCache;
    
    @PostConstruct
    private void init() {
        CacheBuilder<Object, Object> builder = CacheBuilder
                .newBuilder()
                .maximumSize(CACHE_MAX_SIZE)
                .expireAfterWrite(CACHE_EXPIRE, TimeUnit.DAYS);
        
        orgCache = builder.build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return getOrgId(key);
            }
        });
    }
    
    /**
     * 从缓存获取组织ID	   
     * @param orgName
     * @return
     */
    public String getOrgIdByCache(String orgName) {
        // 如果组织名称为空，返回空字符串
        if(StringUtils.isBlank(orgName)){
            return "";
        }
        try {  
            // 如果有缓存则返回；否则运算、缓存、然后返回
            return orgCache.get(orgName);
        } catch (Exception e) {
            LOG.error("UcClient.getOrgIdByCache", ExceptionUtils.getMessage(e));
            return "";
        }  
    }
    
    /**
     * 从UC获取组织ID     
     * @param orgName
     * @return
     */
    public String getOrgId(String orgName){
        // 定义请求参数
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("org_name", orgName);
        // 调用UC接口
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        JSONObject resopnse = wafSecurityHttpClient.postForObject(this.ucUri + "/organizations/actions/query", requestBody, JSONObject.class);
        return resopnse.getString("org_id");
    }
    
    /**
     * 从UC获取组织名称     
     * @param orgId
     * @return
     * @author lianggz
     */
    public String getOrgName(String orgId){
        // 调用UC接口
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        JSONObject resopnse = wafSecurityHttpClient.getForObject(this.ucUri + "/organizations/"+orgId, JSONObject.class);
        return resopnse.getString("org_name");
    }

	/**
     * 新增领域角色
     * @return
     */
    public JSONObject addRole(String roleName,String remarks){
        // 定义请求参数
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("realm", realm);
        requestBody.put("role_name", roleName);
        requestBody.put("remarks", remarks);
        requestBody.put("is_default", false);
        requestBody.put("auth_extra", 0);

        // 调用UC接口
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
        JSONObject resopnse = wafSecurityHttpClient.postForObject(this.ucUri + "/roles", requestBody, JSONObject.class);
        return resopnse;
    }

    /**
     * 获取领域下角色列表
     * @param
     * @return null if exception catched.
     * @author lanyl
     */
    public JSONObject listRealmRoles() {
        try {
            Map<String, String> params = new HashMap<>();
            WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
            return wafSecurityHttpClient.getForObject(this.ucUri + "/roles?realm=" + this.realm, JSONObject.class, params);
        } catch (Exception e) {
            LOG.error("UcClient.listRealmRoles", e.getMessage());
            return null;
        }
    }

    /**
     * 添加用户的角色
     * <p>Description: 访问权限：ROLE_ADMIN 或 BIZ_SERVER </p>
     * <p>Create Time: 2016年06月29日   </p>
     * <p>Create author: lanyl   </p>
     * @param userId
     * @param roleId
     * @return
     */
    public JSONObject addUserRole(String userId, Integer roleId) {
        if (StringUtils.isNotBlank(userId)){
            JSONObject result = null;
            WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
            result =  wafSecurityHttpClient.postForObject(this.ucUri + "/users/" + userId + "/roles/" + roleId, null, JSONObject.class);
            return result;
        }else {
            return null;
        }
    }

    /**
     * 删除用户的角色
     * <p>Description: 访问权限：ROLE_ADMIN 或 BIZ_SERVER </p>
     * <p>Create Time: 2016年06月29日   </p>
     * <p>Create author: lanyl   </p>
     * @param userId
     * @param roleId
     * @return
     */
    public JSONObject deleteUserRole(String userId, Integer roleId) {
        if (StringUtils.isNotBlank(userId)){
            JSONObject result = null;
            WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
            result = wafSecurityHttpClient.deleteForObject(this.ucUri + "/users/" + userId + "/roles/" + roleId, JSONObject.class);
            return result;
        }else {
            return null;
        }
    }


    /**
     * 获取用户角色
     * <p>Description:              </p>
     * <p>Create Time: 2015年10月27日   </p>
     * <p>Create author: Jawinton   </p>
     * @param userId
     * @return
     */
    public JSONObject listUserRoles(String userId) {
        if(StringUtils.isNotBlank(userId)){
            Map<String, String> params = new HashMap<>();
            WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient();
            return wafSecurityHttpClient.getForObject(this.ucUri + "/users/" + userId + "/roles?realm=" + this.realm, JSONObject.class, params);
        }else {
            return null;
        }
    }
}