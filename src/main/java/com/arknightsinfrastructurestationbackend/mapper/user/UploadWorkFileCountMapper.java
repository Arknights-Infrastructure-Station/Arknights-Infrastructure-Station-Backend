package com.arknightsinfrastructurestationbackend.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.arknightsinfrastructurestationbackend.entitiy.user.UploadWorkFileCount;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadWorkFileCountMapper extends BaseMapper<UploadWorkFileCount> {
    @Update("TRUNCATE TABLE upload_work_file_count")
    void truncateTable();
}
