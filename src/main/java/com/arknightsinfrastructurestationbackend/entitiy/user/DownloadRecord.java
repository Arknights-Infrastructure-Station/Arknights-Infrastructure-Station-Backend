package com.arknightsinfrastructurestationbackend.entitiy.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

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
