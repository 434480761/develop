package nd.esp.service.lifecycle.support.interceptors;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.nd.gaea.rest.security.authens.UserCenterRoleDetails;
import com.nd.gaea.rest.security.authens.UserInfo;
import com.nd.gaea.rest.security.services.WafUserDetailsService;
import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.services.usercoveragemapping.v06.UserCoverageMappingService;
import nd.esp.service.lifecycle.services.userrestypemapping.v06.UserRestypeMappingService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Autowired
    private UserCoverageMappingService userCoverageMappingService;

    @Autowired
    private UserRestypeMappingService userRestypeMappingService;

    
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
        // 无鉴权处理
        UserInfo userInfo = (UserInfo)authentication.getPrincipal();
        if(userInfo != null ){
            UserCenterRoleDetails userCenterRoleDetails = this.getMaxRole(userInfo);

            // 库管理员
            if( COVERAGEADMIN.equals(userCenterRoleDetails.getRoleId()) ){
                // 过滤访问的URL
                isCoverageadminMatcher(request, userInfo.getUserId());
            }
            // 资源创建者角色
            else if( RESCREATOR.equals(userCenterRoleDetails.getRoleId()) ){
                // 过滤访问的URL
                isRescreatorMatcher(request, userInfo.getUserId());
            }
            // 资源消费者角色
            else if( RESCONSUMER.equals(userCenterRoleDetails.getRoleId()) ){
                // 过滤访问的URL
                isResconsumerMatcher(request, userInfo.getUserId());
            }
            return true;
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
     * @param request
     * @param userId
     * @throws IOException
     * @author lanyl
     */
    private void isCoverageadminMatcher(HttpServletRequest request, String userId) throws IOException {
	    for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getCoverageAdminMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            String method = request.getMethod();
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + method);
            // 进行coverage权限验证
            this.isCoverageMatch(matcher, method, userId, request);
        }
	}

	/**
     * 是否资源创建者角色匹配
     * @param request
     * @param userId
     * @throws IOException
     * @author lanyl
     */
    private void isRescreatorMatcher(HttpServletRequest request, String userId) throws IOException{
        for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getResCreatorMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            String method = request.getMethod();
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + method);
            // 进行coverage权限验证
            this.isCoverageMatch(matcher, method, userId, request);
            // 进行resType权限验证
            this.isResTypeMatch(matcher, userId);
        }
    }

	/**
     * 是否资源创建者角色匹配
     * @param request
     * @param userId
     * @throws IOException
     * @author lanyl
     */
    private void isResconsumerMatcher(HttpServletRequest request, String userId) throws IOException{
        for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getResConsumerMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            String method = request.getMethod();
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + method);
            // 进行coverage权限验证
            this.isCoverageMatch(matcher, method, userId, request);
            // 进行resType权限验证
            this.isResTypeMatch(matcher, userId);
        }
    }


    /**
     * 判断url请求的ResType是否存在
     * @param matcher
     * @param userId
     * @throws IOException
     * @author lanyl
     */
    private void isResTypeMatch(Matcher matcher, String userId){
        if (matcher.find()) {
            String resType = matcher.group(1);
            if(StringUtils.isNotBlank(resType)){
                // 获取当前用户允许访问的resType列表
                List<String> userRestypelList = this.userRestypeMappingService.findUserRestypeList(userId);
                // resType如果不存在列表中，表示没有权限访问。 报错
                if( !userRestypelList.contains(resType) ){
                    throw new LifeCircleException(HttpStatus.FORBIDDEN,
                            LifeCircleErrorMessageMapper.Forbidden.getCode(), LifeCircleErrorMessageMapper.Forbidden.getMessage());
                }
            }
        }
    }

	/**
     * 判断url请求的coverage是否存在
     * @param matcher
     * @param method
     * @param userId
     * @param request
     * @throws IOException
     * @author lianggz
     */
	private void isCoverageMatch(Matcher matcher, String method, String userId, HttpServletRequest request) throws IOException{
        if (matcher.find()) {
            //post,put,delete请求，获取categories.target_type和categories.target值
            if(ImmutableList.<String>of("POST","PUT","DELETE").contains(method)){
                RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
                String body = requestWrapper.getBody();
                JSONObject json = JSONObject.parseObject(body);
                JSONObject categories = json.getJSONObject("categories");
                // 如果coverage不空的情况下，进行判断
                if(categories != null && categories.size() > 0){
                    // 获取target_type
                    String targetType = categories.getString("target_type");
                    // 获取target
                    String target = categories.getString("target");
                    // 获取coverage
                    String coverage  = targetType + "/" + target;
                    if(StringUtils.isNotBlank(coverage)){
                        // 获取用户的覆盖范围列表,如果不存在, 则没有权限 报错
                        List<String> userCoverageList = this.userCoverageMappingService.findUserCoverageList(userId);
                        if(!userCoverageList.contains(coverage)){
                            throw new LifeCircleException(HttpStatus.FORBIDDEN,
                                    LifeCircleErrorMessageMapper.Forbidden.getCode(), LifeCircleErrorMessageMapper.Forbidden.getMessage());
                        }
                    }
                }
            }
            //get请求 获取coverage参数值
            else if(ImmutableList.<String>of("GET").contains(method)){
                String coverage = request.getParameter("coverage");
                // 如果coverage不空的情况下，进行判断
                if(StringUtils.isNotBlank(coverage)){
                    // 获取用户的覆盖范围列表,如果不存在, 则没有权限 报错
                    List<String> userCoverageList = this.userCoverageMappingService.findUserCoverageList(userId);
                    if(!userCoverageList.contains(coverage)){
                        throw new LifeCircleException(HttpStatus.FORBIDDEN,
                                LifeCircleErrorMessageMapper.Forbidden.getCode(), LifeCircleErrorMessageMapper.Forbidden.getMessage());
                    }
                }
            }
        }
    }


	/**
     * 获取最大角色       
     * @param userInfo
     * @return
     * @author lianggz
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
