package com.arknightsinfrastructurestationbackend.controller.user;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.info.InfrastructureInfo;
import com.arknightsinfrastructurestationbackend.dto.info.OperatorInfo;
import com.arknightsinfrastructurestationbackend.dto.info.UserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.UserChangeEmail;
import com.arknightsinfrastructurestationbackend.dto.user.UserChangePassword;
import com.arknightsinfrastructurestationbackend.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;


    private record OperateResultAndUserInfo(OperateResult operateResult, UserInfo userInfo) {
    }

    @ExcludeFromTokenRefresh
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = Token.getTokenByRequest(request);

        if (token != null) {
            if (userService.logout(token))
                return ResponseEntity.ok("注销成功");
            else ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("注销失败");
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供有效的Token");

        return null;
    }

    // 更新用户名称
    @PutMapping("/updateUsername")
    public ResponseEntity<Object> updateUserName(HttpServletRequest request,
                                                 @RequestBody String newName) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = userService.updateUserName(token, newName);
        UserInfo userInfo = userService.getUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndUserInfo(result, userInfo));
    }

    // 更新用户邮箱
    @PutMapping("/updateEmail")
    public ResponseEntity<Object> updateUserEmail(HttpServletRequest request,
                                                  @RequestBody UserChangeEmail userChangeEmail) {
        String token = Token.getTokenByRequest(request);
        String ip = userService.getUserIpFromRequest(request);
        OperateResult result = userService.updateUserEmail(token, userChangeEmail.getNewEmail(), userChangeEmail.getVerificationCode(), ip);
        UserInfo userInfo = userService.getUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndUserInfo(result, userInfo));
    }

    // 更新用户密码
    @PostMapping("/updatePassword")
    public ResponseEntity<Object> updateUserPassword(HttpServletRequest request,
                                                     @RequestBody UserChangePassword userChangePassword) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = userService.updateUserPassword(token, userChangePassword.getOldPassword(), userChangePassword.getNewPassword());
        UserInfo userInfo = userService.getUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndUserInfo(result, userInfo));
    }

    // 更新用户头像
    @PutMapping("/updateAvatar")
    public ResponseEntity<Object> updateUserAvatar(HttpServletRequest request,
                                                   @RequestBody String newAvatar) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = userService.updateUserAvatar(token, newAvatar);
        UserInfo userInfo = userService.getUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndUserInfo(result, userInfo));
    }

    // 更新干员养成练度
    @PutMapping("/updateOperators")
    public ResponseEntity<Object> updateUserOperators(HttpServletRequest request,
                                                      @RequestBody List<OperatorInfo> operatorInfoList) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = null;
        try {
            result = userService.updateUserOperators(token, operatorInfoList);
        } catch (IOException e) {
            result = new OperateResult(400, "干员自定义养成练度字符串异常");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
        UserInfo userInfo = userService.getUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndUserInfo(result, userInfo));
    }

    // 更新基建排布配置
    @PutMapping("/updateInfrastructure")
    public ResponseEntity<Object> updateUserInfrastructure(HttpServletRequest request,
                                                           @RequestBody List<InfrastructureInfo> infrastructureInfoList) {
        String token = Token.getTokenByRequest(request);
        OperateResult result = null;
        try {
            result = userService.updateUserInfrastructure(token, infrastructureInfoList);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new OperateResult(400, "自定义基建排布配置字符串异常"));
        }
        UserInfo userInfo = userService.getUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndUserInfo(result, userInfo));
    }
}
