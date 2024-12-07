package com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 作业下载记录
 */
@Data
@TableName("`download_list`")
@AllArgsConstructor
public class DownloadRecord {
    private Long wid;
    private Long uid;
}
