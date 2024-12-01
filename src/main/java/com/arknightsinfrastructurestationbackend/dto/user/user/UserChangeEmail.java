package com.arknightsinfrastructurestationbackend.dto.user.user;

import lombok.Data;

@Data
public class UserChangeEmail {
    private String newEmail;
    private String verificationCode;
}
