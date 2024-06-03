package com.arknightsinfrastructurestationbackend.entitiy.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@TableName("`upload_work_file_count`")
public class UploadWorkFileCount {
    private Long id; //用户ID
    private Integer count;
}
