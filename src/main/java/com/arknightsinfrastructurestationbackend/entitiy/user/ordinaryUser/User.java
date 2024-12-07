package com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.entitiy.user.commonUser.BaseUser;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("`user_table`")
public class User extends BaseUser {

    private String avatar; // 用户头像链接

    private String operators; // 干员养成练度

    private String infrastructure; // 基建排布配置

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 分配 "ROLE_USER" 权限
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return Collections.singletonList(authority);
    }
}
