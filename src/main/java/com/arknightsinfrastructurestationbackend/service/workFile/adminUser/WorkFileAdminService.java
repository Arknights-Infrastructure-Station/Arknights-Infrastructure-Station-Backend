package com.arknightsinfrastructurestationbackend.service.workFile.adminUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
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
public class WorkFileAdminService {
    private final WorkFileMapper workFileMapper;
    private final MowerBucketService mowerBucketService;
    private final CommonService commonService;
    private final AdapterService adapterService;

    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult insertWorkFile(WorkFile workFile) throws ServiceException, JsonProcessingException {
        workFile.setReleaseDate(commonService.getCurrentDateTime());
        workFile.setScore(-1f);

        if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(workFile.getFileContent());
            workFile.setFileContent(key);
        }

        // 替换图片存储数组字段
        workFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(workFile.getDescriptionPictures()));

        if (workFileMapper.insert(workFile) > 0) {
            return new OperateResult(200, "作业创建成功");
        } else {
            return new OperateResult(500, "作业创建失败");
        }
    }

    public OperateResult updateWorkFile(WorkFile workFile) throws JsonProcessingException {
        WorkFile existingWorkFile = workFileMapper.selectById(workFile.getId());

        if (existingWorkFile == null) {
            return new OperateResult(404, "作业不存在");
        }

        // 更新字段
        existingWorkFile.setName(workFile.getName());
        existingWorkFile.setType(workFile.getType());
        existingWorkFile.setLayout(workFile.getLayout());
        existingWorkFile.setDescription(workFile.getDescription());

        existingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(workFile.getDescriptionPictures()));

        existingWorkFile.setStorageType(workFile.getStorageType());
        if (StorageType.PICTURE_KEY.getValue().equals(existingWorkFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(workFile.getFileContent());
            existingWorkFile.setFileContent(key);
        } else {
            existingWorkFile.setFileContent(workFile.getFileContent());
        }

        existingWorkFile.setFileRequest(workFile.getFileRequest());
        existingWorkFile.setAuthor(workFile.getAuthor());
        existingWorkFile.setAuthorId(workFile.getAuthorId());
        existingWorkFile.setReleaseDate(commonService.getCurrentDateTime());

        if (workFileMapper.updateById(existingWorkFile) > 0) {
            return new OperateResult(200, "作业更新成功");
        } else {
            return new OperateResult(500, "作业更新失败");
        }
    }

    public OperateResult deleteWorkFile(Long wid) throws JsonProcessingException {
        WorkFile workFile = workFileMapper.selectById(wid);
        if (workFile != null) {
            // 检查是否已webp图片格式存储作业文件
            if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType())) {
                // 删除图片
                mowerBucketService.removeSingleWebP(workFile.getFileContent());
                mowerBucketService.removeMultipleWebP(workFile.getDescriptionPictures());
            }

            if (workFileMapper.deleteById(wid) > 0) {
                return new OperateResult(200, "作业删除成功");
            } else {
                return new OperateResult(500, "作业删除失败");
            }
        } else {
            return new OperateResult(404, "未找到指定的作业");
        }
    }

    public List<WorkFile> screenWorkFileList(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<WorkFile> queryWrapper = adapterService.createAdminUserLimitedQueryWrapper(workFileAdminScreen, WorkFile.class);
        return workFileMapper.selectList(queryWrapper);
    }

    public Long getWorkFileListCount(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<WorkFile> queryWrapper = adapterService.createAdminUserQueryWrapper(workFileAdminScreen, WorkFile.class);
        return workFileMapper.selectCount(queryWrapper);
    }
}

