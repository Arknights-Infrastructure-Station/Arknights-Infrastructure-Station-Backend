package com.arknightsinfrastructurestationbackend.service.user.adminUser;

import com.arknightsinfrastructurestationbackend.entitiy.user.adminUser.AdminUser;
import com.arknightsinfrastructurestationbackend.mapper.user.adminUser.AdminUserMapper;
import com.arknightsinfrastructurestationbackend.service.user.commonUser.BaseSelectCommonUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SelectAdminUserService extends BaseSelectCommonUserService<AdminUser> {
    private final AdminUserMapper adminUserMapper;

    @Override
    protected BaseMapper<AdminUser> getMapper() {
        return adminUserMapper;
    }

    @Override
    protected LambdaQueryWrapper<AdminUser> createQueryWrapper() {
        return new LambdaQueryWrapper<>();
    }
}
