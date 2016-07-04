package nd.esp.service.lifecycle.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nd.esp.service.lifecycle.app.LifeCircleApplicationInitializer;
import nd.esp.service.lifecycle.support.interceptors.RequestWrapper;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nd.gaea.rest.filter.TokenAuthenticationProcessFilter;
import com.nd.gaea.rest.security.authentication.PreAuthenticatedAuthenticationExtractorManager;


public class TokenAuthenticationProcessFilter4LC extends
		TokenAuthenticationProcessFilter {
	
    /** 权限启用开关*/
    public static String AUTHORITY_ENABLE = LifeCircleApplicationInitializer.properties.getProperty("esp_authority_enable");
    
	public TokenAuthenticationProcessFilter4LC(
			AuthenticationManager authenticationManager,
			PreAuthenticatedAuthenticationExtractorManager extractorManager) {
		super(authenticationManager, extractorManager);
	}


	/*
	 * 说明：删除NDR_MAC代码，与徐震宇确认，该代码已废弃。
	 * 理由：这个过滤器，将UC的角色域置空，导致UC角色失效。
	 * 修改备注：@lanyl 2016-07-01
	 */
	/**
	 *
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 * @history lanyl 注释代码（已确认可以注释）
	 */
	//@Override
	/*public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		String authorization = request.getHeader("Authorization");
		//如果是NDR_MAC过来，则不走UC认证
		if(authorization != null && authorization.startsWith("NDR_MAC")){
			SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
			chain.doFilter(request, response);
		}else if("GET".equals(request.getMethod()) && (authorization == null || !request.getRequestURI().contains("downloadurl"))){
			SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
			chain.doFilter(request, response);
			super.doFilterInternal(request, response, chain);
		}else{
			super.doFilterInternal(request, response, chain);
		}
	}*/
	
	/**
     * 重写过滤器
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     * @author lanyl
     */
    /*@Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain chain) throws IOException, ServletException {
        // 缓存request body，解决request.getInputStream()只能获取一次的问题。
        HttpServletRequest requestWrapper = null;
        if(request instanceof HttpServletRequest) {
            requestWrapper = new RequestWrapper((HttpServletRequest) request);
        }
        if(requestWrapper == null) {
            super.doFilterInternal(request, response, chain);
        } else {
            super.doFilterInternal(requestWrapper, response, chain);
        }
    }*/
	
	/**
     * 重写过滤器
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     * @author lanyl
     */
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
	    
	    /*
	     * 说明：设置开关，NDR_MAC代码，在权限开启前进行兼容。
	     * 理由：这个过滤器，将UC的角色域置空，导致UC角色失效。
	     * 修改备注：@lanyl 2016-07-01
	     */
	    
        // 根据开关判断是否执行权限机制
        if(Boolean.valueOf(AUTHORITY_ENABLE)){
            // 缓存request body，解决request.getInputStream()只能获取一次的问题。
            HttpServletRequest requestWrapper = null;
            if(request instanceof HttpServletRequest) {
                requestWrapper = new RequestWrapper((HttpServletRequest) request);
            }
            if(requestWrapper == null) {
                super.doFilterInternal(request, response, chain);
            } else {
                super.doFilterInternal(requestWrapper, response, chain);
            }
        }else{
            // 兼容NDR_MAC，后面NDR_MAC建立权限后，可以废弃删除。
            String authorization = request.getHeader("Authorization");
            //如果是NDR_MAC过来，则不走UC认证
            if(authorization != null && authorization.startsWith("NDR_MAC")){
                SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
                chain.doFilter(request, response);
            }/*else if("GET".equals(request.getMethod()) && (authorization == null || !request.getRequestURI().contains("downloadurl"))){
                SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
                chain.doFilter(request, response);
                super.doFilterInternal(request, response, chain);
            }*/
            else{
                super.doFilterInternal(request, response, chain);
            }
        }  
    }

	

}
