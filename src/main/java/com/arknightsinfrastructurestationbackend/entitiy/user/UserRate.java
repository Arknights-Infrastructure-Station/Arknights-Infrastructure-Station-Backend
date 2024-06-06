package com.arknightsinfrastructurestationbackend.entitiy.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户作业评分
 */
@Data
@TableName("`rate_records`")
@NoArgsConstructor
@AllArgsConstructor
public class UserRate {
    private Long uid;
    private Long wid;
    private Float score;
}