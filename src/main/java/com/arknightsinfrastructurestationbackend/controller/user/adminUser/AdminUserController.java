package com.arknightsinfrastructurestationbackend.controller.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.aspect.tokenRefresh.ExcludeFromTokenRefresh;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.common.tools.Token;
import com.arknightsinfrastructurestationbackend.dto.info.adminUser.AdminUserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.UserChangeEmail;
import com.arknightsinfrastructurestationbackend.dto.user.UserChangePassword;
import com.arknightsinfrastructurestationbackend.service.user.adminUser.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/adminApi/user")
@AllArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @ExcludeFromTokenRefresh
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = Token.getTokenByRequest(request);

        if (token != null) {
            if (adminUserService.logout(token))
                return ResponseEntity.ok("注销成功");
            else ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("注销失败");
        } else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未提供有效的Token");

        return null;
    }

    // 更新用户名称
    @PutMapping("/updateAdminUserName")
    public ResponseEntity<Object> updateAdminUserName(HttpServletRequest request,
                                                      @RequestBody String newName) throws IllegalAccessException {
        String token = Token.getTokenByRequest(request);
        OperateResult result = adminUserService.updateAdminUserName(token, newName);
        AdminUserInfo adminUserInfo = adminUserService.getAdminUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndAdminUserInfo(result, adminUserInfo));
    }

    // 更新用户邮箱
    @PutMapping("/updateEmail")
    public ResponseEntity<Object> updateAdminUserEmail(HttpServletRequest request,
                                                       @RequestBody UserChangeEmail userChangeEmail) throws IllegalAccessException {
        String token = Token.getTokenByRequest(request);
        String ip = adminUserService.getUserIpFromRequest(request);
        OperateResult result = adminUserService.updateAdminUserEmail(token, userChangeEmail.getNewEmail(), userChangeEmail.getVerificationCode(), ip);
        AdminUserInfo adminUserInfo = adminUserService.getAdminUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndAdminUserInfo(result, adminUserInfo));
    }

    // 更新用户密码
    @PostMapping("/updatePassword")
    public ResponseEntity<Object> updateAdminUserPassword(HttpServletRequest request,
                                                          @RequestBody UserChangePassword userChangePassword) throws IllegalAccessException {
        String token = Token.getTokenByRequest(request);
        OperateResult result = adminUserService.updateAdminUserPassword(token, userChangePassword.getOldPassword(), userChangePassword.getNewPassword());
        AdminUserInfo adminUserInfo = adminUserService.getAdminUserInfo(token, true);
        return ResponseEntity.ok(new OperateResultAndAdminUserInfo(result, adminUserInfo));
    }

    private record OperateResultAndAdminUserInfo(OperateResult operateResult, AdminUserInfo adminUserInfo) {
    }
}
