package com.arknightsinfrastructurestationbackend.service.workFile;

import com.arknightsinfrastructurestationbackend.common.aspect.redisLock.RedisLock;
import com.arknightsinfrastructurestationbackend.common.aspect.workFileRefresh.ListDataRefresh;
import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileScreen;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.user.UploadWorkFileCount;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.RefreshType;
import com.arknightsinfrastructurestationbackend.global.type.SortOrderType;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.user.UploadWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.user.SelectUserService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class WorkFileService {
    private final WorkFileMapper workFileMapper;
    private final SelectUserService selectUserService;
    private final MowerBucketService mowerBucketService;
    private final UploadWorkFileCountMapper uploadWorkFileCountMapper;
    private final CommonService commonService;
    private final AdapterService adapterService;

    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult insertWorkFile(String token, WorkFile workFile) throws ServiceException, JsonProcessingException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        // 检查该用户是否已达到上传次数上限
        UploadWorkFileCount uploadWorkFileCount = uploadWorkFileCountMapper.selectById(user.getId());
        if (uploadWorkFileCount != null) {
            if (uploadWorkFileCount.getCount() >= 50) { // 限制每天只能上传50份作业文件
                return new OperateResult(403, "已达到今日上传次数上限");
            }
        }

        // 作业id由数据库自动增长字段生成
        workFile.setAuthorId(user.getId());
        workFile.setAuthor(user.getUsername());
        workFile.setReleaseDate(commonService.getCurrentDateTime());
        workFile.setScore(-1f);

        if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType())) {
            // 如果存储方式为图片方式，那么将图片字节流存入对象存储桶，将存储的key存入FileContent中
            String key = mowerBucketService.uploadSingleWebP(workFile.getFileContent());
            workFile.setFileContent(key); // 如果存储失败，那么返回的key会是null，而fileContent字段被设置为非null，会制止异常数据的插入
        }

        // 替换完成后，重设字段
        workFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(workFile.getDescriptionPictures()));

        if (workFileMapper.insert(workFile) > 0) {
            if (uploadWorkFileCount != null) {
                uploadWorkFileCount.setCount(uploadWorkFileCount.getCount() + 1);
                int count = uploadWorkFileCountMapper.updateById(uploadWorkFileCount);
                if (count <= 0) {
                    String exceptionLog = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                            + commonService.getCurrentDateTime() + "上传更新记录作业文件"
                            + workFile.getId() + "(" + workFile.getName() + ")失败";
                    Log.error(exceptionLog);
                    throw new ServiceException("上传记录更新失败");
                }
            } else {
                UploadWorkFileCount initData = new UploadWorkFileCount(user.getId(), 1);
                int count = uploadWorkFileCountMapper.insert(initData);
                if (count <= 0) {
                    String exceptionLog = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                            + commonService.getCurrentDateTime() + "初始化更新记录作业文件"
                            + workFile.getId() + "(" + workFile.getName() + ")失败";
                    Log.error(exceptionLog);
                    throw new ServiceException("上传记录初始化失败");
                }
            }
            return new OperateResult(200, "作业创建成功");
        } else {
            return new OperateResult(500, "作业创建失败");
        }
    }


    public OperateResult updateWorkFile(String token, WorkFile workFile) throws JsonProcessingException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        WorkFile existingWorkFile = workFileMapper.selectById(workFile.getId());

        if (existingWorkFile == null) {
            return new OperateResult(404, "作业不存在");
        }

        if (existingWorkFile.getAuthorId().equals(user.getId())) {
            // 作业id由前端提供
            existingWorkFile.setName(workFile.getName());
            existingWorkFile.setType(workFile.getType());
            existingWorkFile.setLayout(workFile.getLayout());
            existingWorkFile.setDescription(workFile.getDescription());

            existingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(workFile.getDescriptionPictures()));

            existingWorkFile.setStorageType(workFile.getStorageType());
            if (StorageType.PICTURE_KEY.getValue().equals(existingWorkFile.getStorageType())) {
                //传来的是图片的字节流，载入的是返回的存储key
                String key = mowerBucketService.uploadSingleWebP(workFile.getFileContent());
                existingWorkFile.setFileContent(key);
            } else {
                existingWorkFile.setFileContent(workFile.getFileContent());
            }

            existingWorkFile.setFileRequest(workFile.getFileRequest());
            existingWorkFile.setAuthor(user.getUsername());
            existingWorkFile.setReleaseDate(commonService.getCurrentDateTime());
            if (workFileMapper.updateById(existingWorkFile) > 0) {
                return new OperateResult(200, "作业更新成功");
            } else {
                return new OperateResult(500, "作业更新失败");
            }
        } else {
            return new OperateResult(403, "无权限编辑他人的作业");
        }
    }

    public OperateResult deleteWorkFile(String token, Long wid) throws JsonProcessingException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        WorkFile workFile = workFileMapper.selectById(wid);
        if (workFile != null && workFile.getAuthorId().equals(user.getId())) {

            // 检查是否已webp图片格式存储作业文件
            if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType())) {
                // 如果是，删除其所拥有的唯一key，以及对象存储桶中的键值
                mowerBucketService.removeSingleWebP(workFile.getFileContent());
                mowerBucketService.removeMultipleWebP(workFile.getDescriptionPictures());
            }

            if (workFileMapper.deleteById(wid) > 0) {
                return new OperateResult(200, "作业删除成功");
            } else {
                return new OperateResult(500, "作业删除失败");
            }
        } else {
            return new OperateResult(403, "无权限删除他人的作业");
        }
    }

