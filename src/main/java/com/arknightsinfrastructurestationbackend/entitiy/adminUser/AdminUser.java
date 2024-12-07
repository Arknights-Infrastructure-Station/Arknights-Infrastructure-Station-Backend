package com.arknightsinfrastructurestationbackend.entitiy.adminUser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@TableName("`admin_user_table`")
public class AdminUser implements UserDetails {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id; // 管理员用户id，自动增长

    private String username; // 用户名，管理员账号初次生成的默认名称

    private String email; // 邮箱

    private String password; // 密码

    private String status; // 用户状态（正常、封禁）

    private String token; // 登录凭证

    private String avatar; // 用户头像链接

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 创建具有 "ADMIN" 角色的 GrantedAuthority 对象
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN");

        // 返回包含该角色的集合
        return Collections.singletonList(authority);
    }

    @Override
    public boolean isAccountNonExpired() {
        // 定义账户是否过期
        // true表示账户永不过期
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 定义账户是否被锁定
        // 如果status等于"封禁"，返回false
        return !"ban".equals(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 定义凭证（密码）是否过期
        // true表示密码永不过期
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 定义账户是否启用
        // 根据status字段判断账户是否启用
        return "normal".equals(this.status);
    }
}

