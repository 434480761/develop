package nd.esp.service.lifecycle.app;


import com.nd.gaea.client.http.BearerAuthorizationProvider;
import com.nd.gaea.client.support.DeliverBearerAuthorizationProvider;
import com.nd.gaea.rest.config.WafWebMvcConfigurerAdapter;
import nd.esp.service.lifecycle.educommon.services.impl.CommonServiceHelper;
import nd.esp.service.lifecycle.repository.config.ApplicationConfig;
import nd.esp.service.lifecycle.support.interceptors.RoleResInterceptor;
import nd.esp.service.lifecycle.support.annotation.impl.MethodArgumentsLengthResolver;
import nd.esp.service.lifecycle.support.busi.PackageUtil;
import nd.esp.service.lifecycle.support.busi.TransCodeUtil;
import nd.esp.service.lifecycle.support.busi.elasticsearch.EsClientSupport;
import nd.esp.service.lifecycle.support.busi.titan.GremlinClientFactory;
import nd.esp.service.lifecycle.utils.JDomUtils;
import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "nd.esp.service.lifecycle.repository" ,"nd.esp.service.lifecycle.controllers",
		"nd.esp.service.lifecycle.services","nd.esp.service.lifecycle.daos","nd.esp.service.lifecycle.utils",
		"nd.esp.service.lifecycle.support","nd.esp.service.lifecycle.educommon",
		"nd.esp.service.lifecycle.security"})
@Import(ApplicationConfig.class)
@EnableAspectJAutoProxy
@EnableScheduling
//@PropertySource("classpath:config/worker.properties")
@PropertySource(value = {"classpath:sdkdb/c3p0-config-main.properties","classpath:system.properties"})
public class LifeCircleWebConfig extends WafWebMvcConfigurerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(LifeCircleWebConfig.class);

	//@Value("${db.driver}")
	 //private String driver;
	 @Autowired
	 private Environment env;

	@Autowired
	private RoleResInterceptor roleResInterceptor;
	/**
	 * 加载配置属性文件
	 * @return
	 */
/*	@Bean
	public  PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {

		PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
		propertyPlaceholderConfigurer.setFileEncoding("utf-8");
		propertyPlaceholderConfigurer.setLocation(new ClassPathResource("config/worker.properties"));
		try {
            propertyPlaceholderConfigurer.setProperties(PropertiesLoaderUtils
                    .loadAllProperties("config/worker.properties"));
        } catch (IOException e) {
            LOG.error("加载配置文件失败",e);
            e.printStackTrace();
        }
		//propertyPlaceholderConfigurer.setLocation(new ClassPathResource("META-INF/app.properties"));
		return propertyPlaceholderConfigurer;
	}*/
	/**	
	 * @desc:新的加载配置文件  
	 * <p>需要申明静态来处理,支持多个配置文件</p>
	 * @createtime: 2015年6月29日 
	 * @author: liuwx 
	 * @see Environment
	 * @return
	 */
	@Bean
	public static  PropertySourcesPlaceholderConfigurer  getPropertySourcesPlaceholderConfigurer() {
	    PropertySourcesPlaceholderConfigurer placeholderConfigurer=new PropertySourcesPlaceholderConfigurer();
	    return placeholderConfigurer;
	}
	
	/*
	 * 使用ResourceBundleMessageSource来代替
	 * @Bean
	public ReloadableResourceBundleMessageSource getReloadableResourceBundleMessageSource() {
		ReloadableResourceBundleMessageSource source =new ReloadableResourceBundleMessageSource();
		source.setUseCodeAsDefaultMessage(true);//支持用户自定义的message
		source.setCacheSeconds(10);
		source.setBasename("WEB-INF/messages");
		source.setDefaultEncoding("utf-8");
		return source;
	}*/
	
	@Bean
	public static ResourceBundleMessageSource getResourceBundleMessageSource () {
		ResourceBundleMessageSource  source =new ResourceBundleMessageSource ();
		source.setUseCodeAsDefaultMessage(true);//如果找不到属性值,则使用key作为值返回
		//source.setCacheSeconds(10);
		source.setBasenames(new String[]{"config/valid/messages"});
		source.setDefaultEncoding("utf-8");
		source.setFallbackToSystemLocale(true);
		//LOG.info(source.getMessage("model.href.value.errormsg",   null, Locale.SIMPLIFIED_CHINESE));
		return source;
	}
	
	@Bean
	public LocalValidatorFactoryBean getLocalValidatorFactoryBean() {
		LocalValidatorFactoryBean factoryBean =new LocalValidatorFactoryBean();
		factoryBean.setValidationMessageSource(getResourceBundleMessageSource());
		return factoryBean;
	}
	
	/**
	 * @desc  自定义校验器
	 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#getValidator()
	 * @author liuwx
	 */
	@Override
	public Validator getValidator() {
		
		return getLocalValidatorFactoryBean();
	}
	
	/**
     * 加载jdomutil
     * @return
     */
    @Bean
    public JDomUtils getJDomUtils() {

        JDomUtils domUtils =new JDomUtils();
        
       return domUtils;
    }

    /**
     * 加载CommonServiceHelper
     * 
     * @return
     * @since
     */
    @Bean
    public CommonServiceHelper getCommonServiceHelper() {

        CommonServiceHelper commonServiceHelper = new CommonServiceHelper();

        return commonServiceHelper;
    }
    
    /**
     * 加载elasticsearch client
     * 
     * @return
     * @since
     */
    @Bean
    public Client getClient(){
    	return EsClientSupport.getClient();
    }
    
    //titan
    @Bean
	public org.apache.tinkerpop.gremlin.driver.Client getGremlinClient(
			GremlinClientFactory gremlinClientFactory) {
		return gremlinClientFactory.getGremlinClient();
	}
    /**
     * 加载packageUtil
     * @return
     */
    @Bean
    public PackageUtil getPackageUtil() {
        
        PackageUtil packageUtil =new PackageUtil();
        
        return packageUtil;
    }
    
    /**
     * 加载TaskQueryTimerTask
     * @return
     */
    @Bean
    public TransCodeUtil getTransCodeUtil() {
        TransCodeUtil transCodeUtil=new TransCodeUtil();
        return transCodeUtil;
    }
    
    @Override
	public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
		configurer.setDefaultTimeout(30*1000L);
	}
	
    @Bean
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter adapter=new RequestMappingHandlerAdapter();
		adapter.setSynchronizeOnSession(true);
		List<HandlerMethodArgumentResolver>argumentResolvers=new ArrayList<>();
		argumentResolvers.add(new MethodArgumentsLengthResolver());
		adapter.setCustomArgumentResolvers(argumentResolvers);

        return adapter;
    }
    
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(roleResInterceptor);
		super.addInterceptors(registry);
		//registry.addWebRequestInterceptor()
	}

	@Bean
	@Primary
	public BearerAuthorizationProvider bearerAuthorizationProvider() {
		return new DeliverBearerAuthorizationProvider();
	}
}
