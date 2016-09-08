package nd.esp.service.lifecycle.support.busi;

import com.nd.gaea.client.http.WafSecurityHttpClient;
import nd.esp.service.lifecycle.support.Constant;
import nd.esp.service.lifecycle.support.Constant.CSInstanceInfo;
import nd.esp.service.lifecycle.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class SessionUtil {
    
    private static Map<String,Map<String,Object>> cacheSessions = new HashMap<String,Map<String,Object>>();
    //默认创建session的用户id
    public final static String DEAFULT_SESSION_USERID="777";
    
    /**
     * 调用cs接口获取session
     *
     * @param uid 用户id
     *
     * @return session id
     */
    public static String createSession(String uid) {
        return SessionUtil.createSession(uid, Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getUrl(),
                Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getPath(),
                Constant.CS_INSTANCE_MAP.get(Constant.CS_DEFAULT_INSTANCE).getServiceId());
    }

    /**
     * 调用cs接口获取session
     *
     * @param uid 用户id
     * @param url 获取session的api
     * @param path 请求session的作用path
     * @param serviceId 服务id
     * 
     */
    public static String createSession(String uid, String url, String path, String serviceId) {
        if (cacheSessions.containsKey(uid + "@" + serviceId + path)) {
            Map<String, Object> sessionBefore = cacheSessions.get(uid + "@" + serviceId + path);
            long expireTime = Long.parseLong(String.valueOf(sessionBefore.get("expire_at")));
            if (expireTime - System.currentTimeMillis() > 6000000) {
                return String.valueOf(sessionBefore.get("session"));
            }
        }
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("path", path);
        requestBody.put("service_id", serviceId);
        requestBody.put("uid",uid);
        requestBody.put("role",Constant.FILE_OPERATION_ROLE);
        requestBody.put("expires",Constant.FILE_OPERATION_EXPIRETIME);
        WafSecurityHttpClient wafSecurityHttpClient = new WafSecurityHttpClient(Constant.WAF_CLIENT_RETRY_COUNT);
        url = url + "/sessions";
        Map<String, Object> session = wafSecurityHttpClient.postForObject( url, requestBody, Map.class);
        cacheSessions.put(uid+"@"+serviceId+path, session);
        return String.valueOf(session.get("session"));
    }
    
    /**	
     * @desc: 获取session 
     * @createtime: 2015年6月25日 
     * @author: liuwx 
     * @param uid
     * @param instanceInfo
     * @return
     */
    public static String createSession(String uid,CSInstanceInfo instanceInfo){
        
        return SessionUtil.createSession(uid, 
                instanceInfo.getUrl(), 
                instanceInfo.getPath(),
                instanceInfo.getServiceId());
       
    }
    /**	
     * @desc: 获取session uid使用默认值
     * @createtime: 2015年6月25日 
     * @author: liuwx 
     * @param instanceInfo
     * @return
     */
    public static String createSession(CSInstanceInfo instanceInfo){
       
        return createSession(DEAFULT_SESSION_USERID, instanceInfo);
    }
    
    /**	
     * @desc:获取Href中对应的cs实例key  ${ref-path}/edu
     * @createtime: 2015年6月25日 
     * @author: liuwx 
     * @param href
     * @see Constant.CSInstanceInfo
     * @see Constant#CS_INSTANCE_MAP
     * @return
     */
    public static String getHrefInstanceKey(String href){
        if(StringUtils.isEmpty(href)){
            throw new IllegalArgumentException("href必须不为空");
        }
        if(href.indexOf("/") < 0){
            throw new IllegalArgumentException("href格式不对");
        }

        int secondSlash = href.indexOf("/", href.indexOf("/")+1);
        return href.substring(0, secondSlash);
    }
    
    public static void main(String[] args) {
        String s="${ref-path}/edu/esp/coursewares/4108bde5-470d-404e-a252-29d915dce254.pkg/main.xml";
        System.out.println(s.substring(0,s.indexOf("/", s.indexOf("/")+1)));
        System.out.println(s.indexOf("/", s.indexOf("/")+1));
        System.out.println(getHrefInstanceKey(s));
       }
}
