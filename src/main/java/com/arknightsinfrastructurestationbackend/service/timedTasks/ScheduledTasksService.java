package com.arknightsinfrastructurestationbackend.service.timedTasks;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.mapper.user.UploadStagingWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.UploadWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.integration.WorkFileIntegrationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final String environment = System.getenv("ARKNIGHTS_RUNTIME_ENVIRONMENT");

    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final WorkFileMapper workFileMapper;
    private final UploadWorkFileCountMapper uploadWorkFileCountMapper;
    private final UploadStagingWorkFileCountMapper uploadStagingWorkFileCountMapper;
    private final WorkFileIntegrationService workFileIntegrationService;
    private final MowerBucketService mowerBucketService;

    // 每天执行一次的定时任务

    /**
     * 删除过期作业
     */
    @Scheduled(cron = "0 0 0 * * ?")  // 每天凌晨执行
    public void removeExpiredWorkFiles() {
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();

        // 定义MySQL datetime格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String nowAsString = now.format(formatter);

        // 查找所有clearTime到期的作业
        List<RecyclingWorkFile> expiredWorkFiles = recyclingWorkFileMapper.selectList(new LambdaQueryWrapper<RecyclingWorkFile>()
                .le(RecyclingWorkFile::getClearTime, nowAsString));

        // 删除这些作业
        for (RecyclingWorkFile recyclingWorkFile : expiredWorkFiles) {
            workFileMapper.deleteById(recyclingWorkFile.getId());
        }
    }

    /**
     * 清除用户上传作业记录次数
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetUploadCount() {
        uploadWorkFileCountMapper.truncateTable();
        uploadStagingWorkFileCountMapper.truncateTable();
    }

    /**
     * 清除用户上传作业记录次数
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearRedundantKeys() {
        //该值与Linux服务器/etc/profile中的值保持一致
        if (environment != null && !environment.isBlank() && "production".equals(environment)) {
            try {
                mowerBucketService.deleteRedundantKeys(workFileIntegrationService.getAllPictureKeys());
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
        }
    }
}

