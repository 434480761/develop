/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nd.esp.service.lifecycle.repository.config;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import nd.esp.service.lifecycle.repository.sdk.impl.MyRepositoryFactoryBean;
import nd.esp.service.lifecycle.repository.sdk.impl.ServicesManager;

/**
 * JavaConfig class to enable Spring Data JPA repositories. Re-using common infrastrcuture configuration from
 * {@link InfrastructureConfig}.
 * 
 * @author Oliver Gierke
 */
@Configuration
@ComponentScan(basePackages = { "nd.esp.service.lifecycle.repository.v02"})
@EnableJpaRepositories(basePackages="nd.esp.service.lifecycle.repository.sdk",repositoryFactoryBeanClass = MyRepositoryFactoryBean.class)
@ImportResource("classpath*:spring/applicationContext.xml")
@Import({InfrastructureConfig.class,InfrastructureConfig4Question.class})
public class ApplicationConfig {
	 private static final Logger logger = LoggerFactory
				.getLogger(ApplicationConfig.class);
	public final static String DBCONFIG = "sdkdb/dbconfig.properties"; 
	
	@Bean
	public ServicesManager servicesManager() {
		return new ServicesManager();
	}
	
	@Bean
	public DbConfig dbConfig() throws IOException {
		DbConfig config = new DbConfig();
		org.springframework.core.io.Resource resource = new ClassPathResource(DBCONFIG);
		Properties properties = PropertiesLoaderUtils.loadProperties(resource);
		config.setDriver(properties.getProperty("jdbc.driver"));
		config.setUrl(properties.getProperty("jdbc.url"));
		config.setUsername(properties.getProperty("jdbc.username"));
		config.setPassword(properties.getProperty("jdbc.password"));
		return config;
	}
	
	/**
	 * 加载配置属性文件
	 * @return
	 */
	@Bean
	public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {

		PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
		propertyPlaceholderConfigurer.setLocation(new ClassPathResource("sdkdb/c3p0-config-main.properties"));

		return propertyPlaceholderConfigurer;
	}
}
