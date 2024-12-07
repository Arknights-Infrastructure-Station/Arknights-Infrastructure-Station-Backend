package com.arknightsinfrastructurestationbackend.config.filter;

import com.arknightsinfrastructurestationbackend.entitiy.user.adminUser.AdminUser;
import com.arknightsinfrastructurestationbackend.entitiy.user.ordinaryUser.User;
import com.arknightsinfrastructurestationbackend.global.type.UserType;
import com.arknightsinfrastructurestationbackend.service.user.adminUser.SelectAdminUserService;
import com.arknightsinfrastructurestationbackend.service.user.ordinaryUser.SelectUserService;
import io.jsonwebtoken.*;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
public class JWTUtil {
    private final String secretKey = System.getenv("ARKNIGHTS_JWT_SECRET_KEY"); // 从环境变量获取密钥，需要管理员权限

    // 用于检查 Token 是否唯一
    @Resource
    private final SelectUserService selectUserService;
    @Resource
    private final SelectAdminUserService selectAdminUserService;

    /**
     * 生成用户 Token，从头到尾，uid 都不会被返回给前端，generateUserToken 方法由后端调用
     *
     * @param uid 用户 id
     * @return token
     */
    public String generateUserToken(Long uid) {
        return generateToken(uid, UserType.ORDINARY_USER.getName());
    }

    /**
     * 生成管理员 Token，从头到尾，uid 都不会被返回给前端，generateAdminToken 方法由后端调用
     *
     * @param adminUid 管理员 id
     * @return token
     */
    public String generateAdminToken(Long adminUid) {
        return generateToken(adminUid, UserType.ADMIN_USER.getName());
    }

    /**
     * 生成 Token 的通用方法，包含用户类型信息
     *
     * @param uid      用户或管理员的 id
     * @param userType 用户类型，"CommonUser" 或 "AdminUser"
     * @return token
     */
    private String generateToken(Long uid, String userType) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + 2592000000L; // Token 有效期，一个月
        Date exp = new Date(expMillis);

        // 生成16位长的随机字符串
        String randomString = generateRandomString(16);

        // 组合 uid、时间戳和随机字符串
        String subject = uid.toString() + "-" + nowMillis + "-" + randomString;

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .claim("userType", userType) // 添加用户类型
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    /**
     * 生成随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        while (length-- > 0) {
            int index = (int) (Math.random() * characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    /**
     * 确保生成唯一的用户 Token
     *
     * @param uid 用户 id
     * @return 唯一的 Token
     */
    public String generateUniqueCommonUserToken(Long uid) {
        return generateUniqueToken(uid, UserType.ORDINARY_USER.getName());
    }

    /**
     * 确保生成唯一的管理员 Token
     *
     * @param adminUid 管理员 id
     * @return 唯一的 Token
     */
    public String generateUniqueAdminToken(Long adminUid) {
        return generateUniqueToken(adminUid, UserType.ADMIN_USER.getName());
    }

    /**
     * 确保生成唯一的 Token
     *
     * @param uid      用户或管理员 id
     * @param userType 用户类型，"CommonUser" 或 "AdminUser"
     * @return 唯一的 Token
     */
    private String generateUniqueToken(Long uid, String userType) {
        String token;
        int maxAttempts = 50; // 限制重试次数以避免潜在的无限循环
        int attempts = 0;

        do {
            token = generateToken(uid, userType);
            attempts++;
            if (UserType.ORDINARY_USER.getName().equals(userType)) {
                // 检查用户 Token 是否唯一
                if (selectUserService.getByToken(token) == null) {
                    break;
                }
            } else if (UserType.ADMIN_USER.getName().equals(userType)) {
                // 检查管理员 Token 是否唯一
                if (selectAdminUserService.getAdminUserByToken(token) == null) {
                    break;
                }
            }
        } while (attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            throw new IllegalStateException("无法生成唯一的 Token");
        }

        return token;
    }

    /**
     * 提取 Token 中的 uid
     *
     * @param token token
     * @return uid
     */
    public Long extractUid(String token) {
        String subject = extractClaim(token, Claims::getSubject);
        String uidPart = subject.split("-")[0];
        return Long.parseLong(uidPart);
    }

    /**
     * 提取 Token 中的用户类型
     *
     * @param token token
     * @return 用户类型，"CommonUser" 或 "AdminUser"
     */
    public String extractUserType(String token) {
        return extractClaim(token, claims -> claims.get("userType", String.class));
    }

    /**
     * 提取 Token 中的指定 Claim
     *
     * @param token          token
     * @param claimsResolver 解析函数
     * @param <T>            返回类型
     * @return 提取的 Claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析 Token，提取所有 Claims
     *
     * @param token token
     * @return Claims
     */
    private Claims extractAllClaims(String token) throws SignatureException, ExpiredJwtException {
        // 解析 Token，如果有任何问题会抛出异常
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证用户 Token
     *
     * @param token token
     * @param user  用户对象
     * @return 是否有效
     */
    public Boolean validateUserToken(String token, User user) {
        final Long uid = extractUid(token);
        final String userType = extractUserType(token);
        return (UserType.ORDINARY_USER.getName().equals(userType)) &&
                (uid.equals(user.getId())) &&
                !isTokenExpired(token);
    }

    /**
     * 验证管理员 Token
     *
     * @param token     token
     * @param adminUser 管理员对象
     * @return 是否有效
     */
    public Boolean validateAdminToken(String token, AdminUser adminUser) {
        final Long uid = extractUid(token);
        final String userType = extractUserType(token);
        return (UserType.ADMIN_USER.getName().equals(userType)) &&
                (uid.equals(adminUser.getId())) &&
                !isTokenExpired(token);
    }

    /**
     * 检查 Token 是否已过期
     *
     * @param token token
     * @return 是否过期
     */
    private Boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * 检查 Token 是否将在指定时长内过期
     *
     * @param token    token
     * @param duration 时长
     * @return 是否将在指定时长内过期
     */
    public boolean isTokenExpiringWithin(String token, Duration duration) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        long diff = expiration.getTime() - System.currentTimeMillis();
        return diff < duration.toMillis();
    }
}
