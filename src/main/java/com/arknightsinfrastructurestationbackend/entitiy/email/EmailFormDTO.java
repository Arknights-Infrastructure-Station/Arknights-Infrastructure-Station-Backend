package com.arknightsinfrastructurestationbackend.entitiy.email;

import lombok.Data;

@Data
public class EmailFormDTO {
    private String from; //发信者
    private String to; //收信者
    private String subject; //邮箱主题
    private String text; //邮箱内容
}
