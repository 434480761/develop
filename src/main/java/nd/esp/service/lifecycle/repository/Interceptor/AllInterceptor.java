package nd.esp.service.lifecycle.repository.Interceptor;

import com.nd.gaea.rest.security.services.WafUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>Title: Intercepor to get org id from authentication info.         </p>
 * <p>Description: Function Description </p>
 * <p>Copyright: Copyright (c) 2015     </p>
 * <p>Company: ND Co., Ltd.       </p>
 * <p>Create Time: 2015年7月1日           </p>
 * @author Jawinton
 */
@Component
public class AllInterceptor implements HandlerInterceptor {

    @Autowired
    private WafUserDetailsService wafUserDetailsService;
    

        
    /**
     * 拦截器
     * @author Jawinton
     * @history
     */
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 获取头部信息

		System.out.println(authentication.isAuthenticated());

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
}
