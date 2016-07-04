package nd.esp.service.lifecycle.support.uc;

import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;
import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Title: UcRoleClient  </p>
 * <p>Description: UcRoleClient </p>
 * <p>Copyright: Copyright (c) 2016     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年07月01日           </p>
 * @author lianggz
 */
@Component
public class UcRoleClient {
    /** 超级管理员*/
    public static String SUPERADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_super_admin");
    /** 库管理员*/
    public static String COVERAGEADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_coverage_admin");
    /** 资源创建者角色*/
    public static String RESCREATOR = LifeCircleApplicationInitializer.properties.getProperty("esp_res_creator");
    /** 维度管理者角色*/
    public static String CATEGORYDATAADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_category_data_admin");
    /** 资源消费者角色*/
    public static String RESCONSUMER = LifeCircleApplicationInitializer.properties.getProperty("esp_res_consumer");
    /** 游客角色*/
    public static String GUEST = LifeCircleApplicationInitializer.properties.getProperty("esp_guest");
    
    /**
     * 校验roleid参数是否合法
     * @param roleId
     * @param message
     * @author lanyl
     */
    public void checkValidRoleId(String roleId, String message){
        if(!(UcRoleClient.SUPERADMIN.equals(roleId) 
        || UcRoleClient.COVERAGEADMIN.equals(roleId) 
        || UcRoleClient.RESCREATOR.equals(roleId) 
        || UcRoleClient.CATEGORYDATAADMIN.equals(roleId) 
        || UcRoleClient.RESCONSUMER.equals(roleId) 
        || UcRoleClient.GUEST.equals(roleId))){
            throw new LifeCircleException(HttpStatus.BAD_REQUEST,
                LifeCircleErrorMessageMapper.InvalidArgumentsError.getCode(), 
                message + LifeCircleErrorMessageMapper.InvalidArgumentsError.getMessage());
        }
    }
    
    /**
     * 获取最大角色       
     * @param userInfo
     * @return
     * @author lianggz
     */
    public UserCenterRoleDetails getMaxRole(UserInfo userInfo) {
        List<UserCenterRoleDetails> userCenterRoleDetailList = userInfo.getUserRoles();
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 超级管理员
            if(SUPERADMIN.equals(userCenterRoleDetail.getRoleId())){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 库管理员
            if(COVERAGEADMIN.equals(userCenterRoleDetail.getRoleId())){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 资源创建者角色
            if(RESCREATOR.equals(userCenterRoleDetail.getRoleId())){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 维度管理者角色
            if(CATEGORYDATAADMIN.equals(userCenterRoleDetail.getRoleId())){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 资源消费者角色
            if(RESCONSUMER.equals(userCenterRoleDetail.getRoleId())){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 游客角色
            if(GUEST.equals(userCenterRoleDetail.getRoleId())){
                return userCenterRoleDetail;
            }
        }
        return null;
    }
}