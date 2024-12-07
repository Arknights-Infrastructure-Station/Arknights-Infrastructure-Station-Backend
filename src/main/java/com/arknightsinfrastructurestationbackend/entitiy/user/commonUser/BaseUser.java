package com.arknightsinfrastructurestationbackend.entitiy.user.commonUser;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
public abstract class BaseUser implements UserDetails {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id; // 用户ID，自动生成

    private String username; // 用户名

    private String email; // 邮箱

    private String password; // 密码

    private String status; // 用户状态（正常、封禁）

    private String token; // 登录凭证

    @Override
    public boolean isAccountNonExpired() {
        // 账户永不过期
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 如果状态为"ban"，则账户被锁定
        return !"ban".equals(this.status);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 凭证（密码）永不过期
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 根据状态判断账户是否启用
        return "normal".equals(this.status);
    }

    // 子类需要实现此方法以定义具体的权限
    @Override
    public abstract Collection<? extends GrantedAuthority> getAuthorities();
}