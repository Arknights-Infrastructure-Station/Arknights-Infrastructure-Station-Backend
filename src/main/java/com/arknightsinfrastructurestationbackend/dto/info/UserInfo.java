package com.arknightsinfrastructurestationbackend.dto.info;

import com.arknightsinfrastructurestationbackend.common.tools.fatherUtils.sensitiveInfo.SensitiveData;
import com.arknightsinfrastructurestationbackend.common.tools.fatherUtils.sensitiveInfo.SensitiveInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false) //计算哈希值和互相比较时不需要考虑父类属性
@Data
public class UserInfo extends SensitiveData {

    private String id;

    private String username;

    @SensitiveInfo(start = 5, end = 5) //保留首尾各5个字符，其余字符进行脱敏处理
    private String email;

    private String avatar;

    private String operators;

    private String infrastructure;
}
