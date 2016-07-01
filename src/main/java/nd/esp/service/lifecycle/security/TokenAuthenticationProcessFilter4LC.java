package nd.esp.service.lifecycle.security;

import com.nd.gaea.rest.filter.TokenAuthenticationProcessFilter;
import com.nd.gaea.rest.security.authentication.PreAuthenticatedAuthenticationExtractorManager;
import org.springframework.security.authentication.AuthenticationManager;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenAuthenticationProcessFilter4LC extends
		TokenAuthenticationProcessFilter {
	
	public TokenAuthenticationProcessFilter4LC(
			AuthenticationManager authenticationManager,
			PreAuthenticatedAuthenticationExtractorManager extractorManager) {
		super(authenticationManager, extractorManager);
	}

	/**
	 *
	 * @param request
	 * @param response
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 * @history lanyl 注释代码（已确认可以注释）
	 */
	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		super.doFilterInternal(request, response, chain);
//		String authorization = request.getHeader("Authorization");
//		//如果是NDR_MAC过来，则不走UC认证
//		if(authorization != null && authorization.startsWith("NDR_MAC")){
//			SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
//			chain.doFilter(request, response);
//		}else if("GET".equals(request.getMethod()) && (authorization == null || !request.getRequestURI().contains("downloadurl"))){
//			SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
//			chain.doFilter(request, response);
//			super.doFilterInternal(request, response, chain);
//		}else{
//			super.doFilterInternal(request, response, chain);
//		}
	}

}
