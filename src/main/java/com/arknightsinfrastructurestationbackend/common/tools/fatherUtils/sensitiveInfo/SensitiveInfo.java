package com.arknightsinfrastructurestationbackend.common.tools.fatherUtils.sensitiveInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 脱敏注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SensitiveInfo {
    int start() default 0;
    int end() default 0;
}