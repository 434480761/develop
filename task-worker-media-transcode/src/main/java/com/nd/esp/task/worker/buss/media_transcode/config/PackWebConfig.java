package com.nd.esp.task.worker.buss.media_transcode.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.nd.gaea.rest.config.WafWebMvcConfigurerAdapter;

@Configuration()
@EnableWebMvc
@ComponentScan(basePackages = { "com.nd.esp.task.worker.buss","com.nd.esp.task.worker.container"})
public class PackWebConfig extends WafWebMvcConfigurerAdapter {
    // implements SchedulingConfigurer

    private final Log LOG = LogFactory.getLog(PackWebConfig.class);

    /*
     * @Bean public WafUcServerConfig getWafUcServerConfig(){ WafUcServerConfig wafUcServerConfig = new
     * WafUcServerConfig(); //可以自定义连接UC的账号、密码、url以及版本号等
     * wafUcServerConfig.setUC_API_DOMAIN(LifeCircleApplicationInitializer.properties.getProperty("esp_uc_api_domain"));
     * wafUcServerConfig
     * .setUC_API_VERSION(LifeCircleApplicationInitializer.properties.getProperty("esp_uc_api_version"));
     * wafUcServerConfig
     * .setUC_API_ACCESS_USERNAME(LifeCircleApplicationInitializer.properties.getProperty("esp_uc_api_access_username"
     * )); wafUcServerConfig.setUC_API_ACCESS_PASSWORD(LifeCircleApplicationInitializer.properties.getProperty(
     * "esp_uc_api_access_password")); wafUcServerConfig.setWafUcServerConfig(); return wafUcServerConfig; }
     */

    /**
     * 加载配置属性文件
     * @return
     */
    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertyPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertyPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setFileEncoding("utf-8");
        // propertyPlaceholderConfigurer.setLocation(new ClassPathResource("system.properties"));
        //propertyPlaceholderConfigurer.setLocation(new ClassPathResource("config/system.properties"));
        propertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        propertyPlaceholderConfigurer.setIgnoreResourceNotFound(true);
        return propertyPlaceholderConfigurer;
    }
    /**
     * 加载配置属性文件
     * @return
     */
    // @Bean
    // public JDomUtils getJDomUtils() {
    //
    // JDomUtils jDomUtils=new JDomUtils();
    // return jDomUtils;
    // }

    // @Bean
    // public HeartbeatSchedul getHeartbeatSchedul(){
    // HeartbeatSchedul heartbeatSchedul =new HeartbeatSchedul();
    // return heartbeatSchedul;
    // }
    /*
     * @Bean public ScheduledAnnotationBeanPostProcessor getScheduledAnnotationBeanPostProcessor(){
     * ScheduledAnnotationBeanPostProcessor scheduledAnnotationBeanPostProcessor=new
     * ScheduledAnnotationBeanPostProcessor(); scheduledAnnotationBeanPostProcessor.setScheduler(getHeartbeatSchedul());
     * return scheduledAnnotationBeanPostProcessor; }
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.scheduling.annotation.SchedulingConfigurer#configureTasks(org.springframework.scheduling.
     * config.ScheduledTaskRegistrar)
     */
    /*
     * @Override public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
     * taskRegistrar.setScheduler(taskExecutor()); taskRegistrar.setScheduler(getHeartbeatSchedul()); try {
     * taskRegistrar.addCronTask(new ScheduledMethodRunnable(new HeartbeatSchedul(),"executeTask"),"0/5 * * * * *"); }
     * catch (NoSuchMethodException e) { // TODO Auto-generated catch block e.printStackTrace(); }
     * 
     * }
     * 
     * @Bean(destroyMethod="shutdown") public Executor taskExecutor() { return Executors.newScheduledThreadPool(100); }
     */
    @Bean()
    public RestTemplate  getRestTemplate(){
        RestTemplate restTemplate=new RestTemplate();
        return restTemplate;
    }
   

}
