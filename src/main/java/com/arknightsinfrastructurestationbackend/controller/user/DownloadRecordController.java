package com.arknightsinfrastructurestationbackend.controller.user;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.query.WorkFileSimpleSearch;
import com.arknightsinfrastructurestationbackend.entitiy.workFile.WorkFile;
import com.arknightsinfrastructurestationbackend.service.user.DownloadRecordService;
import com.arknightsinfrastructurestationbackend.service.workFile.WorkFileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/download")
@AllArgsConstructor
@ExcludeFromTokenRefresh
public class DownloadRecordController {
    private final DownloadRecordService downloadRecordService;

    //尝试增加某个作业的下载数
    @PostMapping("/increaseDownloadCount")
    public ResponseEntity<OperateResult> increaseDownloadCount(HttpServletRequest request, @RequestBody WorkFileSimpleSearch workFileSimpleSearch) {
        String token = Token.getTokenByRequest(request);
        OperateResult operateResult = downloadRecordService.tryIncreaseDownloadCount(token, Long.valueOf(workFileSimpleSearch.getWid()));
        return ResponseEntity.ok(operateResult);
    }
}
