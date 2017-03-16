package com.lg.configuration;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Created by kanghong.zhao on 2016-11-7.
 */
@Configuration
public class DruidCfg {

    @Bean
    public ServletRegistrationBean druidServlet(@Value("${druid.loginUsername}") String loginUsername, @Value("${druid.loginPassword}") String loginPassword){
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setServlet(new StatViewServlet());
        reg.addUrlMappings("/druid/*");
        //reg.addInitParameter("allow", "127.0.0.1");
        //reg.addInitParameter("deny","");
        if(loginUsername != null && loginPassword != null){
            reg.addInitParameter("loginUsername", loginUsername);
            reg.addInitParameter("loginPassword", loginPassword);
        }
        return reg;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        return filterRegistrationBean;
    }

}
