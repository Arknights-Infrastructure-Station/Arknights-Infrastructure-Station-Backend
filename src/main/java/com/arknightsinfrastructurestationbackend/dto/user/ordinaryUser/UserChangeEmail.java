package com.arknightsinfrastructurestationbackend.dto.user.ordinaryUser;

import lombok.Data;

@Data
public class UserChangeEmail {
    private String newEmail;
    private String verificationCode;
}
