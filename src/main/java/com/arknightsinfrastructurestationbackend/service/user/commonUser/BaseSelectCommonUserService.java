package com.arknightsinfrastructurestationbackend.service.user.commonUser;

import com.arknightsinfrastructurestationbackend.entitiy.user.commonUser.BaseUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseSelectCommonUserService<T extends BaseUser> {
    protected abstract BaseMapper<T> getMapper();

    protected abstract LambdaQueryWrapper<T> createQueryWrapper();

    /**
     * 通用查询方法
     *
     * @param column 查询字段的 Lambda 表达式
     * @param value  查询值
     * @return 查询结果
     */
    private T getByField(SFunction<T, ?> column, Object value) {
        try {
            LambdaQueryWrapper<T> queryWrapper = createQueryWrapper();
            queryWrapper.eq(column, value);
            return getMapper().selectOne(queryWrapper);
        } catch (Exception e) {
            log.error("数据库查询错误: {}", e.getMessage(), e);
            return null;
        }
    }

    public T getByToken(String token) {
        return getByField(T::getToken, token);
    }

    public T getByEmail(String email) {
        return getByField(T::getEmail, email);
    }
}
