package com.arknightsinfrastructurestationbackend.controller.workFile;

import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_StarRecord;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_WorkFile;
import com.arknightsinfrastructurestationbackend.entitiy.user.StarRecord;
import com.arknightsinfrastructurestationbackend.service.workFile.StaredWorkFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/starRecord")
@AllArgsConstructor
public class StaredWorkFileController {
    private final StaredWorkFileService staredWorkFileService;

    /**
     * 根据用户的uid获取一定量的，该用户的收藏的作业列表
     *
     * @param workFileSimpleSearch 作业筛选简单参数
     * @return 筛选出的收藏作业列表
     */
    @PostMapping("/screenStaredWorkFileList")
    public ResponseEntity<Object> screenStaredWorkFileList(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        return ResponseEntity.ok(new OperateAndStaredWorkFileListResult(new OperateResult(200, "收藏作业列表获取成功")
                , FileConverter.B2FWL(staredWorkFileService.screenStaredWorkFileList(token, workFileSimpleSearch))
                , staredWorkFileService.getStaredWorkFileListCount(token, workFileSimpleSearch)));
    }

    /**
     * 根据用户的uid获取该用户的收藏的所有作业记录列表
     *
     * @return 获取的收藏作业记录列表
     */
    @GetMapping("/getStarListForUser")
    public ResponseEntity<Object> getStarListForUser(HttpServletRequest request) {
        String token = Token.getTokenByRequest(request);
        return ResponseEntity.ok(new OperateAndStarListResult(new OperateResult(200, "收藏记录列表获取成功")
                , FileConverter.B2FSR(staredWorkFileService.getStarListForUser(token))));
    }

    /**
     * 收藏某个作业
     *
     * @param workFileSimpleSearch 仅包含作业ID的DTO
     * @return 收藏操作结果
     */
    @PostMapping("/starWorkFile")
    public ResponseEntity<Object> starWorkFile(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = staredWorkFileService.insertStarRecord(token, Long.valueOf(workFileSimpleSearch.getWid()));
        List<StarRecord> starList = staredWorkFileService.getStarListForUser(token);
        return ResponseEntity.ok(new OperateAndStarListResult(result, FileConverter.B2FSR(starList)));
    }

    /**
     * 取消收藏某个作业
     *
     * @param workFileSimpleSearch 仅包含作业ID的DTO
     * @return 取消收藏操作结果
     */
    @PostMapping("/unstarWorkFile")
    public ResponseEntity<Object> unstarWorkFile(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = staredWorkFileService.deleteStarRecord(token, Long.valueOf(workFileSimpleSearch.getWid()));
        List<StarRecord> starList = staredWorkFileService.getStarListForUser(token);
        return ResponseEntity.ok(new OperateAndStarListResult(result, FileConverter.B2FSR(starList)));
    }

    /**
     * 批量取消收藏
     *
     * @param swids 取消收藏的记录ID列表
     * @return 取消收藏操作结果
     */
    @PostMapping("/unstarMultipleWorkFiles")
    public ResponseEntity<Object> unstarMultipleWorkFiles(HttpServletRequest request, @RequestBody List<String> swids) {
        List<Long> wids = swids.stream().map(Long::parseLong).toList();
        String token = Token.getTokenByRequest(request);
        for (Long wid : wids) {
            staredWorkFileService.deleteStarRecord(token, wid);
        }
        List<StarRecord> starList = staredWorkFileService.getStarListForUser(token);
        return ResponseEntity.ok(new OperateAndStarListResult(new OperateResult(200, "批量取消收藏成功"), FileConverter.B2FSR(starList)));
    }

    private record OperateAndStarListResult(OperateResult operateResult, List<F_StarRecord> starList) {
    }

    private record OperateAndStaredWorkFileListResult(OperateResult operateResult,
                                                      List<F_WorkFile> staredWorkFileList,
                                                      int staredWorkFileListCount) {
    }
}
