package com.arknightsinfrastructurestationbackend.config;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${custom-data.url}")
//    private String url;

    /**
     * 允许前端请求跨域
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173", "http://arknightsinfrastructurestation.cn", "https://arknightsinfrastructurestation.cn")
                        .allowedMethods("*")
                        .allowedHeaders("*");
//                registry.addMapping("/**")
//                        .allowedOrigins("http://localhost:5173")
//                        .allowedMethods("*")
//                        .allowedHeaders("*");
            }
        };
    }
}
