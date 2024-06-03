package com.arknightsinfrastructurestationbackend.global.type;

public enum RefreshType {
    DS("downloadAndStar"), //指定刷新下载量和收藏量
    PK("pictureKey"); //指定刷新图片存储key

    private final String value;

    RefreshType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
