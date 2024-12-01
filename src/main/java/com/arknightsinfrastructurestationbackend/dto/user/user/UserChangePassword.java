package com.arknightsinfrastructurestationbackend.dto.user.user;

import lombok.Data;

@Data
public class UserChangePassword {
    private String oldPassword;
    private String newPassword;
}
