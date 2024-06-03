package com.arknightsinfrastructurestationbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)  // 启用 CGLIB 代理
public class AopConfig {
}
