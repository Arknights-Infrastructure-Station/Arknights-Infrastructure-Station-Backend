package com.arknightsinfrastructurestationbackend.service.user;

import com.arknightsinfrastructurestationbackend.common.tools.Log;
import com.arknightsinfrastructurestationbackend.common.tools.OperateResult;
import com.arknightsinfrastructurestationbackend.projectUtil.PropertyReader;
import com.arknightsinfrastructurestationbackend.service.email.VerificationAttemptService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class EmailService {
    private final RedisTemplate<String, String> redisTemplate;
    private final VerificationAttemptService verificationAttemptService;
    @Resource
    private JavaMailSender javaMailSender;

    public EmailService(RedisTemplate<String, String> redisTemplate, VerificationAttemptService verificationAttemptService) {
        this.redisTemplate = redisTemplate;
        this.verificationAttemptService = verificationAttemptService;
    }

    private static final int EXPIRATION_TIME = 5; // 过期时间（分钟）


    /**
     * 向邮箱发送验证码
     */
    public OperateResult sendEmailVerificationCode(String newEmail, int length,String ipAddress) {
        // 预先检测邮箱是否被冻结
        if (verificationAttemptService.isEmailFrozen(newEmail, ipAddress)) {
            return new OperateResult(429, "该邮箱已被冻结");
        }

        String code = generateVerificationCode(length);
        boolean isStored = storeVerificationCodeInRedis(newEmail, code);

        if (!isStored) {
            return new OperateResult(500, "验证码存储失败");
        }

        boolean isEmailSent = sendVerificationEmail("邮箱验证码", newEmail, code);

        if (!isEmailSent) {
            return new OperateResult(500, "验证码发送失败");
        }

        return new OperateResult(200, "验证码发送成功");
    }

    /**
     * 验证用户提供的验证码是否与Redis中存储的验证码相匹配
     *
     * @param email 用户提供的新的电子邮件地址，或需要找回密码的电子邮件地址
     * @param code  提供的验证码
     * @return 如果验证码匹配，则返回true，否则返回false
     */
    public boolean verifyCode(String email, String code) {
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            String storedCode = operations.get(email);

            return storedCode != null && storedCode.equals(code);
        } catch (Exception e) {
            Log.error("验证验证码时出错：" + e);
            return false;
        }
    }

    private String generateVerificationCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        int minValue = (int) Math.pow(10, length - 1);
        int maxValue = (int) Math.pow(10, length) - 1;
        return String.valueOf((int) (Math.random() * (maxValue - minValue + 1)) + minValue);
    }

    private boolean storeVerificationCodeInRedis(String email, String code) {
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(email, code, EXPIRATION_TIME, TimeUnit.MINUTES); //默认过期时间5分钟
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean sendVerificationEmail(String subject, String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(PropertyReader.getAuthorFromProperties());
            message.setTo(to);
            message.setSubject(subject);
            message.setText("您的验证码是：" + code + "，该验证码将在" + EXPIRATION_TIME + "分钟后过期。");

            javaMailSender.send(message);
            return true;
        } catch (MailException e) {
            Log.error(e.getMessage());
            return false;
        }
    }
}
