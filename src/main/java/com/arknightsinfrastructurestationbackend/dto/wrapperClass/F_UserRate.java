package com.arknightsinfrastructurestationbackend.dto.wrapperClass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class F_UserRate {
    private String uid;
    private String wid;
    private Float score;
}
