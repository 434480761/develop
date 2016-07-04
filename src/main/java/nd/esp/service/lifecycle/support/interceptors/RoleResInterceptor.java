package nd.esp.service.lifecycle.support.interceptors;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.services.usercoveragemapping.v06.UserCoverageMappingService;
import nd.esp.service.lifecycle.services.userrestypemapping.v06.UserRestypeMappingService;
import nd.esp.service.lifecycle.support.LifeCircleErrorMessageMapper;
import nd.esp.service.lifecycle.support.LifeCircleException;
import nd.esp.service.lifecycle.support.RoleResFilterUrlMap;
import nd.esp.service.lifecycle.support.uc.UcRoleClient;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
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

    @Autowired
    private UserCoverageMappingService userCoverageMappingService;

    @Autowired
    private UserRestypeMappingService userRestypeMappingService;
    
    @Autowired
    private UcRoleClient ucRoleClient;
    
    /** 权限启用开关*/
    public static String AUTHORITY_ENABLE = LifeCircleApplicationInitializer.properties.getProperty("esp_authority_enable");

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
	    
	    // 根据开关判断是否执行权限机制
	    if(Boolean.valueOf(AUTHORITY_ENABLE)){
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        // 过滤无鉴权处理
	        if(authentication != null && !"anonymousUser".equals(authentication.getPrincipal().toString())){
	            UserInfo userInfo = (UserInfo)authentication.getPrincipal();
	            if(userInfo != null){
	                UserCenterRoleDetails userCenterRoleDetails = ucRoleClient.getMaxRole(userInfo);
	                if(userCenterRoleDetails != null){
	                    // 超级管理员
	                    if( UcRoleClient.SUPERADMIN.equals(userCenterRoleDetails.getRoleId()) ){
	                        
	                    }
	                    // 库管理员
	                    else if( UcRoleClient.COVERAGEADMIN.equals(userCenterRoleDetails.getRoleId()) ){
	                        // 过滤访问的URL
	                        isCoverageadminMatcher(request, userInfo.getUserId());
	                    }
	                    // 资源创建者角色
	                    else if( UcRoleClient.RESCREATOR.equals(userCenterRoleDetails.getRoleId()) ){
	                        // 过滤访问的URL
	                        isRescreatorMatcher(request, userInfo.getUserId());
	                    }
	                    // 维度管理者角色
	                    else if( UcRoleClient.CATEGORYDATAADMIN.equals(userCenterRoleDetails.getRoleId()) ){
	                        
	                    }
	                    // 资源消费者角色
	                    else if( UcRoleClient.RESCONSUMER.equals(userCenterRoleDetails.getRoleId()) ){
	                        // 过滤访问的URL
	                        isResconsumerMatcher(request, userInfo.getUserId());
	                    }
	                    // 游客角色
	                    else if( UcRoleClient.GUEST.equals(userCenterRoleDetails.getRoleId()) ){
	                        
	                    }
	                    // 其他情况，视为异常
	                    else{
	                        return false;
	                    }
	                    return true;
	                }
	                else{
	                    return false;
	                }
	            }k
	        }
	        return true;
	    }
	    // 不走权限拦截机制
	    else{
	        return true;
	    }
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
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + request.getMethod());
            if(matcher.find()){
                // 进行coverage权限验证
                this.isCoverageMatch(request.getMethod(), userId, request);
                break;
            }
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
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + request.getMethod());
            if(matcher.find()){
                // 进行resType权限验证
                this.isResTypeMatch(matcher.group(1), userId);
                // 进行coverage权限验证
                this.isCoverageMatch(request.getMethod(), userId, request);
                break;
            }
        }
    }

	/**
     * 是否资源消费者角色匹配
     * @param request
     * @param userId
     * @throws IOException
     * @author lanyl
     */
    private void isResconsumerMatcher(HttpServletRequest request, String userId) throws IOException{
        for (Map.Entry<String, String> entry : RoleResFilterUrlMap.getResConsumerMap().entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(request.getRequestURI() + "/" + request.getMethod());
            if(matcher.find()){
                // 进行resType权限验证
                this.isResTypeMatch(matcher.group(1), userId);
                // 进行coverage权限验证
                this.isCoverageMatch(request.getMethod(), userId, request);
                break;
            }
        }
    }

    /**
     * 判断url请求的ResType是否存在
     * @param resType
     * @throws IOException
     * @author lanyl
     */
    private void isResTypeMatch(String resType, String userId){
        if(StringUtils.isNotBlank(resType)){
            // 获取当前用户允许访问的resType列表
            List<String> userRestypelList = this.userRestypeMappingService.findUserRestypeList(userId);
            // resType如果不存在列表中，表示没有权限访问报错
            if( !userRestypelList.contains(resType) ){
                throw new LifeCircleException(HttpStatus.FORBIDDEN,
                        LifeCircleErrorMessageMapper.Forbidden.getCode(), LifeCircleErrorMessageMapper.Forbidden.getMessage());
            }
        }
    }

	/**
     * 判断url请求的coverage是否存在
     * @param method
     * @param userId
     * @param request
     * @throws IOException
     * @author lianggz
     */
	private void isCoverageMatch(String method, String userId, HttpServletRequest request) throws IOException{
        //post, put, delete请求，coverages[i].target值
        if(ImmutableList.<String>of("POST", "PUT", "DELETE").contains(method)){
            // 从Request中获取请求的Body
            RequestWrapper requestWrapper = new RequestWrapper((HttpServletRequest) request);
            String body = requestWrapper.getBody();
            JSONObject json = JSONObject.parseObject(body);
            JSONArray coverages = json.getJSONArray("coverages");
            // 如果coverages不空的情况下，进行判断
            if(coverages != null && coverages.size() > 0){
                // 获取用户所拥有的公私有库列表
                List<String> userCoverageList = this.userCoverageMappingService.findUserCoverageList(userId);
                for(int i=0, num = coverages.size(); i < num; i++){
                    JSONObject coverage = coverages.getJSONObject(i);
                    if(coverage != null && coverage.size() >0){
                        // 获取target_type
                        String targetType = coverage.getString("target_type");
                        // 获取target
                        String target = coverage.getString("target");
                        // 获取coverageStr
                        String coverageStr  = targetType + "/" + target;
                        // 获取用户的覆盖范围列表, 如果不存在, 则没有权限 报错
                        if(!userCoverageList.contains(coverageStr)){
                            throw new LifeCircleException(HttpStatus.FORBIDDEN,
                                    LifeCircleErrorMessageMapper.Forbidden.getCode(), LifeCircleErrorMessageMapper.Forbidden.getMessage());
                        }
                    }
                }
            }
        }
        // get请求，获取coverage参数值
        else if(ImmutableList.<String>of("GET").contains(method)){
            // 从Request中获取请求的parameter
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