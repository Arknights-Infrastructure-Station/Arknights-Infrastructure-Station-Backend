package com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser;

import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.StagingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 基础的暂存作业服务类。
 * <p>抽象出获取、删除等暂存作业的公共逻辑，为子类提供通用方法。</p>
 */
@Service
@AllArgsConstructor
public abstract class BaseStagingWorkFileService {
    protected final StagingWorkFileMapper stagingWorkFileMapper;
    protected final MowerBucketService mowerBucketService;
    protected final CommonService commonService;
    protected final AdapterService adapterService;

    /**
     * 根据暂存作业ID获取对应的暂存作业对象。
     *
     * @param wid 暂存作业ID
     * @return 对应的暂存作业对象，如果不存在则返回null
     */
    protected StagingWorkFile getStagingWorkFile(Long wid) {
        return stagingWorkFileMapper.selectById(wid);
    }

    /**
     * 内部使用的方法，根据传入的暂存作业对象执行删除逻辑。
     * <p>会根据存储类型删除相关的webP图片，并从数据库中删除该记录。</p>
     *
     * @param stagingWorkFile 要删除的暂存作业对象
     * @return 删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    protected boolean deleteStagingWorkFileInternal(StagingWorkFile stagingWorkFile) throws JsonProcessingException {
        if (stagingWorkFile != null) {
            // 检查是否已webp图片格式存储作业文件
            if (StorageType.PICTURE_KEY.getValue().equals(stagingWorkFile.getStorageType())) {
                // 删除图片
                mowerBucketService.removeSingleWebP(stagingWorkFile.getFileContent());
                mowerBucketService.removeMultipleWebP(stagingWorkFile.getDescriptionPictures());
            }

            return stagingWorkFileMapper.deleteById(stagingWorkFile.getId()) > 0;
        }
        return false;
    }
}
