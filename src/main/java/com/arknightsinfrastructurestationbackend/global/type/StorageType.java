package com.arknightsinfrastructurestationbackend.global.type;

public enum StorageType {
    TEXT("text"),
    PICTURE_KEY("pictureKey");

    private final String value;

    StorageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
