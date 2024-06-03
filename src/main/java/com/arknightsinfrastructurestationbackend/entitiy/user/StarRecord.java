package com.arknightsinfrastructurestationbackend.entitiy.user;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Date;

/**
 * 作业收藏记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`star_list`")
public class StarRecord {
    private Long wid;
    private Long uid;
    private String starDate;
}
