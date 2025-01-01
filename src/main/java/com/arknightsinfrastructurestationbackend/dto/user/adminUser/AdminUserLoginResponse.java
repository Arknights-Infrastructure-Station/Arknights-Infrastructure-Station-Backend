package com.arknightsinfrastructurestationbackend.dto.user.adminUser;

import com.arknightsinfrastructurestationbackend.dto.info.adminUser.AdminUserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.commonUser.CommonUserLoginResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminUserLoginResponse extends CommonUserLoginResponse {
    private AdminUserInfo adminUserInfo;
}
