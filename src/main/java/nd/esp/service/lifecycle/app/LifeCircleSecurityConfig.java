package nd.esp.service.lifecycle.app;

import nd.esp.service.lifecycle.security.LcAuthenticationProvider;
import nd.esp.service.lifecycle.security.TokenAuthenticationProcessFilter4LC;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.nd.gaea.rest.config.WafWebSecurityConfigurerAdapter;
import com.nd.gaea.rest.filter.TokenAuthenticationProcessFilter;
import com.nd.gaea.rest.security.authentication.PreAuthenticatedAuthenticationExtractorManager;
import com.nd.gaea.rest.support.WafContext;
@Configuration()
@EnableWebMvcSecurity

public class LifeCircleSecurityConfig extends WafWebSecurityConfigurerAdapter {
	
	private static final String []MODULE_HEADER_URL={"/v*/assets/**","/v*/coursewares/**","/v*/coursewareobjects/**","/v*/coursewareobjecttemplates/**"};

	//需要忽略的url mapping
	private static final String []IGNORE_URL={"/"};
	
	@Autowired
	private PreAuthenticatedAuthenticationExtractorManager extractorManager;


	//waf.security.disabled=false为生效
	@Override
	protected void onConfigure(HttpSecurity http) throws Exception {
		// TODO Auto-generated method stub
		//admin","role_biz_server"
		http.authorizeRequests()
        //匹配get方法,无需要任何身份认证
		 .antMatchers(HttpMethod.GET,"/**").permitAll()
        //匹配"/v*/**"的所有PUT操作，需要用户拥有角色"USER"
		 .antMatchers(HttpMethod.PUT).authenticated()
         //匹配"/v*/**"的所有POST操作，需要用户拥有角色"USER"
		 .antMatchers(HttpMethod.POST).authenticated()
				//匹配"/v*/**"的所有DELETE操作，需要用户拥有角色"USER"
		 .antMatchers(HttpMethod.DELETE).authenticated()
				//匹配"/students/**"的所有（其他）操作，需要用户拥有角色"ADMIN"
		  .antMatchers("/**").authenticated();

		//若其他的Url地址均需要加身份认证，则请添加.anyRequest().authenticated()

	}


	/***
	 * 忽略配置
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		//忽略的url地址
		//web.ignoring().antMatchers("/**");
		web.ignoring() .antMatchers(HttpMethod.GET, "/**").antMatchers(HttpMethod.POST,"/v0.6/*/*/statisticals");
		web.ignoring() .antMatchers(HttpMethod.POST,"/v0.6/*/*/archive");
		web.ignoring() .antMatchers(HttpMethod.POST,"/v0.6/*/*/archive_webp");
		web.ignoring() .antMatchers(HttpMethod.POST,"/v0.6/*/transcode/callback");
		web.ignoring() .antMatchers(HttpMethod.POST,"/v0.6/*/transcode/videoCallback");
		web.ignoring() .antMatchers(HttpMethod.POST,"/v0.6/*/packaging/callback");
	}
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .securityContext()
                .and()
                .addFilterAfter(genericFilterBean(),
                        SecurityContextPersistenceFilter.class).anonymous()
                .and();

        Boolean disabledSecurity = WafContext.isSecurityDisabled();
        if (!disabledSecurity) {
            this.onConfigure(http);
        }
    }	

   /**
	* 是的bearer中带上userid属性
	*@see ://doc.sdp.nd/index.php?title=WAF_rest_api%E4%B8%AD%E5%A6%82%E4%BD%95%E8%BF%9B%E8%A1%8C%E5%AE%89%E5%85%A8%E8%AE%BF%E9%97%AE%E6%8E%A7%E5%88%B6
	* */
	/*@Bean
	@Primary
	public BearerAuthorizationProvider bearerAuthorizationProvider() {
		return new DeliverBearerAuthorizationProvider();
	}

*/
    
    @Bean
    protected TokenAuthenticationProcessFilter genericFilterBean() throws Exception {
        return new TokenAuthenticationProcessFilter4LC(super.authenticationManager(),extractorManager);
    }
    
    
    //以下代码有用，只不过暂时未使用
//    @Bean
//    protected GenericFilterBean genericFilterBean() throws Exception {
//    	String authenticationFilterName = LifeCircleApplicationInitializer.properties.getProperty("authenticationFilterName");
//    	if(StringUtils.isEmpty(authenticationFilterName) || "${authentication_filter_name}".equals(authenticationFilterName)){
//    		return super.tokenAuthenticationProcessFilter();
//    	}else{
////    		AuthenticationManager am = super.authenticationManager();
//    		Class c = Class.forName(authenticationFilterName);
//    		GenericFilterBean gfb = (GenericFilterBean)c.newInstance();
////    		Method method = c.getMethod("setAuthenticationManager", AuthenticationManager.class);
////    		method.invoke(gfb, am);
//    		return gfb;
//    	}
//    }
    
    @Autowired
    private void addFilterProvider(AuthenticationManagerBuilder authenticationManagerBuilder){
    	authenticationManagerBuilder.authenticationProvider(new LcAuthenticationProvider());
    }

}