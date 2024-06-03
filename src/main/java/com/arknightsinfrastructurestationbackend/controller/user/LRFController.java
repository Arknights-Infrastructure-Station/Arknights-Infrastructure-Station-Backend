package com.arknightsinfrastructurestationbackend.controller.user;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.info.UserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.UserLRFData;
import com.arknightsinfrastructurestationbackend.dto.user.UserLoginResponse;
import com.arknightsinfrastructurestationbackend.service.user.EmailService;
import com.arknightsinfrastructurestationbackend.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/lrf")
@AllArgsConstructor
@ExcludeFromTokenRefresh
public class LRFController {
    private final UserService userService;
    private final EmailService emailService;

    /**
     * 用户登录
     *
     * @param userLRFData 用户填写的表单数据
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody UserLRFData userLRFData) {
        userLRFData.setIpAddress(userService.getUserIpFromRequest(request));
        String oldToken = Token.getTokenByRequest(request);
        OperateResult loginResult = userService.login(userLRFData, oldToken);
        if (loginResult.isOk()) {
            UserInfo userInfo = userService.getUserInfo(loginResult.getResult(), true);

            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登录失败：用户信息无法获取");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + loginResult.getResult());
            headers.setAccessControlExposeHeaders(Collections.singletonList("Authorization")); //明确暴露Authorization字段

            // 创建包含用户信息和newToken的响应体
            UserLoginResponse userLoginResponse = new UserLoginResponse();
            userLoginResponse.setMessage(loginResult.getMessage());
            userLoginResponse.setUserInfo(userInfo);

            return ResponseEntity.ok().headers(headers).body(userLoginResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResult.getMessage());
        }
    }

    /**
     * 注册用户
     *
     * @param userLRFData 用户填写的表单数据
     */
    @PostMapping("/register")
    public ResponseEntity<OperateResult> registerUser(HttpServletRequest request, @RequestBody UserLRFData userLRFData) {
        userLRFData.setIpAddress(userService.getUserIpFromRequest(request));
        try {
            OperateResult result = userService.register(userLRFData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OperateResult(500, "内部服务器错误"));
        }
    }

    /**
     * 忘记密码
     *
     * @param userLRFData 用户填写的表单数据
     */
    @PostMapping("/forgetPassword")
    public ResponseEntity<OperateResult> forgetPassword(HttpServletRequest request, @RequestBody UserLRFData userLRFData) {
        userLRFData.setIpAddress(userService.getUserIpFromRequest(request));
        try {
            OperateResult result = userService.forgetPassword(userLRFData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OperateResult(500, "内部服务器错误"));
        }
    }

    /*
    注销用户需要用户权限，在UserController中
     */

    /**
     * 发送邮箱验证码，适用于“验证新邮箱是否可用”、“注册新账号”和“忘记密码”的情况
     *
     * @param email 用户要验证的电子邮件地址
     */
    @GetMapping("/sendEmailVerificationCode")
    public ResponseEntity<OperateResult> sendEmailVerificationCode(HttpServletRequest request, @RequestParam String email) {
        String ip = userService.getUserIpFromRequest(request);
        OperateResult result = emailService.sendEmailVerificationCode(email, 4, ip);
        return ResponseEntity.status(result.getOperateCode()).body(result);
    }
}
