package com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.UploadWorkFileCount;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadWorkFileCountMapper extends BaseMapper<UploadWorkFileCount> {
    @Update("TRUNCATE TABLE upload_work_file_count")
    void truncateTable();
}
