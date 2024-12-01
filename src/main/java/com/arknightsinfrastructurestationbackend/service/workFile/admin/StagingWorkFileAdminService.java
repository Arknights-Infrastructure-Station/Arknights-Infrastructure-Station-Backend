package com.arknightsinfrastructurestationbackend.service.workFile.admin;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.admin.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.StagingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class StagingWorkFileAdminService {
    private final StagingWorkFileMapper stagingWorkFileMapper;
    private final MowerBucketService mowerBucketService;
    private final CommonService commonService;
    private final AdapterService adapterService;

    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult insertStagingWorkFile(StagingWorkFile stagingWorkFile) throws ServiceException, JsonProcessingException {
        stagingWorkFile.setStagingDate(commonService.getCurrentDateTime());

        if (StorageType.PICTURE_KEY.getValue().equals(stagingWorkFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(stagingWorkFile.getFileContent());
            stagingWorkFile.setFileContent(key);
        }

        // 替换图片存储数组字段
        stagingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(stagingWorkFile.getDescriptionPictures()));

        if (stagingWorkFileMapper.insert(stagingWorkFile) > 0) {
            return new OperateResult(200, "暂存作业创建成功");
        } else {
            return new OperateResult(500, "暂存作业创建失败");
        }
    }

    public OperateResult updateStagingWorkFile(StagingWorkFile stagingWorkFile) throws JsonProcessingException {
        StagingWorkFile existingStagingWorkFile = stagingWorkFileMapper.selectById(stagingWorkFile.getId());

        if (existingStagingWorkFile == null) {
            return new OperateResult(404, "暂存作业不存在");
        }

        // 更新字段
        existingStagingWorkFile.setName(stagingWorkFile.getName());
        existingStagingWorkFile.setType(stagingWorkFile.getType());
        existingStagingWorkFile.setLayout(stagingWorkFile.getLayout());
        existingStagingWorkFile.setDescription(stagingWorkFile.getDescription());

        existingStagingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(stagingWorkFile.getDescriptionPictures()));

        existingStagingWorkFile.setStorageType(stagingWorkFile.getStorageType());
        if (StorageType.PICTURE_KEY.getValue().equals(existingStagingWorkFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(stagingWorkFile.getFileContent());
            existingStagingWorkFile.setFileContent(key);
        } else {
            existingStagingWorkFile.setFileContent(stagingWorkFile.getFileContent());
        }

        existingStagingWorkFile.setFileRequest(stagingWorkFile.getFileRequest());
        existingStagingWorkFile.setAuthor(stagingWorkFile.getAuthor());
        existingStagingWorkFile.setAuthorId(stagingWorkFile.getAuthorId());
        existingStagingWorkFile.setStagingDate(commonService.getCurrentDateTime());

        if (stagingWorkFileMapper.updateById(existingStagingWorkFile) > 0)
            return new OperateResult(200, "暂存作业更新成功");
        else
            return new OperateResult(500, "暂存作业更新失败");
    }

    public OperateResult deleteStagingWorkFile(Long wid) throws JsonProcessingException {
        StagingWorkFile stagingWorkFile = stagingWorkFileMapper.selectById(wid);
        if (stagingWorkFile != null) {
            // 检查是否已webp图片格式存储作业文件
            if (StorageType.PICTURE_KEY.getValue().equals(stagingWorkFile.getStorageType())) {
                // 删除图片
                mowerBucketService.removeSingleWebP(stagingWorkFile.getFileContent());
                mowerBucketService.removeMultipleWebP(stagingWorkFile.getDescriptionPictures());
            }

            if (stagingWorkFileMapper.deleteById(wid) > 0) {
                return new OperateResult(200, "暂存作业删除成功");
            } else {
                return new OperateResult(500, "暂存作业删除失败");
            }
        } else {
            return new OperateResult(404, "未找到指定的暂存作业");
        }
    }

    public List<StagingWorkFile> screenStagingWorkFileList(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<StagingWorkFile> queryWrapper = adapterService.createAdminLimitedQueryWrapper(workFileAdminScreen, StagingWorkFile.class);
        return stagingWorkFileMapper.selectList(queryWrapper);
    }

    public Long getStagingWorkFileListCount(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<StagingWorkFile> queryWrapper = adapterService.createAdminQueryWrapper(workFileAdminScreen, StagingWorkFile.class);
        return stagingWorkFileMapper.selectCount(queryWrapper);
    }
}

