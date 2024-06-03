package com.arknightsinfrastructurestationbackend.controller.user;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
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
    private final WorkFileService workFileService;

    private record Result(OperateResult operateResult) {
    }

    //尝试增加某个作业的下载数
    @PostMapping("/increaseDownloadCount")
    public ResponseEntity<Object> increaseDownloadCount(HttpServletRequest request, @RequestBody String wid) {
        String token = Token.getTokenByRequest(request);
        OperateResult operateResult = downloadRecordService.tryIncreaseDownloadCount(token, Long.valueOf(wid));
        Result result = new Result(operateResult);

        return ResponseEntity.ok(result);
    }
}
