package nd.esp.service.lifecycle.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nd.gaea.rest.filter.TokenAuthenticationProcessFilter;
import com.nd.gaea.rest.security.authentication.PreAuthenticatedAuthenticationExtractorManager;
import com.nd.gaea.rest.security.authentication.mac.PreAuthenticatedMacTokenAuthentication;

public class TokenAuthenticationProcessFilter4LC extends
		TokenAuthenticationProcessFilter {
	
	public TokenAuthenticationProcessFilter4LC(
			AuthenticationManager authenticationManager,
			PreAuthenticatedAuthenticationExtractorManager extractorManager) {
		super(authenticationManager, extractorManager);
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
		String authorization = request.getHeader("Authorization");
		//如果是NDR_MAC过来，则不走UC认证
		if(authorization != null && authorization.startsWith("NDR_MAC")){
			SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
			chain.doFilter(request, response);
		}else{
			super.doFilter(req, resp, chain);
		}
	}

}
