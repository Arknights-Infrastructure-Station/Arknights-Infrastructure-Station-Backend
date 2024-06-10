package com.arknightsinfrastructurestationbackend.service.workFile;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.user.DownloadRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.StarRecord;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.user.DownloadRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.user.SelectUserService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class RecyclingWorkFileService {
    private final RecyclingWorkFileMapper recyclingWorkFileMapper;
    private final WorkFileMapper workFileMapper;
    private final StarRecordMapper starRecordMapper;
    private final DownloadRecordMapper downloadRecordMapper;
    private final SelectUserService selectUserService;
    private final CommonService commonService;
    private final MowerBucketService mowerBucketService;
    private final AdapterService adapterService;

    public static String getDateTimeAfterGivenDays(int afterDays) {
        // 获取当前日期和时间
        LocalDateTime now = LocalDateTime.now();

        // 在当前日期和时间上加30天
        LocalDateTime futureDateTime = now.plusDays(afterDays);

        // 定义MySQL datetime格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 转换成字符串
        return futureDateTime.format(formatter);
    }

    @Transactional(rollbackFor = ServiceException.class)  // 确保方法在事务中运行
    public OperateResult addRecyclingWorkFileFromWorkList(String token, Long workFileId) throws ServiceException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        WorkFile workFile = workFileMapper.selectById(workFileId);
        if (workFile == null) {
            return new OperateResult(404, "作业未找到");
        }

        // 检查用户是否发布过这个作业
        if (workFile.getAuthorId().equals(user.getId())) {
            // 为recyclingWorkFile添加清除时间
            RecyclingWorkFile recyclingWorkFile = workFile.toRecyclingWorkFile();
            recyclingWorkFile.setClearTime(getDateTimeAfterGivenDays(30)); // 默认30天后清除该作业

            // 插入待回收记录
            int insertResult = recyclingWorkFileMapper.insert(recyclingWorkFile);

            // 删除已有记录
            int deleteResult = workFileMapper.deleteById(workFile);

            if (insertResult > 0 && deleteResult > 0) {
                return new OperateResult(200, "作业移至回收箱成功");
            } else {
                // 如果任何操作失败，事务将自动回滚
                String moveError = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                        + commonService.getCurrentDateTime() + "尝试将作业文件" + recyclingWorkFile.getId()
                        + "(" + recyclingWorkFile.getName() + ")" + "加入回收箱失败";
                Log.error(moveError);
                throw new ServiceException("操作失败");
            }
        } else {
            return new OperateResult(403, "无权限操作他人作业或作业不存在");
        }
    }

    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult recoverWorkFileFromRecyclingWorkFile(String token, Long recyclingWorkFileId) throws ServiceException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        // 从recycling_work_files表中检索作业
        RecyclingWorkFile recyclingWorkFile = recyclingWorkFileMapper.selectById(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "作业未找到");
        }

        // 检查用户是否发布过这个作业
        if (recyclingWorkFile.getAuthorId().equals(user.getId())) {
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
                String moveError = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                        + commonService.getCurrentDateTime() + "尝试从回收箱中恢复文件" + recyclingWorkFile.getId()
                        + "(" + recyclingWorkFile.getName() + ")" + "失败";
                Log.error(moveError);
                throw new ServiceException("操作失败");
            }
        } else {
            return new OperateResult(403, "无权限操作他人作业或作业不存在");
        }
    }

    public OperateResult manuallyDeleteRecyclingWorkFile(String token, Long recyclingWorkFileId) {
        // 通过token获取用户信息
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        // 检查待清除作业是否存在
        RecyclingWorkFile recyclingWorkFile = recyclingWorkFileMapper.selectById(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "待清除作业未找到");
        }

        // 检查是否为作业的发布者
        if (!recyclingWorkFile.getAuthorId().equals(user.getId())) {
            return new OperateResult(403, "没有权限删除该作业");
        }

        // 检查是否已webp图片格式存储作业文件
        if (StorageType.PICTURE_KEY.getValue().equals(recyclingWorkFile.getStorageType())) {
            // 如果是，删除其所拥有的唯一key，以及对象存储桶中的键值
            mowerBucketService.deleteWebP(recyclingWorkFile.getFileContent());
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

    public List<RecyclingWorkFile> getRecyclingWorkFileList(String token) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            throw new ServiceException("根据用户查询回收箱作业列表时，用户找不到");
        }

        LambdaQueryWrapper<RecyclingWorkFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecyclingWorkFile::getAuthorId, user.getId());
        return recyclingWorkFileMapper.selectList(queryWrapper);
    }

    /**
     * 根据用户设置的筛选条件查询出指定数量的待回收作业列表
     *
     * @param token                用户token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合条件的待回收作业列表
     */
    @Cacheable("recyclingWorkFileList")
    public List<RecyclingWorkFile> screenRecyclingWorkFileList(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }

        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createLimitedQueryWrapper(workFileSimpleSearch, user.getId(), RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取某个用户待回收的作业总数
     *
     * @param token 用户token
     * @return 该用户的待回收作业总数
     */
    @Cacheable("recyclingWorkFileListCount")
    public Long getRecyclingWorkFileListCount(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return 0L;
        }

        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createQueryWrapper(workFileSimpleSearch, user.getId(), RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectCount(queryWrapper);
    }
}
