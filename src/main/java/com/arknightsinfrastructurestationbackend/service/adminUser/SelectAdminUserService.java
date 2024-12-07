package com.arknightsinfrastructurestationbackend.service.adminUser;

import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.entitiy.adminUser.AdminUser;
import com.arknightsinfrastructurestationbackend.mapper.adminUser.AdminUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SelectAdminUserService {
    private final AdminUserMapper adminUserMapper;

    public AdminUser getAdminUserByToken(String token) {
        try {
            LambdaQueryWrapper<AdminUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AdminUser::getToken, token);
            return adminUserMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            Log.error("数据库查询错误：" + e);
            return null;
        }
    }
}
