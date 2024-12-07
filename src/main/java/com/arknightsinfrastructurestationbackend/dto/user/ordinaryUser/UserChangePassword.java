package com.arknightsinfrastructurestationbackend.dto.user.ordinaryUser;

import lombok.Data;

@Data
public class UserChangePassword {
    private String oldPassword;
    private String newPassword;
}
