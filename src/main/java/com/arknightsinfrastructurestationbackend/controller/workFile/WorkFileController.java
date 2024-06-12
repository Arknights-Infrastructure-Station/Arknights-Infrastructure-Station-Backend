package com.arknightsinfrastructurestationbackend.controller.workFile;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.common.exception.ServiceException;
import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileScreen;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_WorkFile;
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
@RequestMapping("/workFile")
@AllArgsConstructor
public class WorkFileController {

    private final WorkFileService workFileService;

    /**
     * 创建作业
     *
     * @param workFile 待存储的包装类WorkFile对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<Object> insertWorkFile(HttpServletRequest request, @RequestBody F_WorkFile workFile) {
        String token = Token.getTokenByRequest(request);
        OperateResult result;
        try {
            result = workFileService.insertWorkFile(token, FileConverter.FB(workFile));
        } catch (ServiceException | JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndWorkFileListResult(result, null, null));
    }

    /**
     * 更新作业
     *
     * @param workFile 待更新的包装类WorkFile对象
     * @return 更新结果
     */
    @PostMapping("/update")
    public ResponseEntity<Object> updateWorkFile(HttpServletRequest request, @RequestBody F_WorkFile workFile) {
        String token = Token.getTokenByRequest(request);
        OperateResult result;
        try {
            result = workFileService.updateWorkFile(token, FileConverter.FB(workFile));
        } catch (JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndWorkFileListResult(result, null, null));
    }

    // 删除作业 暂时用不到，因为用户的删除逻辑是 放入回收箱->手动/自动删除回收箱作业 直接删除作业文件是开发人员后台管理用的
//    @PostMapping("/delete")
    public ResponseEntity<Object> deleteWorkFile(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = null;
        try {
            result = workFileService.deleteWorkFile(token, Long.valueOf(workFileSimpleSearch.getWid()));
        } catch (JsonProcessingException e) {
            result = new OperateResult(500, e.getMessage());
        }
        return ResponseEntity.ok(new OperateAndWorkFileListResult(result, null, null));
    }

    /**
     * 根据用户设置的筛选参数查询出符合要求的作业配置文件
     *
     * @param workFileScreen 作业筛选参数
     * @return 符合要求的作业配置文件列表
     */
    @ExcludeFromTokenRefresh
    @PostMapping("/screenWorkFileList")
    public ResponseEntity<Object> screenWorkFileList(@RequestBody WorkFileScreen workFileScreen) {
        return ResponseEntity.ok(new OperateAndWorkFileListResult
                (new OperateResult(200, "作业文件列表获取成功")
                        , FileConverter.B2FWL(workFileService.screenWorkFileList(workFileScreen))
                        , workFileService.getWorkFileListCount(workFileScreen)));
    }


    /**
     * 根据用户设置的筛选参数查询出符合要求的已发布作业配置文件
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 符合要求的已发布作业配置文件列表
     */
    @PostMapping("/screenPostedWorkFileList")
    public ResponseEntity<Object> screenPostedWorkFileList(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        return ResponseEntity.ok(new OperateAndPostedWorkFileListResult(new OperateResult(200, "已发布作业列表获取成功")
                , FileConverter.B2FWL(workFileService.screenPostedWorkFileList(token, workFileSimpleSearch))
                , workFileService.getPostedWorkFileListCount(token, workFileSimpleSearch)));
    }


    private record OperateAndWorkFileListResult(OperateResult operateResult, List<F_WorkFile> workFileList,
                                                Long workFileListCount) {
    }

    private record OperateAndPostedWorkFileListResult(OperateResult operateResult, List<F_WorkFile> postedWorkFileList,
                                                      Long postedWorkFileListCount) {
    }
}
