package com.arknightsinfrastructurestationbackend.config;

import com.arknightsinfrastructurestationbackend.config.filter.RateLimitingFilter;
import com.arknightsinfrastructurestationbackend.config.filter.RequestSizeFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    /**
     * 请求大小限制
     */
    @Bean
    public FilterRegistrationBean<RequestSizeFilter> requestSizeFilter(){
        FilterRegistrationBean<RequestSizeFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new RequestSizeFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    /**
     * 请求次数限制
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitingFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
