package com.arknightsinfrastructurestationbackend.entitiy.user.adminUser;

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
@TableName("`admin_user_table`")
public class AdminUser extends BaseUser {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 分配 "ROLE_ADMIN" 权限
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");
        return Collections.singletonList(authority);
    }
}

