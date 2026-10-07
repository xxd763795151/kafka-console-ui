package com.xuxd.kafka.console.config;

import com.xuxd.kafka.console.filter.AuthFilter;
import com.xuxd.kafka.console.filter.ContextSetFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xuxd
 * @date: 2026/6/15 00:00
 **/
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistration(AuthFilter authFilter) {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("auth-filter");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<ContextSetFilter> contextSetFilterRegistration(ContextSetFilter contextSetFilter) {
        FilterRegistrationBean<ContextSetFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(contextSetFilter);
        registration.addUrlPatterns(
                "/acl/*",
                "/user/*",
                "/cluster/*",
                "/config/*",
                "/consumer/*",
                "/message/*",
                "/topic/*",
                "/op/*",
                "/client/*"
        );
        registration.setOrder(100);
        registration.setName("context-set-filter");
        return registration;
    }
}
