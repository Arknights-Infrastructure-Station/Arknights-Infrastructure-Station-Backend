package com.arknightsinfrastructurestationbackend.dto.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.dto.info.ordinaryUser.UserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.commonUser.CommonUserLoginResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserLoginResponse extends CommonUserLoginResponse {
    private UserInfo userInfo;
}
