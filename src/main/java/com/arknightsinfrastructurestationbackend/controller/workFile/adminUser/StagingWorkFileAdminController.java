package com.arknightsinfrastructurestationbackend.controller.workFile.adminUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_StagingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.service.workFile.adminUser.StagingWorkFileAdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/adminApi/stagingWorkFile")
@AllArgsConstructor
public class StagingWorkFileAdminController {
    private final StagingWorkFileAdminService stagingWorkFileAdminService;

    /**
     * 创建暂存作业
     *
     * @param workFile 待存储的包装类 F_StagingWorkFile 对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Object> insertStagingWorkFile(@RequestBody F_StagingWorkFile workFile) {
        OperateResult result;
        try {
            result = stagingWorkFileAdminService.insertStagingWorkFile(FileConverter.FB(workFile));
        } catch (ServiceException | JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(result, null, null));
    }

    /**
     * 更新暂存作业
     *
     * @param workFile 待更新的包装类 F_StagingWorkFile 对象
     * @return 更新结果
     */
    @PostMapping("/update")
    public ResponseEntity<Object> updateStagingWorkFile(@RequestBody F_StagingWorkFile workFile) {
        OperateResult result;
        try {
            result = stagingWorkFileAdminService.updateStagingWorkFile(FileConverter.FB(workFile));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(result, null, null));
    }

    /**
     * 删除暂存作业
     *
     * @param workFileAdminScreen 管理员的作业筛选参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ResponseEntity<Object> deleteStagingWorkFile(@RequestBody WorkFileAdminScreen workFileAdminScreen) {
        OperateResult result;
        try {
            Long wid = Long.parseLong(workFileAdminScreen.getWid());
            result = stagingWorkFileAdminService.deleteStagingWorkFile(wid);
        } catch (JsonProcessingException | NumberFormatException e) {
            result = new OperateResult(500, e.getMessage());
        }
        List<StagingWorkFile> stagingWorkFileList = stagingWorkFileAdminService.screenStagingWorkFileList(workFileAdminScreen);
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(result, FileConverter.B2FSWL(stagingWorkFileList)
                , stagingWorkFileAdminService.getStagingWorkFileListCount(workFileAdminScreen)));
    }

    /**
     * 根据管理员设置的筛选参数筛选一定量的暂存作业
     *
     * @param workFileAdminScreen 管理员的作业筛选参数
     * @return 筛选出来的暂存作业列表
     */
    @PostMapping("/screenStagingWorkFileList")
    public ResponseEntity<Object> screenStagingWorkFileList(@RequestBody WorkFileAdminScreen workFileAdminScreen) {
        List<StagingWorkFile> stagingWorkFiles = stagingWorkFileAdminService.screenStagingWorkFileList(workFileAdminScreen);
        Long count = stagingWorkFileAdminService.getStagingWorkFileListCount(workFileAdminScreen);
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(
                new OperateResult(200, "暂存作业列表获取成功"),
                FileConverter.B2FSWL(stagingWorkFiles),
                count));
    }

    private record OperateAndStagingWorkFileListResult(OperateResult operateResult,
                                                       List<F_StagingWorkFile> stagingWorkFileList,
                                                       Long stagingWorkFileListCount) {
    }
}

