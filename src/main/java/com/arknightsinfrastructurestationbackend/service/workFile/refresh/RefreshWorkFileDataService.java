package com.arknightsinfrastructurestationbackend.service.workFile.refresh;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.DownloadRecordService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.UserRateService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.ordinaryUser.StaredWorkFileService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RefreshWorkFileDataService {

    private final WorkFileMapper workFileMapper;
    private final StaredWorkFileService staredWorkFileService;
    private final DownloadRecordService downloadRecordService;
    private final UserRateService userRateService;

    /**
     * 刷新每个作业的收藏数、下载数和评分，只有List<WorkFile>会调用
     *
     * @param workFileList 要刷新的作业列表
     * @return 刷新后的作业列表
     */
    public List<WorkFile> refreshWorkFileData(List<WorkFile> workFileList) {
        //刷新每个作业的收藏数和下载数
        for (int i = 0; i < workFileList.size(); i++) {
            WorkFile workFile = workFileList.get(i);
            workFile.setStarNumber(staredWorkFileService.getStarNumberForWork(workFile.getId()));
            workFile.setDownloadNumber(downloadRecordService.getDownloadNumberForWork(workFile.getId()));
            workFile.setScore(userRateService.getScoreForWork(workFile.getId()));
            workFileMapper.updateById(workFile);
            workFileList.set(i, workFile);
        }
        return workFileList;
    }
}
