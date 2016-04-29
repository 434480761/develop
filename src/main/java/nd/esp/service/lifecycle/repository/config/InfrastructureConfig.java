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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    entityManagerFactoryRef = "entityManagerFactory", 
    transactionManagerRef = "transactionManager"
)
public class InfrastructureConfig {
	private static final Logger logger = LoggerFactory
			.getLogger(InfrastructureConfig.class);

	@Resource
	private Environment env;
	
	public final static String HIBERNATE_CONFIG = "sdkdb/hibernate.properties";

	/**
	 * Bootstraps an in-memory HSQL database.
	 * 
	 * @return
	 * @throws SQLException 
	 * @see http 
	 *      ://static.springsource.org/spring/docs/3.1.x/spring-framework-reference
	 *      /html/jdbc.html#jdbc-embedded-database -support
	 */
	/*@Autowired
	@Bean(destroyMethod = "close")
	public DataSource dataSource(DbConfig dbConfig)
			throws PropertyVetoException, SQLException {

        logger.info("dataSource....");
		        
		String driver = dbConfig.getDriver();
		String url = dbConfig.getUrl();
		String username = dbConfig.getUsername();
		String password = dbConfig.getPassword();
		ComboPooledDataSource dataSource = new ComboPooledDataSource("c3p0-config.properties");
        		
		logger.info("driver:{}", driver);
		logger.info("url:{}", url);
		logger.info("username:{}", username);
        		
		dataSource.setDriverClass(driver);
		dataSource.setJdbcUrl(url);
		dataSource.setUser(username);
		dataSource.setPassword(password);
		dataSource.setMinPoolSize(5);
		dataSource.setMaxPoolSize(300);
		dataSource.setAcquireIncrement(15);
		dataSource.setMaxStatements(0);
		dataSource.setIdleConnectionTestPeriod(30);
		dataSource.setLoginTimeout(100);
		dataSource.setCheckoutTimeout(100);
		dataSource.setPreferredTestQuery("select 1");
		return dataSource;
	}*/

	/**
	 * Sets up a {@link LocalContainerEntityManagerFactoryBean} to use
	 * Hibernate. Activates picking up entities from the project's base package.
	 * 
	 * @return
	 */
	@Primary
	@Autowired
	@Bean(name="entityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			@Qualifier(value="defaultDataSource") DataSource dataSource, @Qualifier(value="defaultProp")Properties hibProperties) {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean
				.setPersistenceProviderClass(HibernatePersistence.class);
		//entityManagerFactoryBean.setPersistenceProvider(persistenceProvider);
		//entityManagerFactoryBean.setPersistenceProvider(persistenceProvider);
		entityManagerFactoryBean
				.setPackagesToScan("nd.esp.service.lifecycle.repository.model");
		
		entityManagerFactoryBean.setJpaProperties(hibProperties);

		return entityManagerFactoryBean;
	}
	
	@Bean(name="defaultProp")
	public Properties hibProperties() throws IOException {
		org.springframework.core.io.Resource resource = new ClassPathResource(HIBERNATE_CONFIG);
		Properties properties = PropertiesLoaderUtils.loadProperties(resource);
		return properties;
	}

	@Primary
	@Autowired
	@Bean(name="transactionManager")
	public PlatformTransactionManager transactionManager(@Qualifier(value="entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory
				.getObject());
		return transactionManager;
	}

	@Bean
	public SpringContextHolder springContextHolder() {
		SpringContextHolder springContextHolder = new SpringContextHolder();
		return springContextHolder;
	}
	
	@Primary
	@Autowired
	@Bean(name="defaultJdbcTemplate")
	public JdbcTemplate jdbcTemplate(@Qualifier(value="defaultDataSource") DataSource dataSource){
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		return jt;
	}
	
	@Primary
	@Autowired
	@Bean(name="transactionTemplate")
	public TransactionTemplate getTransactionTemplate(@Qualifier(value="defaultDataSource") DataSource dataSource, @Qualifier(value="defaultProp") Properties hibProperties){

		LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean=entityManagerFactory(dataSource, hibProperties);

		PlatformTransactionManager manager =transactionManager(localContainerEntityManagerFactoryBean);
		TransactionTemplate transactionTemplate= new TransactionTemplate(manager);

		return transactionTemplate;
	}
}
