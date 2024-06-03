package com.arknightsinfrastructurestationbackend.entitiy.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@TableName("`user_table`")
public class User implements UserDetails {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id; //用户id，自动增长

    private String username; //用户名，用户初次注册账号时生成默认名称

    private String email; //邮箱

    private String password; //密码

    private String status; //用户状态（正常、封禁）

    private String token; //登录凭证

    private String avatar; //用户头像链接

    private String operators; //干员养成练度

    private String infrastructure; //基建排布配置


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 创建具有 "USER" 角色的GrantedAuthority对象
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");

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
