package com.arknightsinfrastructurestationbackend.controller.workFile;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_WorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.RecyclingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.service.workFile.RecyclingWorkFileService;
import com.arknightsinfrastructurestationbackend.service.workFile.WorkFileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recyclingWorkFile")
@AllArgsConstructor
public class RecyclingWorkFileController {
    private final RecyclingWorkFileService recyclingWorkFileService;
    private final WorkFileService workFileService;

    /**
     * 将作业添加到回收箱
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 添加结果、更新后的已发布作业列表、已发布作业列表总数
     */
    @PostMapping("/addToRecycling")
    public ResponseEntity<Object> addRecyclingWorkFileFromWorkList(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result;
        try {
            result = recyclingWorkFileService.addRecyclingWorkFileFromWorkList(token, Long.valueOf(workFileSimpleSearch.getWid()));
        } catch (ServiceException e) {
            result = new OperateResult(500, e.getMessage());
        }
        List<WorkFile> postedWorkFileList = workFileService.screenPostedWorkFileList(token, workFileSimpleSearch);
        return ResponseEntity.ok(new OperateResultAndPostedWorkFileListResult
                (result, FileConverter.B2FWL(postedWorkFileList), workFileService.getPostedWorkFileListCount(token, workFileSimpleSearch)));
    }

    /**
     * 从回收箱恢复作业
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 恢复结果、更新后的待回收作业列表、待回收作业列表总数
     */
    @PostMapping("/recoverFromRecycling")
    public ResponseEntity<Object> recoverWorkFileFromRecyclingWorkFile(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result;
        try {
            result = recyclingWorkFileService.recoverWorkFileFromRecyclingWorkFile(token, Long.valueOf(workFileSimpleSearch.getWid()));
        } catch (ServiceException e) {
            result = new OperateResult(500, e.getMessage());
        }
        List<RecyclingWorkFile> recyclingWorkFiles = recyclingWorkFileService.screenRecyclingWorkFileList(token, workFileSimpleSearch);
        return ResponseEntity.ok(new OperateResultAndRecyclingWorkFileResult
                (result, FileConverter.B2FRWL(recyclingWorkFiles), recyclingWorkFileService.getRecyclingWorkFileListCount(token, workFileSimpleSearch)));
    }

    /**
     * 手动删除回收箱中的作业
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 删除结果、更新后的待回收作业列表、待回收作业列表总数
     */
    @PostMapping("/deleteRecyclingWorkFile")
    public ResponseEntity<Object> manuallyDeleteRecyclingWorkFile(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = null;
        try {
            result = recyclingWorkFileService.manuallyDeleteRecyclingWorkFile(token, Long.valueOf(workFileSimpleSearch.getWid()));
        } catch (JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        List<RecyclingWorkFile> recyclingWorkFiles = recyclingWorkFileService.screenRecyclingWorkFileList(token, workFileSimpleSearch);
        return ResponseEntity.ok(new OperateResultAndRecyclingWorkFileResult
                (result, FileConverter.B2FRWL(recyclingWorkFiles), recyclingWorkFileService.getRecyclingWorkFileListCount(token, workFileSimpleSearch)));
    }

    /**
     * 获取回收箱中的所有作业
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 待回收作业列表、待回收作业列表总数
     */
    @PostMapping("/screenRecyclingWorkFileList")
    public ResponseEntity<Object> screenRecyclingWorkFileList(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        return ResponseEntity.ok(new OperateResultAndRecyclingWorkFileResult
                (new OperateResult(200, "待回收作业列表获取成功")
                        , FileConverter.B2FRWL(recyclingWorkFileService.screenRecyclingWorkFileList(token, workFileSimpleSearch))
                        , recyclingWorkFileService.getRecyclingWorkFileListCount(token, workFileSimpleSearch)));
    }

    private record OperateResultAndRecyclingWorkFileResult(OperateResult operateResult,
                                                           List<F_RecyclingWorkFile> recyclingWorkFileList,
                                                           Long recyclingWorkFileListCount
    ) {
    }

    private record OperateResultAndPostedWorkFileListResult(OperateResult operateResult,
                                                            List<F_WorkFile> postedWorkFileList,
                                                            Long postedWorkFileListCount
    ) {
    }
}
