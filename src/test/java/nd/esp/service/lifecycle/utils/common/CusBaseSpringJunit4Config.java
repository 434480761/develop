package nd.esp.service.lifecycle.utils.common;

import com.nd.gaea.rest.exceptions.WafErrorResolver;
import com.nd.gaea.rest.filter.ExceptionFilter;
import com.nd.gaea.rest.security.authens.UserCenterUserDetails;
import com.nd.gaea.rest.security.authens.WafUserAuthentication;
import com.nd.gaea.rest.security.services.WafUserDetailsService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.List;

/**
 * <p>Title: CusBaseSpringJunit4Config </p>
 * <p>Description: CusBaseSpringJunit4Config </p>
 * <p>Copyright: Copyright (c) 2015 </p>
 * <p>Company: ND Websoft Inc. </p>
 * <p>Create Time: 2016年07月01日 </p>
 * @author lanyl
 * @version 0.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class CusBaseSpringJunit4Config {
	protected MockMvc mockMvc;
	private static WafUserAuthentication authentication;
	protected String userId;
	protected String realm;
	@Autowired
	private WebApplicationContext webApplicationContext;
	@Autowired
	private List<WafErrorResolver> resolvers;
	@Autowired
	private WafUserDetailsService wafUserDetailsService;

	public CusBaseSpringJunit4Config() {
	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRealm() {
		return this.realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	@Before
	public void setUp() {
		ExceptionFilter exceptionFilter = new ExceptionFilter();
		exceptionFilter.setWafErrorResolvers(this.resolvers);
		this.mockMvc = ((DefaultMockMvcBuilder) MockMvcBuilders.webAppContextSetup(this.webApplicationContext).addFilters(new Filter[]{exceptionFilter})).build();
		if(authentication == null) {
			authentication = this.getPrincipal(this.getUserId(), this.getRealm());
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

	}

	protected WafUserAuthentication getPrincipal(String userId, String realm) {
		UserCenterUserDetails userCenterUserDetails = this.wafUserDetailsService.loadUserDetailsByUserIdAndRealm(userId, realm);
		WafUserAuthentication authentication = null;
		if(userCenterUserDetails !=null){
			authentication = new WafUserAuthentication((List)userCenterUserDetails.getAuthorities());
		}else {
			authentication = new WafUserAuthentication((List)null);
		}
		authentication.setDetails(userCenterUserDetails);
		authentication.setAuthenticated(true);
		return authentication;
	}
}