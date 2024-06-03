package com.arknightsinfrastructurestationbackend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> {
}
