package com.arknightsinfrastructurestationbackend.service.workFile;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.user.UploadStagingWorkFileCount;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.user.UploadStagingWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.workFile.StagingWorkFileMapper;
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
public class StagingWorkFileService {
    private final StagingWorkFileMapper stagingWorkFileMapper;
    private final UploadStagingWorkFileCountMapper uploadStagingWorkFileCountMapper;
    private final SelectUserService selectUserService;
    private final CommonService commonService;
    private final MowerBucketService mowerBucketService;
    private final AdapterService adapterService;

    @Transactional(rollbackFor = ServiceException.class)
    public OperateResult insertStagingWorkFile(String token, StagingWorkFile stagingWorkFile) throws ServiceException, JsonProcessingException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        // 检查该用户是否已达到上传次数上限
        UploadStagingWorkFileCount uploadStagingWorkFileCount = uploadStagingWorkFileCountMapper.selectById(user.getId());
        if (uploadStagingWorkFileCount != null) {
            if (uploadStagingWorkFileCount.getCount() >= 50) { // 限制每天只能上传50份暂存作业文件
                return new OperateResult(403, "已达到今日上传次数上限");
            }
        }

        // 检查该用户是否已达到存储上限
        Long userWorkFileCount = stagingWorkFileMapper.selectCount(new LambdaQueryWrapper<StagingWorkFile>().eq(StagingWorkFile::getAuthorId,user.getId()));
        if (userWorkFileCount >= 200) { // 限制用户最多存储200份暂存作业
            return new OperateResult(403, "已达到暂存作业存储上限");
        }

        // 暂存作业id自动生成
        stagingWorkFile.setAuthorId(user.getId());
        stagingWorkFile.setAuthor(user.getUsername());
        stagingWorkFile.setStagingDate(commonService.getCurrentDateTime());

        if (StorageType.PICTURE_KEY.getValue().equals(stagingWorkFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(stagingWorkFile.getFileContent());
            stagingWorkFile.setFileContent(key);
        }

        // 替换图片存储数组字段
        stagingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(stagingWorkFile.getDescriptionPictures()));

