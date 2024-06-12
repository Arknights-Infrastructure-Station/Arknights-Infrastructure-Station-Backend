package com.arknightsinfrastructurestationbackend.service.integration;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.StagingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@AllArgsConstructor
public class WorkFileIntegrationService {
    private final WorkFileMapper workFileMapper;
    private final StagingWorkFileMapper stagingWorkFileMapper;
    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final CommonService commonService;
    private final ObjectMapper objectMapper=new ObjectMapper();

    /**
     * 获取所有作业的图片key
     * @return List<String> 所有作业的图片key
     */
    public List<String> getAllPictureKeys() throws JsonProcessingException {
        List<String> keys=new ArrayList<>();

        List<WorkFile> workFileList = workFileMapper.selectList(null);
        List<StagingWorkFile> stagingWorkFileList = stagingWorkFileMapper.selectList(null);
        List<RecyclingWorkFile> recyclingWorkFileList = recyclingWorkFileMapper.selectList(null);

        //作业文件载入
        for (WorkFile workFile : workFileList) {
            if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType()))
                keys.add(workFile.getFileContent());
            String[] pictureKeys = commonService.convertStringArray(workFile.getDescriptionPictures());
            keys.addAll(Arrays.asList(pictureKeys));
        }

        //暂存作业文件载入
        for (StagingWorkFile stagingWorkFile : stagingWorkFileList) {
            if (StorageType.PICTURE_KEY.getValue().equals(stagingWorkFile.getStorageType()))
                keys.add(stagingWorkFile.getFileContent());
            String[] pictureKeys = commonService.convertStringArray(stagingWorkFile.getDescriptionPictures());
            keys.addAll(Arrays.asList(pictureKeys));
        }

        //待回收作业文件载入
        for (RecyclingWorkFile recyclingWorkFile : recyclingWorkFileList) {
            if (StorageType.PICTURE_KEY.getValue().equals(recyclingWorkFile.getStorageType()))
                keys.add(recyclingWorkFile.getFileContent());
            String[] pictureKeys = commonService.convertStringArray(recyclingWorkFile.getDescriptionPictures());
            keys.addAll(Arrays.asList(pictureKeys));
        }

        return keys;
    }
}
