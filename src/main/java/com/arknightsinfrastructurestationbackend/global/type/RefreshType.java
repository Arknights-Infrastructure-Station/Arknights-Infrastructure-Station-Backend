package com.arknightsinfrastructurestationbackend.global.type;

import lombok.Getter;

@Getter
public enum RefreshType {
    DSS("downloadAndStarAndScore"); //指定刷新下载量、收藏量和评分

//    PK("pictureKey"); //指定刷新图片存储key

    private final String value;

    RefreshType(String value) {
        this.value = value;
    }
}
