package com.arknightsinfrastructurestationbackend.service.user.commonUser;

import com.arknightsinfrastructurestationbackend.entitiy.user.commonUser.BaseUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseSelectCommonUserService<T extends BaseUser> {
    protected abstract BaseMapper<T> getMapper();

    protected abstract LambdaQueryWrapper<T> createQueryWrapper();

    public T getByToken(String token) {
        try {
            LambdaQueryWrapper<T> queryWrapper = createQueryWrapper();
            queryWrapper.eq(T::getToken, token);
            return getMapper().selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("数据库查询错误: {}", e.getMessage(), e);
            return null;
        }
    }

    public T getByEmail(String email) {
        try {
            LambdaQueryWrapper<T> queryWrapper = createQueryWrapper();
            queryWrapper.eq(T::getEmail, email);
            return getMapper().selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("数据库查询错误: {}", e.getMessage(), e);
            return null;
        }
    }
}
