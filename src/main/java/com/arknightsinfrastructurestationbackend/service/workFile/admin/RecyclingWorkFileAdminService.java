package com.arknightsinfrastructurestationbackend.service.workFile.admin;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.admin.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.user.DownloadRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.StarRecord;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.user.DownloadRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class RecyclingWorkFileAdminService {
    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final WorkFileMapper workFileMapper;
    private final StarRecordMapper starRecordMapper;
    private final DownloadRecordMapper downloadRecordMapper;
    private final CommonService commonService;
    private final MowerBucketService mowerBucketService;
    private final AdapterService adapterService;

    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult recoverWorkFileFromRecyclingWorkFile(Long recyclingWorkFileId) throws ServiceException {
        // 从recycling_work_files表中检索作业
        RecyclingWorkFile recyclingWorkFile = recyclingWorkFileMapper.selectById(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "作业未找到");
        }

        // 从recycling_work_files表中删除记录
        int deleteResult = recyclingWorkFileMapper.deleteById(recyclingWorkFile);

        // 转换为WorkFile
        WorkFile workFile = recyclingWorkFile.toWorkFile();
        workFile.setReleaseDate(commonService.getCurrentDateTime());  // 重新设置发布时间

        // 将WorkFile插入到work_files表中
        int insertResult = workFileMapper.insert(workFile);

        if (deleteResult > 0 && insertResult > 0) {
            return new OperateResult(200, "作业恢复成功");
        } else {
            String moveError = "管理员于" + commonService.getCurrentDateTime() + "尝试从回收箱中恢复文件" + recyclingWorkFile.getId()
                    + "(" + recyclingWorkFile.getName() + ")" + "失败";
            Log.error(moveError);
            throw new ServiceException("操作失败");
        }
    }

    public OperateResult manuallyDeleteRecyclingWorkFile(Long recyclingWorkFileId) throws JsonProcessingException {
        // 检查待清除作业是否存在
        RecyclingWorkFile recyclingWorkFile = recyclingWorkFileMapper.selectById(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "待清除作业未找到");
        }

        // 检查是否已webp图片格式存储作业文件
        if (StorageType.PICTURE_KEY.getValue().equals(recyclingWorkFile.getStorageType())) {
            // 如果是，删除其所拥有的唯一key，以及对象存储桶中的键值
            mowerBucketService.removeSingleWebP(recyclingWorkFile.getFileContent());
            mowerBucketService.removeMultipleWebP(recyclingWorkFile.getDescriptionPictures());
        }

        // 执行删除操作
        int deleteResult = recyclingWorkFileMapper.deleteById(recyclingWorkFileId);

        if (deleteResult > 0) {
            // 同步删除这份作业可能存在的收藏记录和下载记录
            LambdaQueryWrapper<StarRecord> starRecordQueryWrapper = new LambdaQueryWrapper<>();
            starRecordQueryWrapper.eq(StarRecord::getWid, recyclingWorkFile.getId());
            if (starRecordMapper.selectCount(starRecordQueryWrapper) > 0)
                starRecordMapper.delete(starRecordQueryWrapper);

            LambdaQueryWrapper<DownloadRecord> downloadRecordQueryWrapper = new LambdaQueryWrapper<>();
            downloadRecordQueryWrapper.eq(DownloadRecord::getWid, recyclingWorkFile.getId());
            if (downloadRecordMapper.selectCount(downloadRecordQueryWrapper) > 0)
                downloadRecordMapper.delete(downloadRecordQueryWrapper);

            return new OperateResult(200, "作业删除成功");
        } else {
            return new OperateResult(500, "作业删除失败");
        }
    }

    public List<RecyclingWorkFile> screenRecyclingWorkFileList(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createAdminLimitedQueryWrapper(workFileAdminScreen, RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectList(queryWrapper);
    }

    public Long getRecyclingWorkFileListCount(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createAdminQueryWrapper(workFileAdminScreen, RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectCount(queryWrapper);
    }
}
