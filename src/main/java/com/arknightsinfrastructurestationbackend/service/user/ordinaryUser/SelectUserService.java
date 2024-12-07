package com.arknightsinfrastructurestationbackend.service.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.User;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.UserMapper;
import com.arknightsinfrastructurestationbackend.service.user.commonUser.BaseSelectCommonUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SelectUserService extends BaseSelectCommonUserService<User> {
    private final UserMapper userMapper;

    @Override
    protected BaseMapper<User> getMapper() {
        return userMapper;
    }

    @Override
    protected LambdaQueryWrapper<User> createQueryWrapper() {
        return new LambdaQueryWrapper<>();
    }
}
