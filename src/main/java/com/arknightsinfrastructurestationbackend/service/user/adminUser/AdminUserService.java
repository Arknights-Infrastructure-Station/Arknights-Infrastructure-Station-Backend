package com.arknightsinfrastructurestationbackend.service.user.adminUser;

import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.config.filter.JWTUtil;
import com.arknightsinfrastructurestationbackend.dto.info.adminUser.AdminUserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.UserLRFData;
import com.arknightsinfrastructurestationbackend.entitiy.user.adminUser.AdminUser;
import com.arknightsinfrastructurestationbackend.mapper.user.adminUser.AdminUserMapper;
import com.arknightsinfrastructurestationbackend.service.email.VerificationAttemptService;
import com.arknightsinfrastructurestationbackend.service.user.commonUser.BaseCommonUserService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.EmailService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService extends BaseCommonUserService<AdminUser> {
    private final AdminUserMapper adminUserMapper;
    private final SelectAdminUserService selectAdminUserService;

    public AdminUserService(
            AdminUserMapper adminUserMapper,
            SelectAdminUserService selectAdminUserService,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            VerificationAttemptService verificationAttemptService,
            JWTUtil jwtUtil,
            CommonService commonService
    ) {
        super(adminUserMapper, passwordEncoder, emailService, verificationAttemptService, jwtUtil, commonService);
        this.adminUserMapper = adminUserMapper;
        this.selectAdminUserService = selectAdminUserService;
    }

    public OperateResult register(UserLRFData userLRFData) {
        // 检查邮箱是否已经注册
        AdminUser existingUser = getUserByEmail(userLRFData.getEmail());
        if (existingUser != null) {
            return new OperateResult(409, "该邮箱已注册");
        }

        // 验证邮箱状态和验证码
        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        // 构建新管理员用户
        AdminUser newAdminUser = new AdminUser();
        int userCount = getUserCount();
        newAdminUser.setUsername("管理员" + (userCount + 1));
        newAdminUser.setEmail(userLRFData.getEmail());
        newAdminUser.setPassword(passwordEncoder.encode(userLRFData.getPassword()));
        newAdminUser.setStatus("normal");
        newAdminUser.setToken("");

        insertUser(newAdminUser);

        return new OperateResult(200, "管理员注册成功");
    }

    // 实现抽象方法
    @Override
    protected AdminUser getUserByEmail(String email) {
        return selectAdminUserService.getByEmail(email);
    }

    @Override
    protected AdminUser getUserByToken(String token) {
        return selectAdminUserService.getByEmail(token);
    }

    @Override
    protected void insertUser(AdminUser user) {
        adminUserMapper.insert(user);
    }

    @Override
    protected void updateUser(AdminUser user) {
        adminUserMapper.updateById(user);
    }

    @Override
    protected int getUserCount() {
        return Math.toIntExact(adminUserMapper.selectCount(null));
    }

    @Override
    protected Long getUserId(AdminUser user) {
        return user.getId();
    }

    @Override
    protected String getUserEmail(AdminUser user) {
        return user.getEmail();
    }

    @Override
    protected String getUserToken(AdminUser user) {
        return user.getToken();
    }

    @Override
    protected String getUserPassword(AdminUser user) {
        return user.getPassword();
    }

    @Override
    protected void setUserToken(AdminUser user, String token) {
        user.setToken(token);
    }

    @Override
    protected void setUserPassword(AdminUser user, String password) {
        user.setPassword(password);
    }

    @Override
    protected String getUserStatus(AdminUser user) {
        return user.getStatus();
    }

    @Override
    protected void setUserEmail(AdminUser user, String email) {
        user.setEmail(email);
    }

    @Override
    protected boolean isUserBanned(AdminUser user) {
        return "ban".equals(user.getStatus());
    }

    @Override
    protected AdminUser findUserById(Long uid) {
        return adminUserMapper.selectById(uid);
    }

    public OperateResult updateAdminEmail(String token, String newEmail, String verificationCode, String ipAddress) {
        if (emailExists(newEmail)) {
            return new OperateResult(409, "邮箱已被占用");
        }

        AdminUser adminUser = getUserByToken(token);
        if (adminUser == null) {
            return new OperateResult(404, "用户未找到");
        }

        OperateResult verifyResult = processEmailVerification(newEmail, verificationCode, ipAddress);
        if (!verifyResult.isRight()) {
            return verifyResult;
        }

        setUserEmail(adminUser, newEmail);
        updateUser(adminUser);
        return new OperateResult(200, "邮箱更新成功");
    }

    public OperateResult updateAdminPassword(String token, String oldPassword, String newPassword) {
        AdminUser adminUser = getUserByToken(token);
        if (adminUser == null) {
            return new OperateResult(404, "用户未找到");
        }

        if (!passwordEncoder.matches(oldPassword, getUserPassword(adminUser))) {
            return new OperateResult(401, "原密码错误");
        }

        setUserPassword(adminUser, passwordEncoder.encode(newPassword));
        updateUser(adminUser);
        return new OperateResult(200, "密码更新成功");
    }

    /**
     * 获取管理员用户信息
     */
    public AdminUserInfo getAdminUserInfo(String token, boolean isSensitive) throws IllegalAccessException {
        AdminUserInfo adminUserInfo = searchAdminUserInfo(token);
        if (adminUserInfo != null && isSensitive) {
            adminUserInfo.handleSensitiveData();
        }
        return adminUserInfo;
    }

    private AdminUserInfo searchAdminUserInfo(String token) {
        AdminUser adminUser = getUserByToken(token);
        if (adminUser == null) {
            return null;
        }

        AdminUserInfo adminUserInfo = new AdminUserInfo();
        adminUserInfo.setId(String.valueOf(adminUserInfo.getId()));
        adminUserInfo.setUsername(adminUserInfo.getUsername());
        adminUserInfo.setEmail(adminUserInfo.getEmail());
        return adminUserInfo;
    }

    public boolean deleteAdminUser(AdminUser user) {
        return adminUserMapper.deleteById(user.getId()) > 0;
    }
}
