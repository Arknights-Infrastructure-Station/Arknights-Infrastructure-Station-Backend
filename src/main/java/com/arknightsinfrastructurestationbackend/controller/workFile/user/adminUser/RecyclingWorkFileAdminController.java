package com.arknightsinfrastructurestationbackend.controller.workFile.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.dto.query.adminUser.WorkFileAdminScreen;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.service.workFile.user.adminUser.RecyclingWorkFileAdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/adminApi/recyclingWorkFile")
@AllArgsConstructor
public class RecyclingWorkFileAdminController {
    private final RecyclingWorkFileAdminService recyclingWorkFileAdminService;

    /**
     * 从回收箱恢复作业
     *
     * @param workFileAdminScreen 管理员的作业筛选参数
     * @return 恢复结果、更新后的待回收作业列表、待回收作业列表总数
     */
    @PostMapping("/recoverFromRecycling")
    public ResponseEntity<Object> recoverWorkFileFromRecyclingWorkFile(@RequestBody WorkFileAdminScreen workFileAdminScreen) {
        OperateResult result;
        try {
            Long wid = Long.parseLong(workFileAdminScreen.getWid());
            result = recyclingWorkFileAdminService.recoverWorkFileFromRecyclingWorkFile(wid);
        } catch (ServiceException | NumberFormatException e) {
            result = new OperateResult(500, e.getMessage());
        }
        List<RecyclingWorkFile> recyclingWorkFiles = recyclingWorkFileAdminService.screenRecyclingWorkFileList(workFileAdminScreen);
        return ResponseEntity.ok(new OperateResultAndRecyclingWorkFileResult
                (result, FileConverter.B2FRWL(recyclingWorkFiles), recyclingWorkFileAdminService.getRecyclingWorkFileListCount(workFileAdminScreen)));
    }

    /**
     * 删除回收箱中的作业
     *
     * @param workFileAdminScreen 管理员的作业筛选参数
     * @return 删除结果、更新后的待回收作业列表、待回收作业列表总数
     */
    @PostMapping("/deleteRecyclingWorkFile")
    public ResponseEntity<Object> manuallyDeleteRecyclingWorkFile(@RequestBody WorkFileAdminScreen workFileAdminScreen) {
        OperateResult result;
        try {
            Long wid = Long.parseLong(workFileAdminScreen.getWid());
            result = recyclingWorkFileAdminService.manuallyDeleteRecyclingWorkFile(wid);
        } catch (JsonProcessingException | NumberFormatException e) {
            result = new OperateResult(500, e.getMessage());
        }
        List<RecyclingWorkFile> recyclingWorkFiles = recyclingWorkFileAdminService.screenRecyclingWorkFileList(workFileAdminScreen);
        return ResponseEntity.ok(new OperateResultAndRecyclingWorkFileResult
                (result, FileConverter.B2FRWL(recyclingWorkFiles), recyclingWorkFileAdminService.getRecyclingWorkFileListCount(workFileAdminScreen)));
    }

    /**
     * 获取回收箱中的所有作业
     *
     * @param workFileAdminScreen 管理员的作业筛选参数
     * @return 待回收作业列表、待回收作业列表总数
     */
    @PostMapping("/screenRecyclingWorkFileList")
    public ResponseEntity<Object> screenRecyclingWorkFileList(@RequestBody WorkFileAdminScreen workFileAdminScreen) {
        List<RecyclingWorkFile> recyclingWorkFiles = recyclingWorkFileAdminService.screenRecyclingWorkFileList(workFileAdminScreen);
        Long count = recyclingWorkFileAdminService.getRecyclingWorkFileListCount(workFileAdminScreen);
        return ResponseEntity.ok(new OperateResultAndRecyclingWorkFileResult
                (new OperateResult(200, "待回收作业列表获取成功"),
                        FileConverter.B2FRWL(recyclingWorkFiles),
                        count));
    }

    private record OperateResultAndRecyclingWorkFileResult(OperateResult operateResult,
                                                           List<F_RecyclingWorkFile> recyclingWorkFileList,
                                                           Long recyclingWorkFileListCount
    ) {
    }
}

