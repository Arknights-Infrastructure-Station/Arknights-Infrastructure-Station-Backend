package com.arknightsinfrastructurestationbackend.service.workFile.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.StagingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser.BaseStagingWorkFileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员专用的暂存作业服务类。
 * <p>管理员可直接对暂存作业进行插入、更新、删除和筛选，不需要进行用户权限校验。</p>
 */
@Service
public class StagingWorkFileAdminService extends BaseStagingWorkFileService {

    public StagingWorkFileAdminService(StagingWorkFileMapper stagingWorkFileMapper,
                                       MowerBucketService mowerBucketService,
                                       CommonService commonService,
                                       AdapterService adapterService) {
        super(stagingWorkFileMapper, mowerBucketService, commonService, adapterService);
    }

    /**
     * 插入新的暂存作业记录（管理员操作）。
     *
     * @param stagingWorkFile 要插入的暂存作业对象
     * @return 操作结果对象，表示插入操作的成功或失败状态
     * @throws ServiceException 业务逻辑处理异常时抛出
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
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

    /**
     * 更新已存在的暂存作业记录（管理员操作）。
     * <p>无需用户权限校验。</p>
     *
     * @param stagingWorkFile 包含更新后字段信息的暂存作业对象
     * @return 操作结果对象，表示更新操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
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

    /**
     * 根据ID删除指定的暂存作业记录（管理员操作）。
     * <p>无需用户权限校验。</p>
     *
     * @param wid 要删除的暂存作业ID
     * @return 操作结果对象，表示删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult deleteStagingWorkFile(Long wid) throws JsonProcessingException {
        StagingWorkFile stagingWorkFile = getStagingWorkFile(wid);
        if (stagingWorkFile != null) {
            boolean isDeleted = deleteStagingWorkFileInternal(stagingWorkFile);
            if (isDeleted) {
                return new OperateResult(200, "暂存作业删除成功");
            } else {
                return new OperateResult(500, "暂存作业删除失败");
            }
        } else {
            return new OperateResult(404, "未找到指定的暂存作业");
        }
    }

    /**
     * 根据管理员设置的筛选条件筛选暂存作业列表。
     *
     * @param workFileAdminScreen 管理员筛选条件对象
     * @return 符合条件的暂存作业列表
     */
    public List<StagingWorkFile> screenStagingWorkFileList(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<StagingWorkFile> queryWrapper = adapterService.createAdminUserLimitedQueryWrapper(workFileAdminScreen, StagingWorkFile.class);
        return stagingWorkFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取符合管理员筛选条件的暂存作业数量。
     *
     * @param workFileAdminScreen 管理员筛选条件对象
     * @return 符合条件的暂存作业总数
     */
    public Long getStagingWorkFileListCount(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<StagingWorkFile> queryWrapper = adapterService.createAdminUserQueryWrapper(workFileAdminScreen, StagingWorkFile.class);
        return stagingWorkFileMapper.selectCount(queryWrapper);
    }
}
