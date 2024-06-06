package com.arknightsinfrastructurestationbackend.global.type;

import lombok.Getter;

@Getter
public enum ScoreNumber {
    LIKE(1),
    DISLIKE(0),
    NULL(-1);

    private final float value;

    ScoreNumber(float value) {
        this.value = value;
    }
}
