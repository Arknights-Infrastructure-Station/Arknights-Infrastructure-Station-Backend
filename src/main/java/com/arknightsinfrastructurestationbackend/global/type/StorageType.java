package com.arknightsinfrastructurestationbackend.global.type;

import lombok.Getter;

@Getter
public enum StorageType {
    TEXT("text"),
    PICTURE_KEY("pictureKey");

    private final String value;

    StorageType(String value) {
        this.value = value;
    }

}
