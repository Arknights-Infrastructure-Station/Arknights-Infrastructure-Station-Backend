package com.arknightsinfrastructurestationbackend.entitiy.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("`upload_staging_work_file_count`")
public class UploadStagingWorkFileCount {
    private Long id; //用户ID
    private Integer count;
}
