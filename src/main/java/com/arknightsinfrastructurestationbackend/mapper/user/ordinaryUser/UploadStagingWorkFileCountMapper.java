package com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.UploadStagingWorkFileCount;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadStagingWorkFileCountMapper extends BaseMapper<UploadStagingWorkFileCount> {
    @Update("TRUNCATE TABLE upload_staging_work_file_count")
    void truncateTable();
}
