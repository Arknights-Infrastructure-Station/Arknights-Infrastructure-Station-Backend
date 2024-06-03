package com.arknightsinfrastructurestationbackend.service.timedTasks;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arknightsinfrastructurestationbackend.entitiy.user.UploadWorkFileCount;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.mapper.user.UploadWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduledTasksService {

    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final WorkFileMapper workFileMapper;
    private final UploadWorkFileCountMapper uploadWorkFileCountMapper;

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
        LambdaQueryWrapper<UploadWorkFileCount> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(UploadWorkFileCount::getId); //条件始终为真，意味着删除所有数据
        uploadWorkFileCountMapper.delete(queryWrapper);
    }
}

