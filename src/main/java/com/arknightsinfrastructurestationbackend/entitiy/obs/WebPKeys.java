package com.arknightsinfrastructurestationbackend.entitiy.obs;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`webp_keys`")
public class WebPKeys {
    @TableField("`key`")
    private String key;
}
