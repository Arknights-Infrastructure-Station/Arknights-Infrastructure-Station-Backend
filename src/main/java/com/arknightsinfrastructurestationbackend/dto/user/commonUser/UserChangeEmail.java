package com.arknightsinfrastructurestationbackend.dto.user.commonUser;

import lombok.Data;

@Data
public class UserChangeEmail {
    private String newEmail;
    private String verificationCode;
}
