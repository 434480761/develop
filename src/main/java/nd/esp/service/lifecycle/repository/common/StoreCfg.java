package nd.esp.service.lifecycle.repository.common;

/**
 * 
 */

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * 
 * 项目名字:nd esp<br>
 * 类描述:读取配置工具类<br>
 * 创建人:wengmd<br>
 * 创建时间:2015年2月6日<br>
 * 修改人:<br>
 * 修改时间:2015年2月6日<br>
 * 修改备注:<br>
 * 
 * @version 0.2<br>
 */
public class StoreCfg {
	private static final Logger logger = LoggerFactory
			.getLogger(StoreCfg.class);
	private static final String cfgFile = "sdkdb/esp_store_cfg.properties";
	private static StoreCfg storeCfg = null;

	private String version = "0.1";

	private long cacheExpireTime = 60L;
	
	public static StoreCfg getInstance() {
		if (storeCfg != null)
			return storeCfg;
		synchronized (StoreCfg.class) {
			if (storeCfg != null)
				return storeCfg;
			storeCfg = new StoreCfg();
			try {
				storeCfg.initProp();
			} catch (IOException e) {
			    
			    if (logger.isErrorEnabled()) {
                    
			        logger.error("StoreCfg getInstance:{}", e);
			        
                }
				        
			}
		}
		return storeCfg;
	}
	
	public void initProp() throws IOException {	
	    
	    if (logger.isDebugEnabled()) {
            
	        logger.debug("cfg path:{}", cfgFile);
	        
        }
		        
		Resource resource = new ClassPathResource("/"+cfgFile);
		Properties prop = PropertiesLoaderUtils.loadProperties(resource);
		version = prop.getProperty("version");
		
		cacheExpireTime = Long.parseLong(prop.getProperty("cache_expire_time", "60"));
		
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getCacheExpireTime() {
		return cacheExpireTime;
	}

	public void setCacheExpireTime(long cacheExpireTime) {
		this.cacheExpireTime = cacheExpireTime;
	}
}
