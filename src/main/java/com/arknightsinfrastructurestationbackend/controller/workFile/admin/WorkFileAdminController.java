package com.arknightsinfrastructurestationbackend.controller.workFile.admin;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.admin.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.dto.query.admin.WorkFileId;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_WorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.service.workFile.admin.WorkFileAdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/adminApi/workFile")
@AllArgsConstructor
public class WorkFileAdminController {

    private final WorkFileAdminService workFileAdminService;

    /**
     * 创建作业
     *
     * @param workFile 待存储的包装类 F_WorkFile 对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Object> createWorkFile(@RequestBody F_WorkFile workFile) {
        OperateResult result;
        try {
            result = workFileAdminService.insertWorkFile(FileConverter.FB(workFile));
        } catch (ServiceException | JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndWorkFileListResult(result, null, null));
    }

    /**
     * 更新作业
     *
     * @param workFile 待更新的包装类 F_WorkFile 对象
     * @return 更新结果
     */
    @PostMapping("/update")
    public ResponseEntity<Object> updateWorkFile(@RequestBody F_WorkFile workFile) {
        OperateResult result;
        try {
            result = workFileAdminService.updateWorkFile(FileConverter.FB(workFile));
        } catch (JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndWorkFileListResult(result, null, null));
    }

    /**
     * 删除作业
     *
     * @param workFileId 作业 ID DTO
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ResponseEntity<Object> deleteWorkFile(@RequestBody WorkFileId workFileId) {
        OperateResult result;
        try {
            Long wid = Long.parseLong(workFileId.getWid());
            result = workFileAdminService.deleteWorkFile(wid);
        } catch (JsonProcessingException | NumberFormatException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndWorkFileListResult(result, null, null));
    }

    /**
     * 根据管理员设置的筛选参数查询出符合要求的作业配置文件
     *
     * @param workFileAdminScreen 管理员的作业筛选参数
     * @return 符合要求的作业配置文件列表
     */
    @PostMapping("/screenWorkFileList")
    public ResponseEntity<Object> screenWorkFileList(@RequestBody WorkFileAdminScreen workFileAdminScreen) {
        List<WorkFile> workFiles = workFileAdminService.screenWorkFileList(workFileAdminScreen);
        Long count = workFileAdminService.getWorkFileListCount(workFileAdminScreen);
        return ResponseEntity.ok(new OperateAndWorkFileListResult(
                new OperateResult(200, "作业文件列表获取成功"),
                FileConverter.B2FWL(workFiles),
                count));
    }

    private record OperateAndWorkFileListResult(OperateResult operateResult, List<F_WorkFile> workFileList,
                                                Long workFileListCount) {
    }
}

