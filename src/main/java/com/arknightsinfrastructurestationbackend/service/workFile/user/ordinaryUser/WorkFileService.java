package com.arknightsinfrastructurestationbackend.service.workFile.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.common.aspect.redisLock.RedisLock;
import com.arknightsinfrastructurestationbackend.common.aspect.workFileRefresh.ListDataRefresh;
import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.ordinaryUser.WorkFileScreen;
import com.arknightsinfrastructurestationbackend.dto.query.ordinaryUser.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.UploadWorkFileCount;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.RefreshType;
import com.arknightsinfrastructurestationbackend.global.type.SortOrderType;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.UploadWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.SelectUserService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser.BaseWorkFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 普通用户专用的作业文件服务类。
 * <p>提供用户权限校验、上传次数限制、用户相关的插入、更新、删除和查询功能。</p>
 */
@Slf4j
@Service
public class WorkFileService extends BaseWorkFileService {
    private final SelectUserService selectUserService;
    private final UploadWorkFileCountMapper uploadWorkFileCountMapper;

    public WorkFileService(WorkFileMapper workFileMapper,
                           MowerBucketService mowerBucketService,
                           CommonService commonService,
                           AdapterService adapterService,
                           SelectUserService selectUserService,
                           UploadWorkFileCountMapper uploadWorkFileCountMapper) {
        super(workFileMapper, mowerBucketService, commonService, adapterService);
        this.selectUserService = selectUserService;
        this.uploadWorkFileCountMapper = uploadWorkFileCountMapper;
    }

