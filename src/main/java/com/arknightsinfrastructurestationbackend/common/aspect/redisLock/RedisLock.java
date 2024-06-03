package com.arknightsinfrastructurestationbackend.common.aspect.redisLock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {
    String key();
    long waitTime() default 10; //默认最多等待10秒
    long leaseTime() default 3; //默认最长持有锁3秒
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}