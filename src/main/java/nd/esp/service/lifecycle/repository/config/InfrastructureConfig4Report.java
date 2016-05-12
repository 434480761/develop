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

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hibernate.ejb.HibernatePersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Common infrastructure configuration class to setup a Spring container and
 * infrastructure components like a {@link DataSource}, a
 * {@link EntityManagerFactory} and a {@link PlatformTransactionManager}. Will
 * be used by the configuration activating the plain JPA based repository
 * configuration (see {@link PlainJpaConfig}) as well as the Spring Data JPA
 * based one (see {@link ApplicationConfig}).
 * 
 * @author Oliver Gierke
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "reportEntityManagerFactory",
    transactionManagerRef = "reportTransactionManager"
)
public class InfrastructureConfig4Report {
	@Resource
	private Environment env;
	
	public final static String HIBERNATE_CONFIG = "sdkdb/hibernate_report.properties";


	/**
	 * Sets up a {@link LocalContainerEntityManagerFactoryBean} to use
	 * Hibernate. Activates picking up entities from the project's base package.
	 * 
	 * @return
	 */
	@Autowired
	@Bean(name="reportEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			@Qualifier(value="reportDataSource") DataSource dataSource, @Qualifier(value="reportProp")Properties hibProperties) {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean
				.setPersistenceProviderClass(HibernatePersistence.class);
		entityManagerFactoryBean
				.setPackagesToScan("nd.esp.service.lifecycle.repository.model.report");
		
		entityManagerFactoryBean.setJpaProperties(hibProperties);

		return entityManagerFactoryBean;
	}
	
	@Bean(name="reportTransactionManager")
	public PlatformTransactionManager transactionManager(@Qualifier(value="reportEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory
				.getObject());
		return transactionManager;
	}
	
	@Bean(name="reportProp")
	public Properties hibProperties() throws IOException {
		org.springframework.core.io.Resource resource = new ClassPathResource(HIBERNATE_CONFIG);
		Properties properties = PropertiesLoaderUtils.loadProperties(resource);
		return properties;
	}

	@Autowired
	@Bean
	public TransactionTemplate getTransactionTemplate(@Qualifier(value="reportDataSource") DataSource dataSource,@Qualifier(value="reportProp") Properties hibProperties){

		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean=entityManagerFactory(dataSource, hibProperties);

		PlatformTransactionManager manager =transactionManager(localContainerEntityManagerFactoryBean);
		TransactionTemplate transactionTemplate= new TransactionTemplate(manager);

		return transactionTemplate;
	}
	
	@Autowired
	@Bean(name="reportJdbcTemplate")
	public JdbcTemplate reportJdbcTemplate(@Qualifier(value="reportDataSource") DataSource dataSource){
		return new JdbcTemplate(dataSource);
	}
	
	@Autowired
	@Bean(name="reportTransactionTemplate")
	public TransactionTemplate getreportTransactionTemplate(@Qualifier(value="reportDataSource") DataSource dataSource, @Qualifier(value="reportProp")Properties hibProperties){

		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean=entityManagerFactory(dataSource, hibProperties);

		PlatformTransactionManager manager =transactionManager(localContainerEntityManagerFactoryBean);
		TransactionTemplate transactionTemplate= new TransactionTemplate(manager);

		return transactionTemplate;
	}
}
