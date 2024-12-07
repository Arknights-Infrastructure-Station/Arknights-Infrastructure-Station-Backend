package com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser;

import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.DownloadRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.StarRecord;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.DownloadRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 基础的待回收作业服务类。
 * <p>抽象出获取和删除回收作业记录的公共逻辑，为子类提供通用方法。</p>
 */
@Service
@AllArgsConstructor
public abstract class BaseRecyclingWorkFileService {
    protected final RecyclingWorkFileMapper recyclingWorkFileMapper;
    protected final WorkFileMapper workFileMapper;
    protected final StarRecordMapper starRecordMapper;
    protected final DownloadRecordMapper downloadRecordMapper;
    protected final CommonService commonService;
    protected final MowerBucketService mowerBucketService;
    protected final AdapterService adapterService;

    /**
     * 根据回收作业ID获取对应的回收作业对象。
     *
     * @param recyclingWorkFileId 回收作业ID
     * @return 对应的回收作业对象，如果不存在则返回null
     */
    protected RecyclingWorkFile getRecyclingWorkFile(Long recyclingWorkFileId) {
        return recyclingWorkFileMapper.selectById(recyclingWorkFileId);
    }

    /**
     * 根据传入的回收作业对象执行删除逻辑。
     * <p>会根据其存储类型删除相关webP图片，并从数据库中删除该记录，同时删除相关收藏和下载记录。</p>
     *
     * @param recyclingWorkFile 要删除的回收作业对象
     * @return 删除操作的成功或失败状态
     */
    protected boolean deleteRecyclingWorkFile(RecyclingWorkFile recyclingWorkFile) {
        int deleteResult = recyclingWorkFileMapper.deleteById(recyclingWorkFile.getId());
        if (deleteResult > 0) {
            // 同步删除收藏记录
            LambdaQueryWrapper<StarRecord> starRecordQueryWrapper = new LambdaQueryWrapper<>();
            starRecordQueryWrapper.eq(StarRecord::getWid, recyclingWorkFile.getId());
            if (starRecordMapper.selectCount(starRecordQueryWrapper) > 0) {
                starRecordMapper.delete(starRecordQueryWrapper);
            }

            // 同步删除下载记录
            LambdaQueryWrapper<DownloadRecord> downloadRecordQueryWrapper = new LambdaQueryWrapper<>();
            downloadRecordQueryWrapper.eq(DownloadRecord::getWid, recyclingWorkFile.getId());
            if (downloadRecordMapper.selectCount(downloadRecordQueryWrapper) > 0) {
                downloadRecordMapper.delete(downloadRecordQueryWrapper);
            }

            return true;
        }
        return false;
    }
}
