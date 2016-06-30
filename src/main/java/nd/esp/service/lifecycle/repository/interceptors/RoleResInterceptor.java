package nd.esp.service.lifecycle.repository.interceptors;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.RoleResFilterUrlMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;
import com.nd.gaea.rest.security.services.WafUserDetailsService;

/**
 * <p>Title: RoleResInterceptor         </p>
 * <p>Description: RoleResInterceptor </p>
 * <p>Copyright: Copyright (c) 2015     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2016年06月30日           </p>
 * @author lanyl
 * @author lianggz
 */
@Component
public class RoleResInterceptor implements HandlerInterceptor {

    @Autowired
    private WafUserDetailsService wafUserDetailsService;
    
    /** 超级管理员*/
    private static String SUPERADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_super_admin");
    /** 库管理员*/
    private static String COVERAGEADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_coverage_admin");
    /** 资源创建者角色*/
    private static String RESCREATOR = LifeCircleApplicationInitializer.properties.getProperty("esp_res_creator");
    /** 维度管理者角色*/
    private static String CATEGORYDATAADMIN = LifeCircleApplicationInitializer.properties.getProperty("esp_category_data_admin");
    /** 资源消费者角色*/
    private static String RESCONSUMER = LifeCircleApplicationInitializer.properties.getProperty("esp_res_consumer");
    /** 游客角色*/
    private static String GUEST = LifeCircleApplicationInitializer.properties.getProperty("esp_guest");
    
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    UserInfo userInfo = (UserInfo)authentication.getPrincipal();
	    String userId = userInfo.getUserId();
	    UserCenterRoleDetails userCenterRoleDetails = this.getMaxRole(userInfo);

	    switch (userCenterRoleDetails.getRoleId()) {
            case COVERAGEADMIN: // 库管理员
                // 过滤访问的URL
                isCoverageadminMatcher(request);      
                break;
            case RESCREATOR:    // 资源创建者角色
                isRescreatorMatcher(request);
                break;
            case RESCONSUMER:    // 资源消费者角色
                isResconsumerMatcher(request);
                break;    
            default:
                break;
        }
	    
	    
        return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
    }
	
	/**
	 * 是否库管理员匹配
	 */
	private void isCoverageadminMatcher(HttpServletRequest request){
	    for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getCoverageAdminMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            String method = request.getMethod();
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + method);
            if (matcher.find()) {
                // 
                if(ImmutableList.<String>of("POST","PUT","DELETE").contains(method)){
                    RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
                    String body = requestWrapper.getBody();
                    JSONObject json = JSONObject.parseObject(body);
                    JSONObject categories = json.getJSONObject("categories");
                    // 如果coverage不空的情况下，进行判断
                    if(categories!=null){
                        // 获取target_type
                        String targetType = categories.getString("target_type");
                        // 获取target
                        String target = categories.getString("target");
                        // 获取coverage
                        String coverage  = targetType + "/" + target;
                        if(StringUtils.isNotBlank(coverage)){
                            //TODO:如果不相等，报错
                        }
                        
                    }
                }
                // 
                else if(ImmutableList.<String>of("GET").contains(method)){
                    String coverage = request.getParameter("coverage");
                    // 如果coverage不空的情况下，进行判断
                    if(StringUtils.isNotBlank(coverage)){
                        //TODO:如果不相等，报错
                    }
                }
                
            }
        }
	}
	
	/**
     * 是否资源创建者角色匹配
     */
    private void isRescreatorMatcher(HttpServletRequest request){
        for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getResCreatorMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            String method = request.getMethod();
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + method);
            if (matcher.find()) {
                // 
                if(ImmutableList.<String>of("POST","PUT","DELETE").contains(method)){
                    String coverage = request.getParameter("categories");
                    // 如果coverage不空的情况下，进行判断
                    if(StringUtils.isNotBlank(coverage)){
                        // 获取target_type
                        
                        // 获取target
                        
                    }
                }
                // 
                else if(ImmutableList.<String>of("GET").contains(method)){
                    String coverage = request.getParameter("coverage");
                    // 如果coverage不空的情况下，进行判断
                    if(StringUtils.isNotBlank(coverage)){
                        //TODO:如果不相等，报错
                    }
                }
                
            }
        }
    }
    
    /**
     * 是否资源创建者角色匹配
     */
    private void isResconsumerMatcher(HttpServletRequest request){
        for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getResConsumerMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            String method = request.getMethod();
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + method);
            if (matcher.find()) {
                // 
                if(ImmutableList.<String>of("POST","PUT","DELETE").contains(method)){
                    String coverage = request.getParameter("categories");
                    // 如果coverage不空的情况下，进行判断
                    if(StringUtils.isNotBlank(coverage)){
                        // 获取target_type
                        
                        // 获取target
                        
                    }
                }
                // 
                else if(ImmutableList.<String>of("GET").contains(method)){
                    String coverage = request.getParameter("coverage");
                    // 如果coverage不空的情况下，进行判断
                    if(StringUtils.isNotBlank(coverage)){
                        //TODO:如果不相等，报错
                    }
                }
                
            }
        }
    }
	
	
	/**
     * 获取最大角色       
     * @param userInfo
     * @return
     */
	private UserCenterRoleDetails getMaxRole(UserInfo userInfo) {
	    List<UserCenterRoleDetails> userCenterRoleDetailList = userInfo.getUserRoles();
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 超级管理员
            if(userCenterRoleDetail.getRoleId().equals(SUPERADMIN)){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 库管理员
            if(userCenterRoleDetail.getRoleId().equals(COVERAGEADMIN)){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 资源创建者角色
            if(userCenterRoleDetail.getRoleId().equals(RESCREATOR)){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 维度管理者角色
            if(userCenterRoleDetail.getRoleId().equals(CATEGORYDATAADMIN)){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 资源消费者角色
            if(userCenterRoleDetail.getRoleId().equals(RESCONSUMER)){
                return userCenterRoleDetail;
            }
        }
        for(UserCenterRoleDetails userCenterRoleDetail: userCenterRoleDetailList){
            // 游客角色
            if(userCenterRoleDetail.getRoleId().equals(GUEST)){
                return userCenterRoleDetail;
            }
        }
        throw new LifeCircleException(HttpStatus.FORBIDDEN,
                LifeCircleErrorMessageMapper.Forbidden.getCode(), LifeCircleErrorMessageMapper.Forbidden.getMessage());
    }
}
