package com.arknightsinfrastructurestationbackend.service.workFile.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser.BaseWorkFileService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员专用的作业文件服务类。
 * <p>主要提供无需用户权限校验和上传次数限制的插入、更新、删除和筛选作业功能。</p>
 */
@Service
public class WorkFileAdminService extends BaseWorkFileService {

    public WorkFileAdminService(WorkFileMapper workFileMapper,
                                MowerBucketService mowerBucketService,
                                CommonService commonService,
                                AdapterService adapterService) {
        super(workFileMapper, mowerBucketService, commonService, adapterService);
    }

    /**
     * 插入新的作业文件记录（管理员操作）。
     *
     * @param workFile 要插入的作业文件对象
     * @return 操作结果对象，表示插入操作的成功或失败状态
     * @throws ServiceException 当业务逻辑处理出错时抛出
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult insertWorkFile(WorkFile workFile) throws ServiceException, JsonProcessingException {
        return doInsertWorkFile(workFile);
    }

    /**
     * 更新已存在的作业文件记录（管理员操作）。
     * <p>无需权限校验。</p>
     *
     * @param workFile 包含更新后字段信息的作业文件对象
     * @return 操作结果对象，表示更新操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult updateWorkFile(WorkFile workFile) throws JsonProcessingException {
        WorkFile existingWorkFile = workFileMapper.selectById(workFile.getId());
        if (existingWorkFile == null) {
            return new OperateResult(404, "作业不存在");
        }
        // 管理员更新无需权限校验，直接调用基础更新逻辑
        return doUpdateWorkFile(existingWorkFile, workFile);
    }

    /**
     * 根据ID删除指定的作业文件记录（管理员操作）。
     * <p>无需权限校验。</p>
     *
     * @param wid 要删除的作业文件ID
     * @return 操作结果对象，表示删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult deleteWorkFile(Long wid) throws JsonProcessingException {
        WorkFile workFile = workFileMapper.selectById(wid);
        // 管理员删除无需权限校验
        return doDeleteWorkFile(workFile);
    }

    /**
     * 根据管理员设置的筛选条件筛选作业文件列表。
     *
     * @param workFileAdminScreen 管理员筛选条件对象
     * @return 符合条件的作业文件列表
     */
    public List<WorkFile> screenWorkFileList(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<WorkFile> queryWrapper = adapterService.createAdminUserLimitedQueryWrapper(workFileAdminScreen, WorkFile.class);
        return workFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取符合管理员筛选条件的作业文件数量。
     *
     * @param workFileAdminScreen 管理员筛选条件对象
     * @return 符合条件的作业文件总数
     */
    public Long getWorkFileListCount(WorkFileAdminScreen workFileAdminScreen) {
        QueryWrapper<WorkFile> queryWrapper = adapterService.createAdminUserQueryWrapper(workFileAdminScreen, WorkFile.class);
        return workFileMapper.selectCount(queryWrapper);
    }
}
