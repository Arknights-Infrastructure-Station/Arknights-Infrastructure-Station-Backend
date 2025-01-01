package com.arknightsinfrastructurestationbackend.dto.info.ordinaryUser;

import com.arknightsinfrastructurestationbackend.dto.info.commonUser.CommonUserInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true) //计算哈希值和互相比较时不需要考虑父类属性
@Data
public class UserInfo extends CommonUserInfo {
    private String avatar;

    private String operators;

    private String infrastructure;
}
