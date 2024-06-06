package com.arknightsinfrastructurestationbackend.controller.user;

import com.arknightsinfrastructurestationbackend.common.tools.FileConverter;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.wrapperClass.F_UserRate;
import com.arknightsinfrastructurestationbackend.entitiy.user.UserRate;
import com.arknightsinfrastructurestationbackend.service.user.UserRateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/score")
@AllArgsConstructor
public class UserRateController {
    private final UserRateService userRateService;

    /**
     * 用户评分
     *
     * @param userRate 用户评分数据
     */
    @PostMapping("/rate")
    public ResponseEntity<Object> userRate(HttpServletRequest request, @RequestBody F_UserRate userRate) {
        String token = Token.getTokenByRequest(request);
        try {
            OperateResult result = userRateService.rate(token, FileConverter.FB(userRate));
            List<UserRate> rateRecordsForUser = userRateService.getRateRecordsForUser(token);
            return ResponseEntity.ok(new OperateAndRateRecordsResult(result, FileConverter.B2FUR(rateRecordsForUser)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OperateResult(500, "内部服务器错误"));
        }
    }

    /**
     * 根据用户的uid获取该用户的所有评分记录
     *
     * @return 获取的评分记录列表
     */
    @GetMapping("/getRateRecordsForUser")
    public ResponseEntity<Object> getRateRecordsForUser(HttpServletRequest request) {
        String token = Token.getTokenByRequest(request);
        return ResponseEntity.ok(new OperateAndRateRecordsResult(new OperateResult(200, "评分记录获取成功")
                , FileConverter.B2FUR(userRateService.getRateRecordsForUser(token))));
    }

    private record OperateAndRateRecordsResult(OperateResult operateResult,
                                               List<F_UserRate> userRates) {
    }
}
