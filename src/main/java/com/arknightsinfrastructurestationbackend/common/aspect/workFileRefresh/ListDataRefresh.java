package com.arknightsinfrastructurestationbackend.common.aspect.workFileRefresh;

import com.arknightsinfrastructurestationbackend.global.type.RefreshType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ListDataRefresh {
    RefreshType[] value();
}
