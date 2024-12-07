package com.arknightsinfrastructurestationbackend.entitiy.commonUser;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
