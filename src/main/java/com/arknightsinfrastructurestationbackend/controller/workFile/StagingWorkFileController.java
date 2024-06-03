package com.arknightsinfrastructurestationbackend.controller.workFile;

import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_StagingWorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.StagingWorkFile;
import com.arknightsinfrastructurestationbackend.service.workFile.StagingWorkFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stagingWorkFile")
@AllArgsConstructor
public class StagingWorkFileController {
    private final StagingWorkFileService stagingWorkFileService;

    /**
     * 创建暂存作业
     *
     * @param workFile 待存储的包装类StagingWorkFile对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Object> insertStagingWorkFile(HttpServletRequest request, @RequestBody F_StagingWorkFile workFile) {
        String token = Token.getTokenByRequest(request);
        OperateResult result;
        try {
            result = stagingWorkFileService.insertStagingWorkFile(token, FileConverter.FB(workFile));
        } catch (ServiceException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(result, null, null));
    }

    /**
     * 更新暂存作业
     *
     * @param workFile 待更新的包装类StagingWorkFile对象
     * @return 更新结果
     */
    @PostMapping("/update")
    public ResponseEntity<Object> updateStagingWorkFile(HttpServletRequest request, @RequestBody F_StagingWorkFile workFile) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = stagingWorkFileService.updateStagingWorkFile(token, FileConverter.FB(workFile));
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(result, null, null));
    }

    /**
     * 删除暂存作业
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 删除结果
     */
    @PostMapping("/delete")
    public ResponseEntity<Object> deleteStagingWorkFile(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = stagingWorkFileService.deleteStagingWorkFile(token, Long.valueOf(workFileSimpleSearch.getWid()));
        List<StagingWorkFile> stagingWorkFileList = stagingWorkFileService.screenStagingWorkFileList(token, workFileSimpleSearch);
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult(result, FileConverter.B2FSWL(stagingWorkFileList)
                , stagingWorkFileService.getStagingWorkFileListCount(token, workFileSimpleSearch)));
    }

    /**
     * 根据用户设置的筛选参数筛选一定量的暂存作业
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 筛选出来的，一定量的暂存作业列表
     */
    @PostMapping("/screenStagingWorkFileList")
    public ResponseEntity<Object> screenStagingWorkFileList(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        return ResponseEntity.ok(new OperateAndStagingWorkFileListResult
                (new OperateResult(200, "暂存作业列表获取成功"),
                        FileConverter.B2FSWL(stagingWorkFileService.screenStagingWorkFileList(token, workFileSimpleSearch))
                        , stagingWorkFileService.getStagingWorkFileListCount(token, workFileSimpleSearch)));
    }

    private record OperateAndStagingWorkFileListResult(OperateResult operateResult,
                                                       List<F_StagingWorkFile> stagingWorkFileList,
                                                       Long stagingWorkFileListCount) {
    }
}
