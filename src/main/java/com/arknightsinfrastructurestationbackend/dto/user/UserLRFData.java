package com.arknightsinfrastructurestationbackend.dto.user;

import lombok.Data;

@Data
//用户前端登录/注册/忘记密码表单数据对象
public class UserLRFData {
    private String ipAddress;

    private String email;

    private String password;

    private String verificationCode; //默认是4为验证码
}
