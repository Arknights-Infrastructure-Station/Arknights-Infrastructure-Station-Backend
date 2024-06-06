package com.arknightsinfrastructurestationbackend.global.type;

import lombok.Getter;

@Getter
public enum SortOrderType {
    RELEASE_DATE_DESC("releaseDateDesc"), //按发布日期降序
    RELEASE_DATE_ASC("releaseDateAsc"), //按发布日期升序
    SCORE_DESC("scoreDesc"), //按评分降序
    SCORE_ASC("scoreAsc"); //按评分升序

    private final String value;

    SortOrderType(String value) {
        this.value = value;
    }

}