        if (stagingWorkFileMapper.insert(stagingWorkFile) > 0) {
            if (uploadStagingWorkFileCount != null) {
                uploadStagingWorkFileCount.setCount(uploadStagingWorkFileCount.getCount() + 1);
                int count = uploadStagingWorkFileCountMapper.updateById(uploadStagingWorkFileCount);
                if (count <= 0) {
                    String exceptionLog = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                            + commonService.getCurrentDateTime() + "上传更新记录暂存作业文件"
                            + stagingWorkFile.getId() + "(" + stagingWorkFile.getName() + ")失败";
                    Log.error(exceptionLog);
                    throw new ServiceException("上传记录更新失败");
                }
            } else {
                UploadStagingWorkFileCount initData = new UploadStagingWorkFileCount(user.getId(), 1);
                int count = uploadStagingWorkFileCountMapper.insert(initData);
                if (count <= 0) {
                    String exceptionLog = "用户：" + user.getId() + "(" + user.getUsername() + ") 于"
                            + commonService.getCurrentDateTime() + "初始化更新记录暂存作业文件"
                            + stagingWorkFile.getId() + "(" + stagingWorkFile.getName() + ")失败";
                    Log.error(exceptionLog);
                    throw new ServiceException("上传记录初始化失败");
                }
            }
            return new OperateResult(200, "暂存作业创建成功");
        } else {
            return new OperateResult(500, "暂存作业创建失败");
        }
    }


    public OperateResult updateStagingWorkFile(String token, StagingWorkFile stagingWorkFile) throws JsonProcessingException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }
        StagingWorkFile existingStagingWorkFile = stagingWorkFileMapper.selectById(stagingWorkFile.getId());

        if (existingStagingWorkFile == null) {
            return new OperateResult(404, "暂存作业不存在");
        }

        if (existingStagingWorkFile.getAuthorId().equals(user.getId())) {
            // 暂存作业id由前端提供
            existingStagingWorkFile.setName(stagingWorkFile.getName());
            existingStagingWorkFile.setType(stagingWorkFile.getType());
            existingStagingWorkFile.setLayout(stagingWorkFile.getLayout());
            existingStagingWorkFile.setDescription(stagingWorkFile.getDescription());

            existingStagingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(existingStagingWorkFile.getDescriptionPictures()));

            existingStagingWorkFile.setStorageType(stagingWorkFile.getStorageType());
            if (StorageType.PICTURE_KEY.getValue().equals(existingStagingWorkFile.getStorageType())) {
                String key = mowerBucketService.uploadSingleWebP(stagingWorkFile.getFileContent());
                existingStagingWorkFile.setFileContent(key);
            } else {
                existingStagingWorkFile.setFileContent(stagingWorkFile.getFileContent());
            }

            existingStagingWorkFile.setFileRequest(stagingWorkFile.getFileRequest());
            existingStagingWorkFile.setAuthor(user.getUsername());
            existingStagingWorkFile.setStagingDate(commonService.getCurrentDateTime());
            if (stagingWorkFileMapper.updateById(existingStagingWorkFile) > 0)
                return new OperateResult(200, "暂存作业更新成功");
            else return new OperateResult(500, "暂存作业更新失败");
        } else {
            return new OperateResult(403, "无权限编辑他人的暂存作业");
        }
    }

    public OperateResult deleteStagingWorkFile(String token, Long wid) throws JsonProcessingException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        StagingWorkFile stagingWorkFile = stagingWorkFileMapper.selectById(wid);
        if (stagingWorkFile != null && stagingWorkFile.getAuthorId().equals(user.getId())) {

            // 检查是否已webp图片格式存储作业文件
            if (StorageType.PICTURE_KEY.getValue().equals(stagingWorkFile.getStorageType())) {
                // 如果是，删除其所拥有的唯一key，以及对象存储桶中的键值
                mowerBucketService.removeSingleWebP(stagingWorkFile.getFileContent());
                mowerBucketService.removeMultipleWebP(stagingWorkFile.getDescriptionPictures());
            }

            if (stagingWorkFileMapper.deleteById(wid) > 0) {
                return new OperateResult(200, "暂存作业删除成功");
            } else {
                return new OperateResult(500, "暂存作业删除失败");
            }
        } else {
            return new OperateResult(404, "未找到指定的暂存作业");
        }
    }

//    @ListDataRefresh({RefreshType.PK})
//    public List<StagingWorkFile> getStagingWorkFileList(String token) {
//        User user = selectUserService.getUserByToken(token);
//        if (user == null) {
//            throw new ServiceException("根据用户查询暂存作业列表时，用户找不到");
//        }
//
//        LambdaQueryWrapper<StagingWorkFile> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(StagingWorkFile::getAuthorId, user.getId());
//        return stagingWorkFileMapper.selectList(queryWrapper);
//    }

    /**
     * 根据用户设置的筛选条件查询出指定数量的暂存作业列表
     *
     * @param token                用户token
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合条件的暂存作业列表
     */
    @Cacheable("stagingWorkFileList")
    public List<StagingWorkFile> screenStagingWorkFileList(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return Collections.emptyList();
        }

        QueryWrapper<StagingWorkFile> queryWrapper = adapterService.createLimitedQueryWrapper(workFileSimpleSearch, user.getId(), StagingWorkFile.class);
        return stagingWorkFileMapper.selectList(queryWrapper);
    }
    /**
     * 获取某个用户暂存作业总数
     *
     * @param token 用户token
     * @return 该用户的暂存作业总数
     */
    @Cacheable("stagingWorkFileListCount")
    public Long getStagingWorkFileListCount(String token, WorkFileSimpleSearch workFileSimpleSearch) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return 0L;
        }

        QueryWrapper<StagingWorkFile> queryWrapper = adapterService.createQueryWrapper(workFileSimpleSearch, user.getId(), StagingWorkFile.class);
        return stagingWorkFileMapper.selectCount(queryWrapper);
    }
}
