package nd.esp.service.lifecycle.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class LcAuthenticationFilter extends GenericFilterBean {
//	private AuthenticationManager authenticationManager;
//	
//	public AuthenticationManager getAuthenticationManager() {
//		return authenticationManager;
//	}
//
//	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
//		this.authenticationManager = authenticationManager;
//	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		//Authentication successAuthentication = authenticationManager.authenticate(new LcAuthenticationToken());
        SecurityContextHolder.getContext().setAuthentication(new LcAuthenticationToken());
		chain.doFilter(request, response);
	}
}
