package com.nd.esp.task.worker.buss.document_transcode.config;

import com.nd.esp.task.worker.container.springcfg.AppConfig;
import com.nd.gaea.rest.AbstractWafWebApplicationInitializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 *<h2>配置工具类</h2>
 *
 *<p></p>
 *	
 * @author liuwx
 *
 * @since 
 *
 * @create 2015年6月8日 下午7:37:21
 */
public class PackApplicationInitializer extends AbstractWafWebApplicationInitializer {
    private final static Log LOG = LogFactory.getLog(PackApplicationInitializer.class);

    public static Properties properties = null;

    static {
        try {
            properties = PropertiesLoaderUtils.loadAllProperties("config/system.properties");
        } catch (IOException e) {
            LOG.error("加载配置文件失败", e);
        }
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        LOG.info("startup lifecycle pack project..");

        super.onStartup(servletContext);
    }

    /*
     * @Override public void initUcConfig() { WafUcConfig config = new WafUcConfig();
     * config.setUC_API_ACCESS_USERNAME("830917"); config.setUC_API_VERSION("v0.6");
     * config.setUC_API_ACCESS_PASSWORD("80fba977d063a6f7262a8a9c95f61140"); WafUCServerAuthenService serverAuth = new
     * WafUcServerAuthenServiceImpl(); WafContext.configUc(config, serverAuth);
     * 
     * }
     */

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // TODO Auto-generated method stub
        return new Class[] { PackSecurityConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // TODO Auto-generated method stub
        return new Class[] { PackWebConfig.class, AppConfig.class };
    }

    @Override
    public String getRealm() {
        // TODO Auto-generated method stub
        return "lc.service.esp.nd";
    }

}
