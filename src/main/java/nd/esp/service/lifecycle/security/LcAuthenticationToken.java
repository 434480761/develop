package nd.esp.service.lifecycle.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class LcAuthenticationToken extends AbstractAuthenticationToken {

	private static final long serialVersionUID = 1L;

	public LcAuthenticationToken() {
		super(null);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return null;
	}

}
