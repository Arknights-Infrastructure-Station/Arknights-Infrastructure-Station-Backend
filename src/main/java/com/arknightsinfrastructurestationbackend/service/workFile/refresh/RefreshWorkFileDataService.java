package com.arknightsinfrastructurestationbackend.service.workFile.refresh;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.adapter.WorkFileInterface;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.user.DownloadRecordService;
import com.arknightsinfrastructurestationbackend.service.user.UserRateService;
import com.arknightsinfrastructurestationbackend.service.workFile.StaredWorkFileService;
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
    private final MowerBucketService mowerBucketService;

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

    /**
     * 恢复每一个存储方式为pictureKey的作业的作业内容（由key恢复为数据url），List<WorkFile>和List<StagingWorkFile>均会调用
     *
     * @param fileList 要刷新的作业列表或暂存作业列表
     * @param <W>      List<WorkFile>或List<StagingWorkFile>
     * @return 刷新后的作业列表或暂存作业列表
     */
    public <W extends WorkFileInterface> List<W> recoverPictureKey(List<W> fileList) {
        // 恢复每一个存储方式为pictureKey的作业的作业内容（由key恢复为数据url）
        for (int i = 0; i < fileList.size(); i++) {
            W file = fileList.get(i);
            if (StorageType.PICTURE_KEY.getValue().equals(file.getStorageType())) {
                file.setFileContent(mowerBucketService.syncDownloadPng(file.getFileContent()));
                fileList.set(i, file);
            }
        }
        return fileList;
    }
}