//    @ListDataRefresh({RefreshType.DS, RefreshType.PK})
//    public List<WorkFile> getAllWorkFile() {
//        return workFileMapper.selectList(null);
//    }

//    @ListDataRefresh({RefreshType.DS, RefreshType.PK})
//    public List<WorkFile> getPostedWorkFileList(String token) {
//        User user = selectUserService.getUserByToken(token);
//        if (user == null) {
//            return Collections.emptyList();
//        }
//
//        LambdaQueryWrapper<WorkFile> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(WorkFile::getAuthorId, user.getId());
//
//        return workFileMapper.selectList(queryWrapper);
//    }

    // 该方法一般为后端内部调用
    public WorkFile getWorkFileById(Long wid) {
        return workFileMapper.selectById(wid);
    }

    /*
    切面和缓存顺序
    Spring AOP和缓存注解的执行顺序如下：
    1.切面@Around注解会在方法执行前后都拦截该方法。
    2.@Cacheable注解会在方法执行后将返回值存入缓存。
    缓存存储顺序
    1.切面ListDataRefreshAspect的@Around方法在目标方法执行前被调用。
    2.目标方法（screenWorkFileList）执行，并返回结果列表。
    3.切面继续执行，将结果列表传递给refreshWorkFileDataService.refreshStarAndDownloadNumber进行刷新处理。
    4.刷新后的结果列表被返回到调用者。
    5.如果目标方法上有@Cacheable注解，则Spring缓存机制会将切面返回的结果存入缓存。
    因此，刷新后的结果会被存入缓存。具体步骤如下：
    1.ListDataRefreshAspect的around方法开始执行，并捕获原方法调用。
    2.执行原方法（screenWorkFileList），获取结果列表。
    3.调用refreshWorkFileDataService.refreshStarAndDownloadNumber方法刷新结果。
    4.返回刷新后的结果。
    5.@Cacheable注解会将最终的结果存入缓存。
     */
    @ListDataRefresh({RefreshType.DSS})
    @Cacheable("workFileList")
    @RedisLock(key = "'lock:WorkFileService:screenWorkFileList:' + #workFileScreen.hashCode()")
    public List<WorkFile> screenWorkFileList(WorkFileScreen workFileScreen) {
        LambdaQueryWrapper<WorkFile> queryWrapper = buildWrapper(workFileScreen);

        // 分页，限制查询到的总条目数
        int currentPage = workFileScreen.getCurrentPage();
        int pageSize = workFileScreen.getPageSize();
        int offset = Math.min((currentPage - 1) * pageSize, 10000 - pageSize);
        queryWrapper.last("LIMIT " + offset + "," + pageSize);

        return workFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取 WorkFile 表中的总条目数
     *
     * @return Long 总条目数
     */
    @Cacheable("workFileListCount")
    @RedisLock(key = "'lock:WorkFileService:getWorkFileListCount:' + #workFileScreen.hashCode()")
    public Long getWorkFileListCount(WorkFileScreen workFileScreen) {
        LambdaQueryWrapper<WorkFile> queryWrapper = buildWrapper(workFileScreen);
        return workFileMapper.selectCount(queryWrapper);
    }

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
     * 根据用户设置的筛选条件查询出指定数量的作业列表
     *
     * @param token                用户token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合条件的作业列表
     */
    @ListDataRefresh({RefreshType.DSS})
    @Cacheable("postedWorkFileList")
    public List<WorkFile> screenPostedWorkFileList(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }

        QueryWrapper<WorkFile> queryWrapper = adapterService.createLimitedQueryWrapper(workFileSimpleSearch, user.getId(), WorkFile.class);
        return workFileMapper.selectList(queryWrapper);
    }

    /**
     * 获取某个用户已发布的作业总数
     *
     * @param token 用户token
     * @return 该用户的已发布作业总数
     */
    @Cacheable("postedWorkFileListCount")
    public Long getPostedWorkFileListCount(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return 0L;
        }

        QueryWrapper<WorkFile> queryWrapper = adapterService.createQueryWrapper(workFileSimpleSearch, user.getId(), WorkFile.class);
        return workFileMapper.selectCount(queryWrapper);
    }
}
