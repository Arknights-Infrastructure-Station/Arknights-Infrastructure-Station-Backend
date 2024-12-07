package com.arknightsinfrastructurestationbackend.global.type;

import lombok.Getter;

@Getter
public enum UserType {
    ADMIN_USER("AdminUser"),
    COMMON_USER("CommonUser");

    private final String name;

    UserType(String name) {
        this.name = name;
    }
}
