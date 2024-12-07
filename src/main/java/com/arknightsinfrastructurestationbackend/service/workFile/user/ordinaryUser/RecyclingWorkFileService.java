package com.arknightsinfrastructurestationbackend.service.workFile.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.ordinaryUser.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.DownloadRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.StarRecordMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.RecyclingWorkFileMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.SelectUserService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser.BaseRecyclingWorkFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 普通用户专用的待回收作业服务类。
 * <p>用户可将自己的已发布作业移动至待回收列表，从待回收列表恢复或删除自己的作业，并筛选和统计待回收作业。</p>
 */
@Slf4j
@Service
public class RecyclingWorkFileService extends BaseRecyclingWorkFileService {
    private final SelectUserService selectUserService;

    public RecyclingWorkFileService(RecyclingWorkFileMapper recyclingWorkFileMapper,
                                    WorkFileMapper workFileMapper,
                                    StarRecordMapper starRecordMapper,
                                    DownloadRecordMapper downloadRecordMapper,
                                    CommonService commonService,
                                    MowerBucketService mowerBucketService,
                                    AdapterService adapterService,
                                    SelectUserService selectUserService) {
        super(recyclingWorkFileMapper, workFileMapper, starRecordMapper, downloadRecordMapper, commonService, mowerBucketService, adapterService);
        this.selectUserService = selectUserService;
    }

    public static String getDateTimeAfterGivenDays(int afterDays) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDateTime = now.plusDays(afterDays);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return futureDateTime.format(formatter);
    }

    /**
     * 将用户的作业文件移入待回收列表。
     * <p>会进行用户验证和权限校验（必须为本人的作业）。</p>
     *
     * @param token      用户身份标识token
     * @param workFileId 要移入待回收列表的作业文件ID
     * @return 操作结果对象，表示操作的成功或失败状态
     * @throws ServiceException 当业务逻辑处理出错时抛出
     */
    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult addRecyclingWorkFileFromWorkList(String token, Long workFileId) throws ServiceException {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        WorkFile workFile = workFileMapper.selectById(workFileId);
        if (workFile == null) {
            return new OperateResult(404, "作业未找到");
        }

        Long userWorkFileCount = recyclingWorkFileMapper.selectCount(new LambdaQueryWrapper<RecyclingWorkFile>().eq(RecyclingWorkFile::getAuthorId, user.getId()));
        if (userWorkFileCount >= 200) {
            return new OperateResult(403, "已达到待回收作业存储上限");
        }

        if (workFile.getAuthorId().equals(user.getId())) {
            RecyclingWorkFile recyclingWorkFile = workFile.toRecyclingWorkFile();
            recyclingWorkFile.setClearTime(getDateTimeAfterGivenDays(30));

            int insertResult = recyclingWorkFileMapper.insert(recyclingWorkFile);
            int deleteResult = workFileMapper.deleteById(workFile);

            if (insertResult > 0 && deleteResult > 0) {
                return new OperateResult(200, "作业移至回收箱成功");
            } else {
                String moveError = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                        + commonService.getCurrentDateTime() + "尝试将作业文件" + recyclingWorkFile.getId()
                        + "(" + recyclingWorkFile.getName() + ")" + "加入回收箱失败";
                log.error(moveError);
                throw new ServiceException("操作失败");
            }
        } else {
            return new OperateResult(403, "无权限操作他人作业或作业不存在");
        }
    }

    /**
     * 从待回收列表中恢复用户的作业文件。
     * <p>会进行用户验证和权限校验（必须为本人的作业）。</p>
     *
     * @param token               用户身份标识token
     * @param recyclingWorkFileId 要恢复的回收作业ID
     * @return 操作结果对象，表示恢复操作的成功或失败状态
     * @throws ServiceException 当业务逻辑处理出错时抛出
     */
    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult recoverWorkFileFromRecyclingWorkFile(String token, Long recyclingWorkFileId) throws ServiceException {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        RecyclingWorkFile recyclingWorkFile = getRecyclingWorkFile(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "作业未找到");
        }

        if (recyclingWorkFile.getAuthorId().equals(user.getId())) {
            int deleteResult = recyclingWorkFileMapper.deleteById(recyclingWorkFile);
            WorkFile workFile = recyclingWorkFile.toWorkFile();
            workFile.setReleaseDate(commonService.getCurrentDateTime());

            int insertResult = workFileMapper.insert(workFile);

            if (deleteResult > 0 && insertResult > 0) {
                return new OperateResult(200, "作业恢复成功");
            } else {
                String moveError = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                        + commonService.getCurrentDateTime() + "尝试从回收箱中恢复文件" + recyclingWorkFile.getId()
                        + "(" + recyclingWorkFile.getName() + ")" + "失败";
                log.error(moveError);
                throw new ServiceException("操作失败");
            }
        } else {
            return new OperateResult(403, "无权限操作他人作业或作业不存在");
        }
    }

    /**
     * 手动删除用户的待回收作业记录。
     * <p>会进行用户验证和权限校验（必须为本人的作业）。</p>
     *
     * @param token               用户身份标识token
     * @param recyclingWorkFileId 要删除的回收作业ID
     * @return 操作结果对象，表示删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult manuallyDeleteRecyclingWorkFile(String token, Long recyclingWorkFileId) throws JsonProcessingException {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        RecyclingWorkFile recyclingWorkFile = getRecyclingWorkFile(recyclingWorkFileId);
        if (recyclingWorkFile == null) {
            return new OperateResult(404, "待清除作业未找到");
        }

        if (!recyclingWorkFile.getAuthorId().equals(user.getId())) {
            return new OperateResult(403, "没有权限删除该作业");
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
     * 根据用户的简单筛选条件查询符合条件的待回收作业列表。
     * <p>需要进行用户验证。</p>
     *
     * @param token                用户身份标识token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合条件的待回收作业列表，如果用户不存在则返回空列表
     */
    @Cacheable("recyclingWorkFileList")
    public List<RecyclingWorkFile> screenRecyclingWorkFileList(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }

        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createCommonUserLimitedQueryWrapper(workFileSimpleSearch, user.getId(), RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取用户待回收的作业总数。
     * <p>需要进行用户验证。</p>
     *
     * @param token                用户身份标识token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 用户待回收作业的总数，如果用户不存在则返回0
     */
    @Cacheable("recyclingWorkFileListCount")
    public Long getRecyclingWorkFileListCount(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return 0L;
        }

        QueryWrapper<RecyclingWorkFile> queryWrapper = adapterService.createCommonUserQueryWrapper(workFileSimpleSearch, user.getId(), RecyclingWorkFile.class);
        return recyclingWorkFileMapper.selectCount(queryWrapper);
    }
}
