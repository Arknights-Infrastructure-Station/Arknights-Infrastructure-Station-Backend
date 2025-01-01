package com.arknightsinfrastructurestationbackend.service.user.ordinaryUser;

import com.arknightsinfrastructurestationbackend.common.tools.JsonWorkProcessor;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.config.filter.JWTUtil;
import com.arknightsinfrastructurestationbackend.dto.info.ordinaryUser.InfrastructureInfo;
import com.arknightsinfrastructurestationbackend.dto.info.ordinaryUser.OperatorInfo;
import com.arknightsinfrastructurestationbackend.dto.info.ordinaryUser.UserInfo;
import com.arknightsinfrastructurestationbackend.dto.user.UserLRFData;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.User;
import com.arknightsinfrastructurestationbackend.mapper.user.ordinaryUser.UserMapper;
import com.arknightsinfrastructurestationbackend.service.email.VerificationAttemptService;
import com.arknightsinfrastructurestationbackend.service.user.commonUser.BaseCommonUserService;
import com.arknightsinfrastructurestationbackend.service.utils.CommonService;
import org.json.JSONArray;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class UserService extends BaseCommonUserService<User> {
    private final UserMapper userMapper;
    private final SelectUserService selectUserService;

    public UserService(
            UserMapper userMapper,
            SelectUserService selectUserService,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            VerificationAttemptService verificationAttemptService,
            JWTUtil jwtUtil,
            CommonService commonService
    ) {
        super(userMapper, passwordEncoder, emailService, verificationAttemptService, jwtUtil, commonService);
        this.userMapper = userMapper;
        this.selectUserService = selectUserService;
    }

    /**
     * 用户注册逻辑。
     * 与Admin用户不同的是，这里用户名以“博士”+(userCount + 1)命名，并且包含avatar、operators、infrastructure字段的初始化。
     */
    public OperateResult register(UserLRFData userLRFData) {
        // 查询邮箱是否已注册
        User existingUser = getUserByEmail(userLRFData.getEmail());
        if (existingUser != null) {
            return new OperateResult(409, "该邮箱已注册");
        }

        // 验证邮箱和验证码
        OperateResult validationResult = validateEmailAndCode(userLRFData);
        if (validationResult != null) {
            return validationResult;
        }

        // 创建新用户
        User newUser = new User();
        int userCount = getUserCount();
        newUser.setUsername("博士" + (userCount + 1));
        newUser.setEmail(userLRFData.getEmail());
        newUser.setPassword(passwordEncoder.encode(userLRFData.getPassword()));
        newUser.setStatus("normal");
        newUser.setToken("");
        newUser.setAvatar("char_002_amiya");
        newUser.setOperators(new JSONArray().toString());
        newUser.setInfrastructure(new JSONArray().toString());

        insertUser(newUser);

        return new OperateResult(200, "注册成功");
    }

    /**
     * 更新用户名称
     */
    public OperateResult updateUserName(String token, String newName) {
        User user = getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }
        user.setUsername(newName);
        updateUser(user);
        commonService.syncFileAuthor(user); //同步该用户的作业记录的作者名称
        return new OperateResult(200, "名称更新成功");
    }

    /**
     * 更新用户邮箱
     */
    public OperateResult updateUserEmail(String token, String newEmail, String verificationCode, String ipAddress) {
        if (emailExists(newEmail)) {
            return new OperateResult(409, "邮箱已被占用");
        }

        User user = getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        OperateResult verifyResult = processEmailVerification(newEmail, verificationCode, ipAddress);
        if (!verifyResult.isRight()) {
            return verifyResult;
        }

        setUserEmail(user, newEmail);
        updateUser(user);
        return new OperateResult(200, "邮箱更新成功");
    }

    /**
     * 更新用户密码
     */
    public OperateResult updateUserPassword(String token, String oldPassword, String newPassword) {
        User user = getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        if (!passwordEncoder.matches(oldPassword, getUserPassword(user))) {
            return new OperateResult(401, "原密码错误");
        }

        setUserPassword(user, passwordEncoder.encode(newPassword));
        updateUser(user);
        return new OperateResult(200, "密码更新成功");
    }

    /**
     * 更新用户头像
     */
    public OperateResult updateUserAvatar(String token, String newAvatar) {
        User user = getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setAvatar(newAvatar);
        updateUser(user);
        return new OperateResult(200, "头像更新成功");
    }

    /**
     * 更新用户干员信息
     */
    public OperateResult updateUserOperators(String token, List<OperatorInfo> operatorInfoList) throws IOException {
        User user = getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setOperators(JsonWorkProcessor.convertListToJson(operatorInfoList));
        if (userMapper.updateById(user) > 0)
            return new OperateResult(200, "干员养成练度更新成功");
        else return new OperateResult(500, "干员养成练度更新失败");
    }

    /**
     * 更新用户基建信息
     */
    public OperateResult updateUserInfrastructure(String token, List<InfrastructureInfo> infrastructureInfoList) throws IOException {
        User user = getUserByToken(token);
        if (user == null) {
            return new OperateResult(404, "用户未找到");
        }

        user.setInfrastructure(JsonWorkProcessor.convertListToJson(infrastructureInfoList));
        updateUser(user);
        return new OperateResult(200, "基建排布配置更新成功");
    }

    /**
     * 获取用户信息
     */
    public UserInfo getUserInfo(String token, boolean isSensitive) throws IllegalAccessException {
        UserInfo userInfo = searchUserInfo(token);
        if (userInfo != null && isSensitive) {
            userInfo.handleSensitiveData();
        }
        return userInfo;
    }

    private UserInfo searchUserInfo(String token) {
        User user = getUserByToken(token);
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

    /**
     * 删除用户
     */
    public boolean deleteUser(User user) {
        return userMapper.deleteById(user.getId()) > 0;
    }

    @Override
    protected User getUserByEmail(String email) {
        return selectUserService.getByEmail(email);
    }

    @Override
    protected User getUserByToken(String token) {
        return selectUserService.getByToken(token);
    }

    @Override
    protected void insertUser(User user) {
        userMapper.insert(user);
    }

    @Override
    protected void updateUser(User user) {
        userMapper.updateById(user);
    }

    @Override
    protected int getUserCount() {
        return Math.toIntExact(userMapper.selectCount(null));
    }

    @Override
    protected Long getUserId(User user) {
        return user.getId();
    }

    @Override
    protected String getUserEmail(User user) {
        return user.getEmail();
    }

    @Override
    protected String getUserToken(User user) {
        return user.getToken();
    }

    @Override
    protected String getUserPassword(User user) {
        return user.getPassword();
    }

    @Override
    protected void setUserToken(User user, String token) {
        user.setToken(token);
    }

    @Override
    protected void setUserPassword(User user, String password) {
        user.setPassword(password);
    }

    @Override
    protected String getUserStatus(User user) {
        return user.getStatus();
    }

    @Override
    protected void setUserEmail(User user, String email) {
        user.setEmail(email);
    }

    @Override
    protected boolean isUserBanned(User user) {
        return "ban".equals(user.getStatus());
    }

    @Override
    protected User findUserById(Long uid) {
        return userMapper.selectById(uid);
    }
}