    /**
     * 为指定用户插入新的作业文件记录。
     * <p>在插入前会进行用户验证和上传次数限制检查，插入成功后会更新用户的上传次数记录。</p>
     *
     * @param token    用户身份标识token
     * @param workFile 要插入的作业文件对象
     * @return 操作结果对象，表示插入操作的成功或失败状态
     * @throws ServiceException        当业务逻辑处理出错（例如上传次数更新失败）时抛出
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult insertWorkFile(String token, WorkFile workFile) throws ServiceException, JsonProcessingException {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        // 检查用户上传限制逻辑
        UploadWorkFileCount uploadWorkFileCount = uploadWorkFileCountMapper.selectById(user.getId());
        if (uploadWorkFileCount != null && uploadWorkFileCount.getCount() >= 50) {
            return new OperateResult(403, "已达到今日上传次数上限");
        }

        // 设置作者信息和其他必要字段
        workFile.setAuthorId(user.getId());
        workFile.setAuthor(user.getUsername());
        // 父类中的doInsertWorkFile负责执行基础的插入逻辑
        OperateResult result = doInsertWorkFile(workFile);

        if (result.getOperateCode() == 200) {
            // 更新用户上传次数计数
            if (uploadWorkFileCount != null) {
                uploadWorkFileCount.setCount(uploadWorkFileCount.getCount() + 1);
                if (uploadWorkFileCountMapper.updateById(uploadWorkFileCount) <= 0) {
                    log.error("用户：{}({}) 于{}上传更新记录作业文件{}({})失败", user.getId(), user.getUsername(), commonService.getCurrentDateTime(), workFile.getId(), workFile.getName());
                    throw new ServiceException("上传记录更新失败");
                }
            } else {
                UploadWorkFileCount initData = new UploadWorkFileCount(user.getId(), 1);
                if (uploadWorkFileCountMapper.insert(initData) <= 0) {
                    log.error("用户：{}({}) 于{}初始化更新记录作业文件{}({})失败", user.getId(), user.getUsername(), commonService.getCurrentDateTime(), workFile.getId(), workFile.getName());
                    throw new ServiceException("上传记录初始化失败");
                }
            }
        }
        return result;
    }

    /**
     * 为指定用户更新已有的作业文件记录。
     * <p>在更新前会进行用户验证和权限校验（只能更新自己的作业）。</p>
     *
     * @param token    用户身份标识token
     * @param workFile 包含更新后字段信息的作业文件对象
     * @return 操作结果对象，表示更新操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult updateWorkFile(String token, WorkFile workFile) throws JsonProcessingException {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        WorkFile existingWorkFile = workFileMapper.selectById(workFile.getId());
        if (existingWorkFile == null) {
            return new OperateResult(404, "作业不存在");
        }

        // 检查是否是本人作品
        if (!existingWorkFile.getAuthorId().equals(user.getId())) {
            return new OperateResult(403, "无权限编辑他人的作业");
        }

        // 调用父类方法执行基础更新逻辑
        // 在更新之前，需要把当前用户信息更新到 newWorkFile 对象中，以确保作者信息正确
        workFile.setAuthor(user.getUsername());
        workFile.setAuthorId(user.getId());
        return doUpdateWorkFile(existingWorkFile, workFile);
    }

    /**
     * 为指定用户删除已有的作业文件记录。
     * <p>在删除前会进行用户验证和权限校验（只能删除自己的作业）。</p>
     *
     * @param token 用户身份标识token
     * @param wid   要删除的作业文件ID
     * @return 操作结果对象，表示删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    public OperateResult deleteWorkFile(String token, Long wid) throws JsonProcessingException {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        WorkFile workFile = workFileMapper.selectById(wid);
        if (workFile == null) {
            return new OperateResult(404, "作业不存在");
        }

        if (!workFile.getAuthorId().equals(user.getId())) {
            return new OperateResult(403, "无权限删除他人的作业");
        }

        // 调用父类基础删除方法
        return doDeleteWorkFile(workFile);
    }

    /**
     * 根据用户提供的筛选条件查询作业文件列表（无需特定用户关联）。
     * <p>方法调用时会进行数据刷新和缓存操作。</p>
     *
     * @param workFileScreen 用户自定义的筛选条件对象
     * @return 符合条件的作业文件列表（分页后的结果）
     */
    @ListDataRefresh({RefreshType.DSS})
    @Cacheable("workFileList")
    @RedisLock(key = "'lock:WorkFileService:screenWorkFileList:' + #workFileScreen.hashCode()")
    public List<WorkFile> screenWorkFileList(WorkFileScreen workFileScreen) {
        LambdaQueryWrapper<WorkFile> queryWrapper = buildWrapper(workFileScreen);

        int currentPage = workFileScreen.getCurrentPage();
        int pageSize = workFileScreen.getPageSize();
        int offset = Math.min((currentPage - 1) * pageSize, 10000 - pageSize);
        queryWrapper.last("LIMIT " + offset + "," + pageSize);

        return workFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取符合用户筛选条件的作业文件数量（无需特定用户关联）。
     * <p>方法调用时会使用缓存。</p>
     *
     * @param workFileScreen 用户自定义的筛选条件对象
     * @return 符合条件的作业文件总数
     */
    @Cacheable("workFileListCount")
    @RedisLock(key = "'lock:WorkFileService:getWorkFileListCount:' + #workFileScreen.hashCode()")
    public Long getWorkFileListCount(WorkFileScreen workFileScreen) {
        LambdaQueryWrapper<WorkFile> queryWrapper = buildWrapper(workFileScreen);
        return workFileMapper.selectCount(queryWrapper);
    }

    /**
     * 根据筛选条件构建用于查询WorkFile的条件构造器。
     * <p>该方法用于内部逻辑辅助。</p>
     *
     * @param workFileScreen 用户自定义的筛选条件对象
     * @return 根据筛选条件构建的LambdaQueryWrapper
     */
    private LambdaQueryWrapper<WorkFile> buildWrapper(WorkFileScreen workFileScreen) {
        LambdaQueryWrapper<WorkFile> queryWrapper = new LambdaQueryWrapper<>();

        // 筛选 type，如果不是"全部"则添加条件
        if (workFileScreen.getType() != null && !"全部".equals(workFileScreen.getType())) {
            queryWrapper.eq(WorkFile::getType, workFileScreen.getType());
        }

        // 筛选 layout，如果不是"全部"则添加条件
        if (workFileScreen.getLayout() != null && !"全部".equals(workFileScreen.getLayout())) {
            queryWrapper.eq(WorkFile::getLayout, workFileScreen.getLayout());
        }

        // 筛选 dateRange，如果存在开始和结束日期
        if (workFileScreen.getDateRange() != null && workFileScreen.getDateRange().size() == 2) {
            queryWrapper.between(WorkFile::getReleaseDate, workFileScreen.getDateRange().get(0), workFileScreen.getDateRange().get(1));
        }

        // 模糊搜索 workQuery，在多个字段中搜索
        if (workFileScreen.getWorkQuery() != null && !workFileScreen.getWorkQuery().isEmpty()) {
            queryWrapper.and(wrapper ->
                    wrapper.like(WorkFile::getId, workFileScreen.getWorkQuery())
                            .or().like(WorkFile::getName, workFileScreen.getWorkQuery())
                            .or().like(WorkFile::getDescription, workFileScreen.getWorkQuery())
                            .or().like(WorkFile::getAuthor, workFileScreen.getWorkQuery())
                            .or().like(WorkFile::getAuthorId, workFileScreen.getWorkQuery()));
        }

        if (SortOrderType.RELEASE_DATE_DESC.getValue().equals(workFileScreen.getSortOrder()))
            queryWrapper.orderByDesc(WorkFile::getReleaseDate);
        else if (SortOrderType.RELEASE_DATE_ASC.getValue().equals(workFileScreen.getSortOrder()))
            queryWrapper.orderByAsc(WorkFile::getReleaseDate);
        else if (SortOrderType.SCORE_DESC.getValue().equals(workFileScreen.getSortOrder()))
            queryWrapper.orderByDesc(WorkFile::getScore);
        else if (SortOrderType.SCORE_ASC.getValue().equals(workFileScreen.getSortOrder()))
            queryWrapper.orderByAsc(WorkFile::getScore);

        return queryWrapper;
    }

    /**
     * 根据用户和简单筛选条件查询用户已发布的作业列表。
     * <p>需要进行用户验证。</p>
     *
     * @param token                用户身份标识token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合条件的已发布作业列表
     */
    @ListDataRefresh({RefreshType.DSS})
    @Cacheable("postedWorkFileList")
    public List<WorkFile> screenPostedWorkFileList(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }

        QueryWrapper<WorkFile> queryWrapper = adapterService.createCommonUserLimitedQueryWrapper(workFileSimpleSearch, user.getId(), WorkFile.class);
        return workFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取指定用户已发布的作业总数。
     * <p>需要进行用户验证并使用缓存。</p>
     *
     * @param token                用户身份标识token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 该用户已发布的作业总数
     */
    @Cacheable("postedWorkFileListCount")
    public Long getPostedWorkFileListCount(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getByToken(token);
        if (user == null) {
            return 0L;
        }

        QueryWrapper<WorkFile> queryWrapper = adapterService.createCommonUserQueryWrapper(workFileSimpleSearch, user.getId(), WorkFile.class);
        return workFileMapper.selectCount(queryWrapper);
    }

    /**
     * 根据作业文件ID获取对应的作业文件对象。
     *
     * @param wid 作业文件ID
     * @return 对应的作业文件对象，如果不存在则返回null
     */
    public WorkFile getWorkFileById(Long wid) {
        return workFileMapper.selectById(wid);
    }
}
