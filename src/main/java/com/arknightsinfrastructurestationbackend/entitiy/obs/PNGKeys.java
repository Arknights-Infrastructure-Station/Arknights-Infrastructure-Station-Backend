package com.arknightsinfrastructurestationbackend.entitiy.obs;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`png_keys`")
public class PNGKeys {
    @TableField("`key`")
    private String key;
}
