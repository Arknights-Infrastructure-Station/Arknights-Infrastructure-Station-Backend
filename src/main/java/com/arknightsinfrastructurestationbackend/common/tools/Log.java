package com.arknightsinfrastructurestationbackend.common.tools;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Log {

    public static void info(String message) {
        log.info(message);
    }

    public static void error(String message) {
        log.error(message);
    }
}