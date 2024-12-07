package com.arknightsinfrastructurestationbackend.dto.user.commonUser;

import lombok.Data;

@Data
public class UserChangePassword {
    private String oldPassword;
    private String newPassword;
}
