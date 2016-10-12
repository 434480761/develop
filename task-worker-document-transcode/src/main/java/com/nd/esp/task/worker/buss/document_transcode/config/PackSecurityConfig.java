package com.nd.esp.task.worker.buss.document_transcode.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import com.nd.gaea.rest.config.WafWebSecurityConfigurerAdapter;

@Configuration()
@EnableWebMvcSecurity
@Order(value=20)
public class PackSecurityConfig extends WafWebSecurityConfigurerAdapter {
    
    
    @Override
    protected void onConfigure(HttpSecurity http) throws Exception {
    }

    /**
     * 默认开启权限
     *
     * @param WebSecurity
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略的url地址
        web.ignoring().antMatchers("/**");
    }

}
