package com.arknightsinfrastructurestationbackend.service.workFile.user.commonUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.global.type.StorageType;
import com.arknightsinfrastructurestationbackend.mapper.workFile.WorkFileMapper;
import com.arknightsinfrastructurestationbackend.service.buckets.MowerBucketService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.workFile.adapter.AdapterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 基础的作业文件服务类，抽象出插入、更新、删除和处理文件存储的公共逻辑。
 */
@Service
@AllArgsConstructor
public abstract class BaseWorkFileService {
    protected final WorkFileMapper workFileMapper;
    protected final MowerBucketService mowerBucketService;
    protected final CommonService commonService;
    protected final AdapterService adapterService;

    /**
     * 执行基础的作业文件插入逻辑。
     * <p>此方法不包含用户权限校验和上传次数限制等逻辑，仅负责公共的数据处理和入库操作。</p>
     *
     * @param workFile 要插入的作业文件对象
     * @return 操作结果对象，表示插入操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    @Transactional(rollbackFor = ServiceException.class)
    protected OperateResult doInsertWorkFile(WorkFile workFile) throws JsonProcessingException {
        workFile.setReleaseDate(commonService.getCurrentDateTime());
        workFile.setScore(-1f);

        handleWorkFileStorage(workFile);

        if (workFileMapper.insert(workFile) > 0) {
            return new OperateResult(200, "作业创建成功");
        } else {
            return new OperateResult(500, "作业创建失败");
        }
    }

    /**
     * 执行基础的作业文件更新逻辑。
     * <p>此方法不包含用户权限校验逻辑，仅负责根据传入的新数据更新已有作业文件的公共字段和存储信息。</p>
     *
     * @param existingWorkFile 数据库中已存在的作业文件记录
     * @param newWorkFile      包含更新后字段信息的作业文件对象
     * @return 操作结果对象，表示更新操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    protected OperateResult doUpdateWorkFile(WorkFile existingWorkFile, WorkFile newWorkFile) throws JsonProcessingException {
        // 更新可变字段
        existingWorkFile.setName(newWorkFile.getName());
        existingWorkFile.setType(newWorkFile.getType());
        existingWorkFile.setLayout(newWorkFile.getLayout());
        existingWorkFile.setDescription(newWorkFile.getDescription());
        existingWorkFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(newWorkFile.getDescriptionPictures()));

        existingWorkFile.setStorageType(newWorkFile.getStorageType());
        if (StorageType.PICTURE_KEY.getValue().equals(existingWorkFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(newWorkFile.getFileContent());
            existingWorkFile.setFileContent(key);
        } else {
            existingWorkFile.setFileContent(newWorkFile.getFileContent());
        }

        existingWorkFile.setFileRequest(newWorkFile.getFileRequest());
        existingWorkFile.setAuthor(newWorkFile.getAuthor());
        existingWorkFile.setAuthorId(newWorkFile.getAuthorId());
        existingWorkFile.setReleaseDate(commonService.getCurrentDateTime());

        if (workFileMapper.updateById(existingWorkFile) > 0) {
            return new OperateResult(200, "作业更新成功");
        } else {
            return new OperateResult(500, "作业更新失败");
        }
    }

    /**
     * 执行基础的作业文件删除逻辑。
     * <p>此方法不包含权限校验逻辑，仅负责根据传入的作业文件对象进行删除操作，并根据其存储类型删除相关图片资源。</p>
     *
     * @param workFile 要删除的作业文件对象
     * @return 操作结果对象，表示删除操作的成功或失败状态
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    protected OperateResult doDeleteWorkFile(WorkFile workFile) throws JsonProcessingException {
        if (workFile != null) {
            if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType())) {
                mowerBucketService.removeSingleWebP(workFile.getFileContent());
                mowerBucketService.removeMultipleWebP(workFile.getDescriptionPictures());
            }

            if (workFileMapper.deleteById(workFile.getId()) > 0) {
                return new OperateResult(200, "作业删除成功");
            } else {
                return new OperateResult(500, "作业删除失败");
            }
        } else {
            return new OperateResult(404, "未找到指定的作业");
        }
    }

    /**
     * 根据作业文件的存储类型处理其内容的存储逻辑。
     * <p>如果存储类型为图片，将文件上传为WebP格式并更新描述图片列表。</p>
     *
     * @param workFile 要处理的作业文件对象
     * @throws JsonProcessingException 当处理JSON相关数据出错时抛出
     */
    protected void handleWorkFileStorage(WorkFile workFile) throws JsonProcessingException {
        if (StorageType.PICTURE_KEY.getValue().equals(workFile.getStorageType())) {
            String key = mowerBucketService.uploadSingleWebP(workFile.getFileContent());
            workFile.setFileContent(key);
        }

        workFile.setDescriptionPictures(mowerBucketService.uploadMultipleWebP(workFile.getDescriptionPictures()));
    }
}
