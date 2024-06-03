package com.arknightsinfrastructurestationbackend.service.user;

import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import com.arknightsinfrastructurestationbackend.service.email.VerificationAttemptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.arknightsinfrastructurestationbackend.common.tools.JsonWorkProcessor;
import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.config.JWTUtil;
import com.arknightsinfrastructurestationbackend.entitiy.user.User;
import com.arknightsinfrastructurestationbackend.dto.info.InfrastructureInfo;
import com.arknightsinfrastructurestationbackend.dto.info.OperatorInfo;
import com.arknightsinfrastructurestationbackend.dto.info.UserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.UserLRFData;
import com.arknightsinfrastructurestationbackend.mapper.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final SelectUserService selectUserService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationAttemptService verificationAttemptService;
    private final JWTUtil jwtUtil;
    private final CommonService commonService;

    public OperateResult register(UserLRFData userLRFData) {
        // 查询是否该邮箱已被注册
        User existingUser = selectUserService.getUserByEmail(userLRFData.getEmail());

        if (existingUser != null) {
            return new OperateResult(409, "该邮箱已注册");
        }

        // 验证邮箱状态和验证码
        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        // 验证码通过，构建一个新的用户对象，准备插入数据库
        User newUser = new User();

        // 设置默认名称为"博士" + (user_table中的记录总数 + 1)
        int userCount = Math.toIntExact(userMapper.selectCount(new QueryWrapper<>()));
        newUser.setUsername("博士" + (userCount + 1));

        newUser.setEmail(userLRFData.getEmail());
        newUser.setPassword(passwordEncoder.encode(userLRFData.getPassword()));
        newUser.setStatus("normal");
        newUser.setToken("");
        newUser.setAvatar("char_002_amiya");
        newUser.setOperators(new JSONObject().toString());
        newUser.setInfrastructure(new JSONObject().toString());

        // 插入数据
        int insertResult = userMapper.insert(newUser);

        if (insertResult > 0) {
            // 注册成功
            return new OperateResult(200, "注册成功");
        } else {
            // 注册失败（因为可能的其它原因）
            return new OperateResult(409, "注册失败，请稍后重试");
        }
    }

    public OperateResult login(UserLRFData userLRFData, String oldToken) {
        // 验证邮箱状态和验证码
        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        User user = selectUserService.getUserByEmail(userLRFData.getEmail());

        if (oldToken != null && oldToken.equals(user.getToken()))
            return new OperateResult(204, "您已登录，无需再次登录");

        if (user != null && passwordEncoder.matches(userLRFData.getPassword(), user.getPassword())) {
            // 密码匹配，生成新的Token
            String newToken = jwtUtil.generateUniqueToken(user.getId());
            updateUserToken(user.getId(), newToken); // 更新用户的Token
            return new OperateResult(200, "登录成功", newToken);
        } else {
            // 密码不匹配时记录失败尝试
            verificationAttemptService.recordFailedAttempt(userLRFData.getEmail(), userLRFData.getIpAddress());
            return new OperateResult(401, "登录失败：用户名或密码不正确");
        }
    }

    public OperateResult forgetPassword(UserLRFData userLRFData) {
        // 检查邮箱是否已注册
        User existingUser = selectUserService.getUserByEmail(userLRFData.getEmail());
        if (existingUser == null) {
            return new OperateResult(404, "邮箱未注册，请先注册");
        }

        // 验证邮箱状态和验证码
        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        try {
            // 重置密码
            existingUser.setPassword(passwordEncoder.encode(userLRFData.getPassword()));
            int updateResult = userMapper.updateById(existingUser);

            if (updateResult > 0) {
                return new OperateResult(200, "密码重置成功");
            } else {
                return new OperateResult(500, "密码重置失败，请稍后重试");
            }
        } catch (Exception e) {
            // 日志记录异常
            Log.error("数据库更新错误：" + e);
            return new OperateResult(500, "内部服务器错误，请稍后重试");
        }
    }

    private OperateResult validateEmailAndCode(UserLRFData userLRFData) {
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

    public boolean logout(String token) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getToken, token);
        User user = userMapper.selectOne(queryWrapper);

        if (user != null) {
            user.setToken(""); // 将Token设置为空字符串
            userMapper.updateById(user); // 更新用户记录

            // 从安全上下文中移除认证信息
            SecurityContextHolder.clearContext();
            return true;
        } else return false;
    }

    public OperateResult updateUserName(String token, String newName) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setUsername(newName);
        userMapper.updateById(user);
        commonService.syncFileAuthor(user); //同步数据库中所有该用户的作业记录的作者名称
        return new OperateResult(200, "名称更新成功");
    }

    public OperateResult updateUserEmail(String token, String newEmail, String verificationCode, String ipAddress) {
        // 邮箱是否已经被注册
        if (emailExists(newEmail)) {
            return new OperateResult(409, "邮箱已被占用");
        }

        // 用户是否存在
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        // 验证用户传来的验证码是否与Redis中存储的一致
        OperateResult verifyResult = processEmailVerification(newEmail, verificationCode, ipAddress);
        if (!verifyResult.isRight())
            return verifyResult;

        user.setEmail(newEmail);
        userMapper.updateById(user);
        return new OperateResult(200, "邮箱更新成功");
    }

    public OperateResult updateUserPassword(String token, String oldPassword, String newPassword) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return new OperateResult(401, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return new OperateResult(200, "密码更新成功");
    }

    public OperateResult updateUserToken(Long uid, String newToken) {
        //通过uid检索用户记录，再更新Token
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, uid);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setToken(newToken);
        userMapper.updateById(user);
        return new OperateResult(200, "Token更新成功");
    }

    public OperateResult updateUserAvatar(String token, String newAvatar) {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setAvatar(newAvatar);
        userMapper.updateById(user);
        return new OperateResult(200, "头像更新成功");
    }

    public OperateResult updateUserOperators(String token, List<OperatorInfo> operatorInfoList) throws IOException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setOperators(JsonWorkProcessor.convertListToJson(operatorInfoList));
        if (userMapper.updateById(user) > 0)
            return new OperateResult(200, "干员养成练度更新成功");
        else return new OperateResult(500, "干员养成练度更新失败");
    }

    public OperateResult updateUserInfrastructure(String token, List<InfrastructureInfo> infrastructureInfoList) throws IOException {
        User user = selectUserService.getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setInfrastructure(JsonWorkProcessor.convertListToJson(infrastructureInfoList));
        userMapper.updateById(user);
        return new OperateResult(200, "基建排布配置更新成功");
    }


    public boolean deleteUser(User user) {
        return userMapper.deleteById(user.getId()) > 0;
    }

    public UserInfo getUserInfo(String token, boolean isSensitive) {
        UserInfo userInfo = searchUserInfo(token);
        if (isSensitive) {
            assert userInfo != null;
            userInfo.handleSensitiveData();
        }
        return userInfo;
    }

    private UserInfo searchUserInfo(String token) {
        // 查找用户
        User user = selectUserService.getUserByToken(token);

        if (user == null) {
            return null;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setId(String.valueOf(user.getId()));
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setOperators(user.getOperators());
        userInfo.setInfrastructure(user.getInfrastructure());

        return userInfo;
    }


    private boolean emailExists(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    private OperateResult verifyCodeAndHandleAttempts(String email, String verificationCode, String ipAddress) {
        // 检查邮箱和IP地址组合是否被冻结
        if (verificationAttemptService.isEmailFrozen(email, ipAddress)) {
            long remainingFreezeTime = verificationAttemptService.getRemainingFreezeTime(email, ipAddress);
            return new OperateResult(429, "由于多次错误尝试，邮箱已被暂时冻结，剩余冻结时间：" + remainingFreezeTime + "分钟");
        }

        // 验证验证码
        OperateResult verifyResult = processEmailVerification(email, verificationCode, ipAddress);
        if (!verifyResult.isRight())
            return verifyResult;

        // 验证码正确，清除错误尝试记录
        verificationAttemptService.clearAttempts(email, ipAddress);
        return new OperateResult(200, "验证码正确");
    }

    public String getUserIpFromRequest(HttpServletRequest request) {
        // 获取用户的IP地址
        String remoteAddr = request.getRemoteAddr();

        // 有时候真实的客户端IP可能会在 X-Forwarded-For 请求头中
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null) {
            remoteAddr = xForwardedForHeader.split(",")[0].trim();
        }

        return remoteAddr;
    }

    public OperateResult processEmailVerification(String email, String verificationCode, String ipAddress) {
        // 验证验证码是否正确
        if (!emailService.verifyCode(email, verificationCode)) {
            // 如果验证码错误，则记录失败尝试，并获取剩余尝试次数
            int remainingAttempts = verificationAttemptService.recordFailedAttempt(email, ipAddress);
            // 判断用户是否已达到最大尝试次数
            if (remainingAttempts == 0) {
                // 获取剩余冻结时间
                long remainingFreezeTime = verificationAttemptService.getRemainingFreezeTime(email, ipAddress);
                // 返回冻结提示信息
                return new OperateResult(429, "由于多次尝试，该邮箱已被冻结，剩余冻结时间：" + remainingFreezeTime + "分钟");
            }
            // 返回剩余尝试次数提示信息
            return new OperateResult(500, "验证码错误，还剩" + remainingAttempts + "次机会");
        }
        return new OperateResult(200, "验证码匹配正确");
    }
}
