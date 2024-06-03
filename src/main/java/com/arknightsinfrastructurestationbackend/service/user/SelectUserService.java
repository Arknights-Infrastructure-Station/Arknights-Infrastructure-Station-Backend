package com.arknightsinfrastructurestationbackend.service.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.mapper.user.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SelectUserService {
    private final UserMapper userMapper;

    public User getUserByToken(String token) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getToken, token);
            return userMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            Log.error("数据库查询错误：" + e);
            return null;
        }
    }

    public User getUserByEmail(String email) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, email);
            return userMapper.selectOne(queryWrapper);
        } catch (Exception e) {
            Log.error("数据库查询错误：" + e);
            return null;
        }
    }
}
