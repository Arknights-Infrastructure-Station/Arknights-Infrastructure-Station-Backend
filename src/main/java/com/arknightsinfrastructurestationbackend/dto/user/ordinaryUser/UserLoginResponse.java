package com.arknightsinfrastructurestationbackend.dto.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.dto.info.UserInfo;
import lombok.Data;

@Data
public class UserLoginResponse {
    private String message;
    private UserInfo userInfo;
}
