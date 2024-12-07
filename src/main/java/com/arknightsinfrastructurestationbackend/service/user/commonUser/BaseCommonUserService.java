package com.arknightsinfrastructurestationbackend.service.user.commonUser;

import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.config.filter.JWTUtil;
import com.arknightsinfrastructurestationbackend.dto.user.ordinaryUser.UserLRFData;
import com.arknightsinfrastructurestationbackend.service.email.VerificationAttemptService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.EmailService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@AllArgsConstructor
public abstract class BaseCommonUserService<U> {
    protected final BaseMapper<U> userMapper;
    protected final PasswordEncoder passwordEncoder;
    protected final EmailService emailService;
    protected final VerificationAttemptService verificationAttemptService;
    protected final JWTUtil jwtUtil;
    protected final CommonService commonService;

    // 子类需要实现的抽象方法，用来通过email获取用户，用来通过token获取用户
    protected abstract U getUserByEmail(String email);

    protected abstract U getUserByToken(String token);

    protected abstract void insertUser(U user);

    protected abstract void updateUser(U user);

    protected abstract int getUserCount();

    protected abstract Long getUserId(U user);

    protected abstract String getUserEmail(U user);

    protected abstract String getUserToken(U user);

    protected abstract String getUserPassword(U user);

    protected abstract void setUserToken(U user, String token);

    protected abstract void setUserPassword(U user, String password);

    protected abstract String getUserStatus(U user);

    protected abstract void setUserEmail(U user, String email);

    protected abstract boolean isUserBanned(U user);

    // 统一验证邮箱和验证码的方法
    protected OperateResult validateEmailAndCode(UserLRFData userLRFData) {
        if (verificationAttemptService.isEmailFrozen(userLRFData.getEmail(), userLRFData.getIpAddress())) {
            long remainingFreezeTime = verificationAttemptService.getRemainingFreezeTime(userLRFData.getEmail(), userLRFData.getIpAddress());
            return new OperateResult(429, "由于多次尝试，该邮箱已被冻结，剩余冻结时间：" + remainingFreezeTime + "分钟");
        }

        OperateResult verificationResult = verifyCodeAndHandleAttempts(userLRFData.getEmail(), userLRFData.getVerificationCode(), userLRFData.getIpAddress());
        if (!verificationResult.isRight()) {
            return verificationResult;
        }
        return null;
    }

    protected OperateResult verifyCodeAndHandleAttempts(String email, String verificationCode, String ipAddress) {
        if (verificationAttemptService.isEmailFrozen(email, ipAddress)) {
            long remainingFreezeTime = verificationAttemptService.getRemainingFreezeTime(email, ipAddress);
            return new OperateResult(429, "由于多次错误尝试，邮箱已被暂时冻结，剩余冻结时间：" + remainingFreezeTime + "分钟");
        }

        OperateResult verifyResult = processEmailVerification(email, verificationCode, ipAddress);
        if (!verifyResult.isRight()) {
            return verifyResult;
        }

        // 验证成功，清除错误记录
        verificationAttemptService.clearAttempts(email, ipAddress);
        return new OperateResult(200, "验证码正确");
    }

    public OperateResult processEmailVerification(String email, String verificationCode, String ipAddress) {
        if (!emailService.verifyCode(email, verificationCode)) {
            int remainingAttempts = verificationAttemptService.recordFailedAttempt(email, ipAddress);
            if (remainingAttempts == 0) {
                long remainingFreezeTime = verificationAttemptService.getRemainingFreezeTime(email, ipAddress);
                return new OperateResult(429, "由于多次尝试，该邮箱已被冻结，剩余冻结时间：" + remainingFreezeTime + "分钟");
            }
            return new OperateResult(500, "验证码错误，还剩" + remainingAttempts + "次机会");
        }
        return new OperateResult(200, "验证码匹配正确");
    }

    public boolean logout(String token) {
        U user = getUserByToken(token);
        if (user != null) {
            setUserToken(user, "");
            updateUser(user);
            // 从安全上下文中移除认证信息
            SecurityContextHolder.clearContext();
            return true;
        }
        return false;
    }

    public OperateResult forgetPassword(UserLRFData userLRFData) {
        U existingUser = getUserByEmail(userLRFData.getEmail());
        if (existingUser == null) {
            return new OperateResult(404, "邮箱未注册，请先注册");
        }

        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        try {
            setUserPassword(existingUser, passwordEncoder.encode(userLRFData.getPassword()));
            updateUser(existingUser);
            return new OperateResult(200, "密码重置成功");
        } catch (Exception e) {
            log.error("数据库更新错误：{}", e.getMessage());
            return new OperateResult(500, "内部服务器错误，请稍后重试");
        }
    }

    public boolean deleteUser(U user) {
        // 因为这里未实现抽象, 这里可在子类中实现delete操作
        return false;
    }

    protected boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    public OperateResult login(UserLRFData userLRFData, String oldToken) {
        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        U user = getUserByEmail(userLRFData.getEmail());

        if (user == null) {
            verificationAttemptService.recordFailedAttempt(userLRFData.getEmail(), userLRFData.getIpAddress());
            return new OperateResult(401, "登录失败：用户名或密码不正确");
        }

        if (oldToken != null && oldToken.equals(getUserToken(user))) {
            return new OperateResult(204, "您已登录，无需再次登录");
        }

        if (passwordEncoder.matches(userLRFData.getPassword(), getUserPassword(user))) {
            if (isUserBanned(user)) {
                return new OperateResult(403, "用户已被封禁，无法登录");
            }
            String newToken = jwtUtil.generateUniqueCommonUserToken(getUserId(user));
            updateUserToken(getUserId(user), newToken);
            return new OperateResult(200, "登录成功", newToken);
        } else {
            verificationAttemptService.recordFailedAttempt(userLRFData.getEmail(), userLRFData.getIpAddress());
            return new OperateResult(401, "登录失败：用户名或密码不正确");
        }
    }

    public OperateResult updateUserToken(Long uid, String newToken) {
        U user = findUserById(uid);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }
        setUserToken(user, newToken);
        updateUser(user);
        return new OperateResult(200, "Token更新成功");
    }

    // 子类实现通过id查找用户
    protected abstract U findUserById(Long uid);

    public String getUserIpFromRequest(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null) {
            remoteAddr = xForwardedForHeader.split(",")[0].trim();
        }
        return remoteAddr;
    }
}
