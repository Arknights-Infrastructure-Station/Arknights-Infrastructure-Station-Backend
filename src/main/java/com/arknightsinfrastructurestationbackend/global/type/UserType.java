package com.arknightsinfrastructurestationbackend.global.type;

import lombok.Getter;

@Getter
public enum UserType {
    ADMIN_USER("AdminUser"),
    ORDINARY_USER("OrdinaryUser");

    private final String name;

    UserType(String name) {
        this.name = name;
    }
}
