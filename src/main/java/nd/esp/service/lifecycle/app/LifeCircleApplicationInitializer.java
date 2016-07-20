package nd.esp.service.lifecycle.app;

import java.io.IOException;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.nd.gaea.rest.AbstractWafWebApplicationInitializer;
import com.nd.gaea.util.WafJsonMapper;


/**
 * @title
 * @Desc TODO
 * @author liuwx
 * @version 1.0
 * @update 2015年3月9日 下午7:54:38
 * @updateContent 加入全局读取配置文件类properties
 */
public class LifeCircleApplicationInitializer extends
		AbstractWafWebApplicationInitializer {
	private final static Logger LOG= LoggerFactory.getLogger(LifeCircleApplicationInitializer.class);


	public static Properties properties = null;
	public static Properties message_properties=null;
	public static Properties worker_properties=null;
	public static Properties props_properties_es=null;
	public static Properties props_properties_db=null;
	public static Properties tablenames_properties=null;
	public static Properties ndCode_properties=null;
	

	static {
		try {
			properties = PropertiesLoaderUtils
					.loadAllProperties("system.properties");
			message_properties = PropertiesLoaderUtils
					.loadAllProperties("config/message/exception_message.properties");
			worker_properties = PropertiesLoaderUtils
			        .loadAllProperties("config/worker/worker.properties");
			props_properties_es = PropertiesLoaderUtils
                    .loadAllProperties("config/props/resource_props_range_es.properties");
			props_properties_db = PropertiesLoaderUtils
			        .loadAllProperties("config/props/resource_props_range_db.properties");
			tablenames_properties = PropertiesLoaderUtils
			        .loadAllProperties("config/props/restype_corresponding_tablename.properties");
			ndCode_properties = PropertiesLoaderUtils
			        .loadAllProperties("config/props/ndcode_corresponding_tablename.properties");
		} catch (IOException e) {

			LOG.warn("加载配置文件失败", e);
		}
	}
	

	@Override
	public void onStartup(ServletContext servletContext)
			throws ServletException {
		/*
		 * waf内置支持
		 * LOG.info("lifecycle onStartup");
		servletContext.addFilter("corsFilter", new CORSFilter());
		*/
	    /*AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();  
        ctx.register(LifeCircleWebConfig.class);  
        ctx.setServletContext(servletContext);    
        servletContext.addListener(new ContextLoaderListener(ctx));*/
		
		WafJsonMapper.getMapper().setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
        servletContext.addListener(new RequestContextListener());
        
        super.onStartup(servletContext);
	}
	
	/**
     * 
    * @Title: initCharacterEncodingFilter 
    * @Description:  字符编码过滤器
    * @param @param servletContext    设定文件 
    * @return void    返回类型 
    * @throws
     */
    protected void initCharacterEncodingFilter(ServletContext servletContext) {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setForceEncoding(true);
        characterEncodingFilter.setEncoding("UTF-8");
        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter("characterEncodingFilter", characterEncodingFilter);
        filterRegistration.setAsyncSupported(isAsyncSupported());
        filterRegistration.addMappingForUrlPatterns(getDispatcherTypes(), false,  "/*");
    }


/*	@Override
	public void initUcConfig() {
		WafUcConfig config = new WafUcConfig();
		config.setUC_API_ACCESS_USERNAME("830917");
		config.setUC_API_VERSION("v0.6");
		config.setUC_API_ACCESS_PASSWORD("80fba977d063a6f7262a8a9c95f61140");
		WafUCServerAuthenService serverAuth = new WafUcServerAuthenServiceImpl();
		WafContext.configUc(config, serverAuth);

	}*/

	@Override
	protected Class<?>[] getRootConfigClasses() {
		// TODO Auto-generated method stub
		return new Class[] { LifeCircleSecurityConfig.class,
				LifeCycleEspStoreConfig.class,LifeCycleRedisCacheConfig.class };
	}

	@Override
	protected Class<?>[] getServletConfigClasses() {
		// TODO Auto-generated method stub
		return new Class[] { LifeCircleWebConfig.class};
	}


	
	@Override
	protected void customizeRegistration(Dynamic registration) {
		registration.setAsyncSupported(true);
	}


}
