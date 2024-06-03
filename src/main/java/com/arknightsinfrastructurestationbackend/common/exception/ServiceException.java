package com.arknightsinfrastructurestationbackend.common.exception;

/**
 * 自定义service层异常类
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
