package nd.esp.service.lifecycle.security;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
@Component
@Order(50)
public class LcAuthenticationProvider implements AuthenticationProvider {
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		return authentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication == LcAuthenticationToken.class;
	}

}
