package com.arknightsinfrastructurestationbackend.service.workFile.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.DownloadRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser.BaseRecyclingWorkFileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员专用的待回收作业服务类。
 * <p>管理员可直接恢复、删除待回收作业文件，并支持按条件筛选和计数。</p>
 */
@Slf4j
@Service
public class RecyclingWorkFileAdminService extends BaseRecyclingWorkFileService {

    public RecyclingWorkFileAdminService(RecyclingWorkFileMapper recyclingWorkFileMapper,
                                         WorkFileMapper workFileMapper,
                                         StarRecordMapper starRecordMapper,
                                         DownloadRecordMapper downloadRecordMapper,
                                         CommonService commonService,
                                         MowerBucketService mowerBucketService,
                                         AdapterService adapterService) {
        super(recyclingWorkFileMapper, workFileMapper, starRecordMapper, downloadRecordMapper, commonService, mowerBucketService, adapterService);
    }

    /**
     * 从待回收列表中恢复指定的作业文件（管理员操作）。
     *
     * @param recyclingWorkFileId 要恢复的回收作业ID
     * @return 操作结果对象，表示恢复操作的成功或失败状态
     * @throws ServiceException 当业务逻辑处理出错时抛出
     */
    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult recoverWorkFileFromRecyclingWorkFile(Long recyclingWorkFileId) throws ServiceException {
        RecyclingWorkFile recyclingWorkFile = getRecyclingWorkFile(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "作业未找到");
        }

        int deleteResult = recyclingWorkFileMapper.deleteById(recyclingWorkFile);
        WorkFile workFile = recyclingWorkFile.toWorkFile();
        workFile.setReleaseDate(commonService.getCurrentDateTime());

        int insertResult = workFileMapper.insert(workFile);

        if (deleteResult > 0 && insertResult > 0) {
            return new OperateResult(200, "作业恢复成功");
        } else {
            String moveError = "管理员于" + commonService.getCurrentDateTime() + "尝试从回收箱中恢复文件" + recyclingWorkFile.getId()
                    + "(" + recyclingWorkFile.getName() + ")" + "失败";
            log.error(moveError);
            throw new ServiceException("操作失败");
        }
    }

    /**
     * 手动删除指定的回收作业记录（管理员操作）。
     *
     * @param recyclingWorkFileId 要删除的回收作业ID
     * @return 操作结果对象，表示删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult manuallyDeleteRecyclingWorkFile(Long recyclingWorkFileId) throws JsonProcessingException {
        RecyclingWorkFile recyclingWorkFile = getRecyclingWorkFile(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "待清除作业未找到");
        }

        if (StorageType.PICTURE_KEY.getValue().equals(recyclingWorkFile.getStorageType())) {
            mowerBucketService.removeSingleWebP(recyclingWorkFile.getFileContent());
            mowerBucketService.removeMultipleWebP(recyclingWorkFile.getDescriptionPictures());
        }

        boolean isDeleted = deleteRecyclingWorkFile(recyclingWorkFile);
        if (isDeleted) {
            return new OperateResult(200, "作业删除成功");
        } else {
            return new OperateResult(500, "作业删除失败");
        }
    }

    /**
     * 根据管理员筛选条件筛选待回收作业列表。
     *
     * @param workFileAdminScreen 管理员筛选条件对象
     * @return 符合条件的待回收作业列表
     */
    public List<RecyclingWorkFile> screenRecyclingWorkFileList(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createAdminUserLimitedQueryWrapper(workFileAdminScreen, RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取符合管理员筛选条件的待回收作业总数。
     *
     * @param workFileAdminScreen 管理员筛选条件对象
     * @return 符合条件的待回收作业总数
     */
    public Long getRecyclingWorkFileListCount(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createAdminUserQueryWrapper(workFileAdminScreen, RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectCount(queryWrapper);
    }
}
