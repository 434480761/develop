package nd.esp.service.lifecycle.entity.cs;

import java.util.HashMap;
import java.util.Map;

import nd.esp.service.lifecycle.support.Constant;


/**
 * @title 内容服务session模型
 * @desc
 * @atuh lwx
 * @createtime on 2015年6月30日 下午9:33:04
 */
public class CsSession {
    public static final String CS_ADMIN_ROLE = "admin";

    public static final String CS_USER_ROLE = "user";

    public static final String CS_DEFAULT_ROLE = CS_ADMIN_ROLE;
    
    public static final String CS_DEFAULT_UID="777";
    
    public static final int CS_DEFAULT_EXPIRE=Constant.FILE_OPERATION_EXPIRETIME;
    
    
    /**	
     * @desc:获取默认创建的回话参数配置  
     * @createtime: 2015年6月30日 
     * @author: liuwx 
     * @return
     */
    public static Map<String,Object> getDefaultSessionParam(){
        Map<String,Object> map =new HashMap<String, Object>();
        map.put("uid", CS_DEFAULT_UID);
        map.put("role", CS_DEFAULT_ROLE);
        map.put("expires", CS_DEFAULT_EXPIRE);
        return map;
    }
    
    private String session;

    // 授权的路径（包含子目录项）
    private String path;

    // 服务Id(UUID)
    private String serviceId;

    // 用户uid
    private String uid;

    // 用户角色
    private String role;

    // 过期时间(秒)
    private String expireAt;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(String expireAt) {
        this.expireAt = expireAt;
    }


}
